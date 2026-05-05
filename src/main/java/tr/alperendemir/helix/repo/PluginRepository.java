package tr.alperendemir.helix.repo;

import com.google.gson.JsonObject;
import tr.alperendemir.helix.BukkitHelixProvider;
import tr.alperendemir.helix.utils.JsonUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PluginRepository {

    private final String id;
    private final String url;
    private final Map<String, PluginRepositoryEntry> entries = new ConcurrentHashMap<>();
    private final Map<String, PluginRepositoryEntry> entriesView = Collections.unmodifiableMap(this.entries);

    public PluginRepository(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public String getId() {
        return this.id;
    }

    public String getUrl() {
        return this.url;
    }

    public boolean ping() throws IOException {
        var connection = this.establishConnection("ping", "");
        if (connection == null) return false;
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return false;

        var all = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return all.equals("Pong!");
    }

    public void refreshListing() throws IOException {
        this.entries.clear();

        var connection = this.establishConnection("list", "");
        if (connection == null) return;

        var all = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        var response = JsonUtils.GSON.fromJson(all, JsonObject.class);

        for (var element : response.entrySet()) {
            this.entries.put(element.getKey(), PluginRepositoryEntry.fromJson((JsonObject) element.getValue()));
        }
    }

    @Nullable
    public PluginRepositoryEntry getEntry(String entry, boolean fetchNew) throws IOException {
        if (!fetchNew) {
            return this.entries.get(entry);
        }

        var connection = this.establishConnection("info", entry);
        if (connection == null) return null;

        return PluginRepositoryEntry.fromJson(connection.getInputStream());
    }

    @Nullable
    public PluginRepositoryEntry getOrFetchEntry(String entry) throws IOException {
        var gotten = this.entries.get(entry);
        if (gotten != null) {
            return gotten;
        }

        var connection = this.establishConnection("info", entry);
        if (connection == null) return null;

        return PluginRepositoryEntry.fromJson(connection.getInputStream());
    }

    public PluginRepositorySearchResult searchEntry(String entry, boolean fetchMissingEntries) throws IOException {
        var connection = this.establishConnection("search", entry);
        if (connection == null) return null;

        var all = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        var object = JsonUtils.GSON.fromJson(all, JsonObject.class);

        Function<String, PluginRepositoryEntry> getter = fetchMissingEntries ? en -> {
            try {
                return getOrFetchEntry(en);
            } catch (IOException e) {
                var plugin = JavaPlugin.getPlugin(BukkitHelixProvider.class);
                plugin.getLogger().log(Level.SEVERE, "Unable to download plugin information", e);
                return null;
            }
        } : en -> {
            try {
                return this.getEntry(en, false);
            } catch (IOException e) {
                var plugin = JavaPlugin.getPlugin(BukkitHelixProvider.class);
                plugin.getLogger().log(Level.SEVERE, "Unable to download plugin information", e);
                return null;
            }
        };

        var best = getter.apply(object.get("best").getAsString());

        var out = object.getAsJsonObject("results").entrySet()
                .stream()
                .map(element -> {
                    var key = getter.apply(element.getKey());
                    if (key == null) return null;

                    return Map.entry(key, element.getValue().getAsInt());
                })
                .filter(Objects::nonNull)
                .sorted((o1, o2) -> -Integer.compare(o1.getValue(), o2.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));

        var firstVal = out.values().iterator().next();

        return new PluginRepositorySearchResult(out, best, firstVal);
    }

    public void download(String entry, File file, RepoDownloadCallback callback) {
        try {
            var remoteInfo = this.getEntry(entry, true);
            if (remoteInfo == null) {
                callback.onFailure();
                return;
            }

            var connection = this.establishConnection("download", entry);
            if (connection == null) {
                callback.onFailure();
                return;
            }

            var contentLength = this.getConnectionSize(connection);
            callback.onSizeReceived(contentLength);

            try (var fc = FileChannel.open(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                 var channel = new ProgressByteChannel(
                         Channels.newChannel(connection.getInputStream()),
                         contentLength,
                         callback::onProgressUpdate
                 )) {

                fc.transferFrom(
                        channel,
                        0,
                        contentLength
                );
            }

            var tempFile = new File(file.getParentFile(), file.getName() + ".tmp");
            try(var jarFile = new JarFile(file);
                var out = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)))) {
                var entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    var current = entries.nextElement();
                    var stream = jarFile.getInputStream(current);

                    out.putNextEntry(current);
                    stream.transferTo(out);
                    out.closeEntry();
                }

                out.putNextEntry(new JarEntry("download.info"));

                var info = """
repository: %s
entry: %s
version: %s
""".formatted(this.id, entry, remoteInfo.requestedVersion().toString());

                out.write(info.getBytes(StandardCharsets.UTF_8));

                out.closeEntry();
            } catch (IOException exception) {
                exception.printStackTrace();
            }

            try(var fc = FileChannel.open(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                var oc = Files.newInputStream(tempFile.toPath());
                var lock = fc.lock()) {

                fc.transferFrom(Channels.newChannel(oc), 0, oc.available());

                tempFile.delete();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            callback.onSuccess();
        } catch (IOException e) {
            var plugin = JavaPlugin.getPlugin(BukkitHelixProvider.class);
            plugin.getLogger().log(Level.SEVERE, "Unable to download plugin", e);

            callback.onFailure();
        }
    }

    private HttpURLConnection establishConnection(String mode, String entry) throws IOException {
        var url = this.getUrl(mode, entry);
        var connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.connect();

        var responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            return null;
        }

        return connection;
    }

    private long getConnectionSize(HttpURLConnection connection) {
        var length = connection.getContentLengthLong();
        return length <= 0 ? Long.MAX_VALUE : length;
    }

    public Map<String, PluginRepositoryEntry> getEntries() {
        return this.entriesView;
    }

    private URL getUrl(String... paths) throws MalformedURLException {
        var urlPath = String.join("/", this.url, String.join("/", paths));
        if (!urlPath.startsWith("http")) {
            return new URL("http://" + urlPath);
        }

        return new URL(urlPath);
    }
}
