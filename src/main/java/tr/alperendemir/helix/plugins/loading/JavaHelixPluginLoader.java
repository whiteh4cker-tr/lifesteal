package tr.alperendemir.helix.plugins.loading;

import tr.alperendemir.helix.BukkitHelixProvider;
import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.logging.HelixLogger;
import tr.alperendemir.helix.api.namespaced.UniqueIdentifier;
import tr.alperendemir.helix.api.plugins.HelixPlugin;
import tr.alperendemir.helix.api.plugins.loading.HelixPluginLoader;
import tr.alperendemir.helix.api.semver.Version;
import tr.alperendemir.helix.plugins.JavaHelixPlugin;
import tr.alperendemir.helix.plugins.PluginMetadata;
import tr.alperendemir.helix.screens.HelixScreenRegistryImpl;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class JavaHelixPluginLoader implements HelixPluginLoader {

    private static final MethodHandle BUKKIT_PLUGINS_HANDLE;
    private static final MethodHandle BUKKIT_PLUGIN_NAMES_HANDLE;

    static {
        Class<?> pluginManagerClass = Bukkit.getPluginManager().getClass();

        try {
            MethodHandles.Lookup pluginManagerLookup = MethodHandles.privateLookupIn(pluginManagerClass, MethodHandles.lookup());

            BUKKIT_PLUGINS_HANDLE = pluginManagerLookup.findGetter(pluginManagerClass, "plugins", List.class);
            BUKKIT_PLUGIN_NAMES_HANDLE = pluginManagerLookup.findGetter(pluginManagerClass, "lookupNames", Map.class);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private final Map<String, JavaHelixPlugin> plugins = new HashMap<>();
    private final Map<String, HelixPlugin> pluginsView = Collections.unmodifiableMap(this.plugins);

    private final PluginFolderWatcher folderWatcher;

    private final File pluginFolder;
    private boolean pluginFolderDirty;

    public JavaHelixPluginLoader(File pluginFolder) {
        this.pluginFolder = pluginFolder;

        this.folderWatcher = new PluginFolderWatcher(this.pluginFolder, () -> this.pluginFolderDirty = true);
        this.folderWatcher.start();
    }

    @Override
    public HelixPlugin load(File file) throws InvalidPluginException, InvalidDescriptionException {
        file = file.getAbsoluteFile();

        var id = this.getPluginIdInternal(file);
        if (id == null) {
            HelixLogger.reportError("Unable to load plugin '%s' as it has an invalid plugin file!", file.getName());
            return null;
        }

        if (id.equals(UniqueIdentifier.HELIX_NAMESPACE)) {
            HelixLogger.reportError("Plugin '%s' is named 'helix' even though that is a reserved name!", file.getName());
            return null;
        }

        if (Bukkit.getPluginManager().getPlugin(id) != null) { // Check for bukkit plugin using the plugin's name
            HelixLogger.reportError("Unable to load plugin file '%s' as it is already loaded (with id '%s')!", file.getName(), id);
            // this.disableFile(file);
            return null;
        }

        var metadata = new PluginMetadata();
        metadata.setVersion(Version.of(1, 0, 0));
        metadata.setId(file.getName().substring(0, file.getName().lastIndexOf('.')));

        if (!id.equals("helix-builtins") && !this.loadMetadata(metadata, file)) { // Make sure the metadata exists
            return null;
        }

        var lowercaseId = id.toLowerCase(Locale.ROOT);
        if (this.plugins.containsKey(lowercaseId)) { // Make sure that a plugin with the same lowercased ID doesn't exist
            HelixLogger.reportError("Unable to load plugin file '%s' as it is already loaded as a helix plugin (with id '%s')!", file.getName(), lowercaseId);
            // this.disableFile(file);
            return null;
        }

        Plugin plugin = Bukkit.getPluginManager().loadPlugin(file);
        if (plugin == null) {
            return null;
        }

        return this.onPluginLoad(plugin, metadata);
    }

    private void disableFile(File file) {
        try {
            Files.move(
                    file.toPath(),
                    new File(file.getParentFile(), file.getName() + ".disabled").toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            HelixLogger.reportError(e);
        }
    }

    private boolean loadMetadata(PluginMetadata metadata, File file) {
        var downloadEntry = new ZipEntry("download.info");
        try (var jarFile = new JarFile(file);
             var stream = jarFile.getInputStream(downloadEntry)) {
            if (stream == null) {
                return true;
            }

            return loadEntry(metadata, file, stream);
        } catch (IOException exception) {
            HelixLogger.reportError("Unable to load plugin '%s' as it has no metadata!");
            return false;
        }
    }

    private boolean loadEntry(PluginMetadata metadata, File file, InputStream stream) throws IOException {
        var text = new String(stream.readAllBytes());

        var entries = text.split("\n");

        for(var entry : entries) {
            var splitIndex = entry.indexOf(": ");
            if (splitIndex == -1) {
                HelixLogger.reportError("Invalid info entry '%s' in '%s'!", entry, file.getName());
                return false;
            }

            var key = entry.substring(0, splitIndex).toLowerCase(Locale.ROOT);
            var value = entry.substring(splitIndex + 2);

            switch (key) {
                case "repository" -> metadata.setOriginRepository(value);
                case "entry" -> metadata.setId(value);
                case "version" -> metadata.setVersion(Version.parse(value));
            }
        }

        return true;
    }

    public void unload(HelixPlugin plugin) {
        var removed = this.plugins.remove(plugin.getId());
        if (removed == null) {
            throw new IllegalArgumentException("Cannot unload a plugin which wasn't loaded as a helix plugin!");
        }

        this.onPluginUnload(removed);
    }

    public int loadAll() {
        if (!this.pluginFolder.isDirectory()) {
            return -1;
        }

        this.pluginFolderDirty = false;

        var files = this.pluginFolder.listFiles();
        var loadedCount = 0;
        if (files != null) {
            for (var file : files) {
                if (file.isDirectory() || !file.getName().endsWith(".jar")) continue;

                try {
                    var plugin = this.load(file);
                    if (plugin != null) {
                        loadedCount++;
                    }
                } catch (InvalidPluginException | InvalidDescriptionException e) {
                    HelixLogger.reportError(e);
                }
            }
        }

        return loadedCount;
    }

    public void unloadAll() {
        for (var plugin : this.plugins.values()) {
            this.onPluginUnload(plugin);
        }

        this.plugins.clear();
    }

    @Override
    public void enable(HelixPlugin plugin) {
        if (!(plugin instanceof JavaHelixPlugin javaHelixPlugin)) {
            throw new IllegalArgumentException("The JavaHelixPluginLoader can only enable JavaHelixPlugin instances!");
        }

        if (!this.plugins.containsKey(plugin.getId())) {
            throw new IllegalArgumentException("Cannot enable a plugin which wasn't loaded as a helix plugin!");
        }

        this.onPluginEnable(javaHelixPlugin);
    }

    public void enableAll() {
        for (var plugin : this.plugins.values()) {
            this.onPluginEnable(plugin);
        }
    }

    @Override
    public void disable(HelixPlugin plugin) {
        if (!(plugin instanceof JavaHelixPlugin javaMetaPlugin)) {
            throw new IllegalArgumentException("The JavaHelixPluginLoader can only disable JavaHelixPlugin instances!");
        }

        if (!this.plugins.containsKey(plugin.getId())) {
            throw new IllegalArgumentException("Cannot disable a plugin which wasn't loaded as a helix plugin!");
        }

        this.onPluginDisable(javaMetaPlugin);
    }

    @Override
    public HelixPlugin reload(HelixPlugin plugin) throws URISyntaxException, InvalidPluginException, InvalidDescriptionException {
        var filePath = plugin.getBukkitPlugin()
                .getClass()
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
                .getPath();
        var jarFile = new File(filePath);
        this.unload(plugin);

        var loaded = this.load(jarFile);
        if (loaded == null) {
            return null;
        }

        this.enable(loaded);
        return loaded;
    }

    public void disableAll() {
        for (var plugin : this.plugins.values()) {
            this.onPluginDisable(plugin);
        }
    }

    public void shutdown() {
        this.folderWatcher.shutdown();
    }

    @Override
    public File getPluginFolder() {
        return this.pluginFolder;
    }

    @Override
    public boolean isPluginDirectoryDirty() {
        return this.pluginFolderDirty;
    }

    @Override
    public String getPluginId(File file) {
        var id = this.getPluginIdInternal(file);
        if (id == null) return null;

        return id.toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean isLoaded(File file) {
        return this.plugins.containsKey(this.getPluginId(file));
    }

    @Override
    public Map<String, HelixPlugin> getPlugins() {
        return this.pluginsView;
    }

    private String getPluginIdInternal(File file) {
        try {
            return JavaPlugin.getPlugin(BukkitHelixProvider.class).getPluginLoader().getPluginDescription(file).getName();
        } catch (InvalidDescriptionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private HelixPlugin onPluginLoad(Plugin plugin, PluginMetadata metadata) {
        var helixPlugin = new JavaHelixPlugin(plugin, metadata);
        this.plugins.put(helixPlugin.getId(), helixPlugin);
        plugin.onLoad();

        return helixPlugin;
    }

    private void onPluginEnable(JavaHelixPlugin plugin) {
        Plugin bukkitPlugin = plugin.getBukkitPlugin();
        if (bukkitPlugin.isEnabled()) return;

        plugin.reloadAllowedWorlds();
        bukkitPlugin.getPluginLoader().enablePlugin(bukkitPlugin);

        var commandRegistry = Helix.commands();
        commandRegistry.syncAll();
    }

    private void onPluginDisable(JavaHelixPlugin plugin) {
        Plugin bukkitPlugin = plugin.getBukkitPlugin();
        if (!bukkitPlugin.isEnabled()) return;

        this.disablePlugin(bukkitPlugin);
    }

    private void onPluginUnload(JavaHelixPlugin plugin) {
        this.onPluginDisable(plugin);
        this.unloadPlugin(plugin.getBukkitPlugin());
    }

    @ApiStatus.Internal
    public void disablePlugin(Plugin plugin) {
        var ids = Helix.items().getKeys(plugin);
        for (var id : ids) {
            Helix.items().unregister(id);
        }

        var pluginId = plugin.getName().toLowerCase(Locale.ROOT);
        var iter = Bukkit.recipeIterator();
        while (iter.hasNext()) {
            var recipe = iter.next();

            if (recipe instanceof Keyed keyed) {
                if (keyed.getKey().getNamespace().equals(pluginId)) {
                    iter.remove();
                    Bukkit.removeRecipe(keyed.getKey());
                }
            }
        }

        var commandRegistry = Helix.commands();
        commandRegistry.unregisterAll(plugin);
        commandRegistry.syncAll();

        var id = plugin.getName().toLowerCase(Locale.ROOT);
        ((HelixScreenRegistryImpl) Helix.screens()).unregisterAll(id);

        HandlerList.unregisterAll(plugin);
        plugin.getPluginLoader().disablePlugin(plugin);
    }

    @ApiStatus.Internal
    @SuppressWarnings("unchecked")
    public void unloadPlugin(Plugin plugin) {
        var manager = Bukkit.getPluginManager();

        List<Plugin> bukkitPlugins;
        Map<String, Plugin> bukkitPluginNames;

        try {
            bukkitPlugins = (List<Plugin>) BUKKIT_PLUGINS_HANDLE.invoke(manager);
            bukkitPluginNames = (Map<String, Plugin>) BUKKIT_PLUGIN_NAMES_HANDLE.invoke(manager);
        } catch (Throwable throwable) {
            return;
        }

        bukkitPlugins.remove(plugin);
        bukkitPluginNames.remove(plugin.getName());
    }

}
