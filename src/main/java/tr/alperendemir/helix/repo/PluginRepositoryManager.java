package tr.alperendemir.helix.repo;

import tr.alperendemir.helix.BukkitHelixProvider;
import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.logging.HelixLogger;
import tr.alperendemir.helix.api.logging.LoggerLevel;
import tr.alperendemir.helix.api.reporting.ErrorType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Level;

public class PluginRepositoryManager {

    private final Map<String, PluginRepository> repositories = new HashMap<>();
    private final Map<String, PluginRepository> repositoryView = Collections.unmodifiableMap(this.repositories);
    private final File pluginFolder;

    public PluginRepositoryManager(File pluginFolder) {
        this.pluginFolder = pluginFolder;
    }

    public void addRepositories(Map<String, String> urls) {
        for (var entry : urls.entrySet()) {
            var id = entry.getKey();
            var url = entry.getValue();
            this.repositories.put(id, new PluginRepository(id, url));
        }
    }

    public void cleanRepositories() {
        this.repositories.clear();
    }

    public void refreshListings(BiConsumer<String, String> repositoryFailedConsumer) {
        var i = 0;
        var size = this.repositories.size();
        for (Iterator<Map.Entry<String, PluginRepository>> iterator = this.repositories.entrySet().iterator(); iterator.hasNext(); ) {
            i++;
            var entry = iterator.next();
            try {
                entry.getValue().refreshListing();
                HelixLogger.print(Level.INFO, "<lighter:gray>Refreshed listing for '<white>%s<lighter:gray>' (%s / %s)", entry.getKey(), i, size);
            } catch (IOException exception) {
                repositoryFailedConsumer.accept(entry.getKey(), exception.getMessage());
                var text = String.format("<light:red>Failed to refresh listing for repository '<white>%s<light:red>'.", entry.getKey());
                HelixLogger.warning(text);
                HelixLogger.warning("<light:red>Message: %s", exception.getMessage());

                Helix.errors().reportError(text, ErrorType.WARNING);

                iterator.remove();
            }
        }
    }

    public void refreshListings() {
        refreshListings((s, s2) -> {});
    }

    public Map<String, PluginRepositorySearchResult> searchRepositories(String entry) {
        var out = new HashMap<String, PluginRepositorySearchResult>();

        for (var en : this.repositories.entrySet()) {
            var id = en.getKey();
            var repo = en.getValue();

            try {
                var res = repo.searchEntry(entry, true);
                out.put(id, res);
            } catch (IOException exception) {
                var text = "<light:red>Failed to search for resource '<white>%s<light:red>' in repository '<white>%s<light:red>'.".formatted(entry, id);
                HelixLogger.warning(text);
                HelixLogger.warning("<light:red>Message: %s", exception.getMessage());

                Helix.errors().reportError(text, ErrorType.WARNING);
            }
        }

        return out;
    }

    public int getPluginRepositoryCount() {
        return this.repositories.size();
    }

    public Map<String, PluginRepository> getRepositories() {
        return this.repositoryView;
    }

    public File downloadPlugin(String repoUrl, String entry, RepoDownloadCallback callback) {
        var repo = this.repositories.get(repoUrl);
        if (repo == null) {
            HelixLogger.error("<light:red>Repository '<white>%s<light:red>' not found while attempting to download resource '<white>%s<light:red>'.", repoUrl, entry);
            if (callback != null) {
                callback.onFailure();
            }
            return null;
        }

        HelixLogger.print(Level.INFO, "<light:yellow>Downloading resource '<white>%s<light:yellow>'...", entry);

        var file = getFile(entry);
        repo.download(entry, file, callback == null ? new DefaultCallback(file, entry) : callback);

        return file;
    }

    private @NotNull File getFile(String entry) {
        var versionSeparator = entry.indexOf("==");
        var entryId = versionSeparator == -1 ? entry : entry.substring(0, versionSeparator);

        var isHelix = entry.equals("helix");
        var pluginFolder = isHelix ? ((BukkitHelixProvider) Helix.provider()).getDataFolder().getParentFile() : null;
        var updateFolder = new File(pluginFolder, "update");
        if (!updateFolder.exists()) {
            updateFolder.mkdirs();
        }

        return isHelix ? new File(updateFolder, "helix.jar") : new File(this.pluginFolder, entryId + ".jar");
    }

    private static class DefaultCallback implements RepoDownloadCallback {
        private final File file;
        private final String entry;

        long downloadSize = 0L;

        private DefaultCallback(File file, String entry) {
            this.file = file;
            this.entry = entry;
        }

        @Override
        public void onSizeReceived(long size) {
            this.downloadSize = size;

            this.printProgress(0);
        }

        @Override
        public void onProgressUpdate(double progressUpdate) {
            this.printProgress(progressUpdate);
        }

        @Override
        public void onFailure() {
            HelixLogger.error("<light:red>Failed to download resource '<white>%s<light:red>'.", entry);
        }

        @Override
        public void onSuccess() {
            HelixLogger.ok("<light:green>Successfully downloaded resource '<white>%s<light:green>' <rgb:56:67:94>(%s)<light:green>.", entry, file.getAbsolutePath());
            HelixLogger.info("<light:yellow>Load it with <white>/helix load %s <light:yellow>then use <white>/helix enable %s", file.getName(), entry);
        }

        private void printProgress(double progress) {
            HelixLogger.print(
                    LoggerLevel.INFO,
                    "<yellow>Downloading resource '<white>%s<yellow>' <rgb:56:67:94>|<cyan> %.2f MB <rgb:56:67:94>| %s%.2f%%",
                    entry,
                    (double) downloadSize / 1024 / 1024,
                    progress < 30
                            ? "<light:red>"
                            : progress < 75
                            ? "<light:yellow>"
                            : "<light:green>",
                    progress
            );
        }
    }

}
