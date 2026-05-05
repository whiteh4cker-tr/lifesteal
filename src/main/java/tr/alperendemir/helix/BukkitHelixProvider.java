package tr.alperendemir.helix;

import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.HelixProvider;
import tr.alperendemir.helix.api.commands.HelixCommandRegistry;
import tr.alperendemir.helix.api.config.HelixConfigurationProvider;
import tr.alperendemir.helix.api.events.HelixEventRegistry;
import tr.alperendemir.helix.api.helper.FileHelper;
import tr.alperendemir.helix.api.items.HelixItemRegistry;
import tr.alperendemir.helix.api.logging.HelixLogger;
import tr.alperendemir.helix.api.logging.LoggerLevel;
import tr.alperendemir.helix.api.namespaced.UniqueIdentifier;
import tr.alperendemir.helix.api.players.HelixPlayerManager;
import tr.alperendemir.helix.api.plugins.loading.HelixPluginLoader;
import tr.alperendemir.helix.api.reporting.ErrorType;
import tr.alperendemir.helix.api.scheduling.HelixScheduler;
import tr.alperendemir.helix.api.screens.HelixScreenRegistry;
import tr.alperendemir.helix.api.semver.Version;
import tr.alperendemir.helix.api.tags.HelixTagManager;
import tr.alperendemir.helix.api.words.HelixWorldManager;
import tr.alperendemir.helix.commands.JavaHelixCommandRegistry;
import tr.alperendemir.helix.config.ConfigProvider;
import tr.alperendemir.helix.events.ListenerRegistry;
import tr.alperendemir.helix.items.JavaHelixItemRegistry;
import tr.alperendemir.helix.listeners.CommandListener;
import tr.alperendemir.helix.listeners.InventoryListener;
import tr.alperendemir.helix.listeners.ItemUseListener;
import tr.alperendemir.helix.listeners.PlayerConnectionListener;
import tr.alperendemir.helix.listeners.PlayerMoveWorldsListener;
import tr.alperendemir.helix.listeners.WorldLoadListener;
import tr.alperendemir.helix.listeners.custom.PlayerDamageListener;
import tr.alperendemir.helix.logging.HelixPluginLogger;
import tr.alperendemir.helix.players.BukkitHelixPlayerManager;
import tr.alperendemir.helix.plugins.loading.JavaHelixPluginLoader;
import tr.alperendemir.helix.repo.PluginRepositoryManager;
import tr.alperendemir.helix.reporting.BukkitHelixErrorReporter;
import tr.alperendemir.helix.scheduling.BukkitHelixScheduler;
import tr.alperendemir.helix.screens.HelixScreenRegistryImpl;
import tr.alperendemir.helix.tags.BukkitHelixTagManager;
import tr.alperendemir.helix.worlds.BukkitHelixWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.logging.Level;

public class BukkitHelixProvider extends JavaPlugin implements HelixProvider {

    private final JavaHelixPluginLoader helixPluginLoader = new JavaHelixPluginLoader(
            new File(getDataFolder(), "plugins")
    );
    private final ListenerRegistry listenerRegistry = new ListenerRegistry();
    private final HelixItemRegistry helixItemRegistry = new JavaHelixItemRegistry(
            UniqueIdentifier.helix("variables"),
            UniqueIdentifier.helix("item_id")
    );
    private final JavaHelixCommandRegistry helixCommandRegistry = new JavaHelixCommandRegistry();
    private final PluginRepositoryManager repositoryManager = new PluginRepositoryManager(
            this.helixPluginLoader.getPluginFolder()
    );
    private final BukkitHelixErrorReporter errorReporter = new BukkitHelixErrorReporter();
    private final ConfigProvider configManager = new ConfigProvider();
    private final BukkitHelixScheduler scheduler = new BukkitHelixScheduler();
    private final HelixScreenRegistry screenRegistry = new HelixScreenRegistryImpl();
    private final HelixWorldManager worldManager = new BukkitHelixWorldManager();
    private final HelixPlayerManager playerManager = new BukkitHelixPlayerManager();
    private final HelixTagManager tagManager = new BukkitHelixTagManager();
    private final Version VERSION;

    private Path builtinFile;

    public BukkitHelixProvider() {
        var props = new Properties();
        try {
            props.load(BukkitHelixProvider.class.getResourceAsStream("/version.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        VERSION = Version.parse(props.getProperty("VERSION", "0.0.1"));
    }

    @Override
    public void onLoad() {
        new HelixPluginLogger();

        Helix.setHelixProvider(this);
    }

    @Override
    public void onEnable() {
        if(!getDataFolder().isDirectory() && !getDataFolder().mkdir()) {
            throw new IllegalStateException("Helix could not start as it was unable to create it's data folder!");
        }

        this.errorReporter.beginSession();
        HelixLogger.printCaret(true);


        getServer().getPluginManager().registerEvents(new WorldLoadListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveWorldsListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(), this);
        getServer().getPluginManager().registerEvents(new CommandListener(this.helixCommandRegistry.getDispatcher()), this);
        getServer().getPluginManager().registerEvents(new ItemUseListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(), this);

        this.bootUp();
    }

    @Override
    public void onDisable() {
        this.unloadAll();

        this.helixPluginLoader.shutdown();

        try {
            this.errorReporter.dumpErrors(new File(getDataFolder(), "errors"));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Unable to save errors", e);
        }

        this.errorReporter.endSession();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        this.unloadAll();
        this.loadPluginsAndRepos();
    }

    // FIXME remove this shit wtf is this shit
    public void movePlugin(Plugin plugin) {
        try {
            final var name = plugin.getDescription().getName();

            var file = new File(plugin.getClass()
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());

            this.helixPluginLoader.disablePlugin(plugin);
            this.helixPluginLoader.unloadPlugin(plugin);

            var dir = this.helixPluginLoader.getPluginFolder();
            Files.move(file.toPath(), dir.toPath().resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);

            Files.deleteIfExists(file.toPath());

            HelixLogger.warning("Plugin '%s' has requested a move from the standard plugins directory to the helix plugin directory.", name);
            HelixLogger.warning("The plugin has been moved, and the server will now shutdown.");
            HelixLogger.warning("If this message keeps repeating, please go into your plugins folder, and move:");
            HelixLogger.warning("The file '%s' to 'helix/plugins/%s'", file.getName(), file.getName());

            Bukkit.shutdown();
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PluginRepositoryManager getRepositoryManager() {
        return repositoryManager;
    }

    protected boolean shouldLogBootMessages() {
        return true;
    }

    private void bootUp() {
        if (shouldLogBootMessages()) {
            HelixLogger.info("<yellow>Helix is starting, please wait.");
        }

        this.loadPluginsAndRepos();
    }

    private void loadPluginsAndRepos() {
        var repositorySize = this.repositoryManager.getPluginRepositoryCount();
        var repositorySuffix = repositorySize != 1 ? "ies" : "y";
        if (shouldLogBootMessages()) {
            HelixLogger.info("<yellow>Checking %s plugin repositor%s...", repositorySize, repositorySuffix);
        }

        this.repositoryManager.refreshListings();

        var pluginSize = 0;
        String pluginSuffix = "s";
        var files = this.helixPluginLoader.getPluginFolder().listFiles();

        if (files != null) {
            for (var file : files) {
                if (file.isDirectory()) continue;

                var isJar = FileHelper.isJar(file);
                if (!isJar) continue;

                pluginSize++;
            }

            pluginSuffix = pluginSize != 1 ? "s" : "";
            if (shouldLogBootMessages()) {
                HelixLogger.info("<yellow>Attempting to load %s plugin%s.", pluginSize, pluginSuffix);
            }
        } else {
            if (shouldLogBootMessages()) {
                HelixLogger.error("Unable to access plugin files.");
            }
        }

        this.loadAll();

        var newRepoSize = this.repositoryManager.getPluginRepositoryCount();
        repositorySuffix = newRepoSize != 1 ? "ies" : "y";

        var sb = new StringBuilder();
        for (var type : ErrorType.values()) {
            if (type == ErrorType.INTERNAL) continue;

            var count = this.errorReporter.errorCount(type);
            if (count == 0) continue;

            var name = type.getName(count);

            sb.append("<white>%s %s<white>, ".formatted(count, name));
        }
        if (!sb.isEmpty()) {
            sb.setLength(sb.length() - 9);
        }

        if (shouldLogBootMessages()) {
            HelixLogger.println(
                    sb.isEmpty() ? LoggerLevel.OK : LoggerLevel.WARNING,
                    "<bold:green>Done, loaded %s plugin%s and %s repositor%s. %s",
                    pluginSize,
                    pluginSuffix,
                    newRepoSize,
                    repositorySuffix,
                    sb
            );
        }
    }

    private void unloadAll() {
        this.helixPluginLoader.unloadAll();
        this.repositoryManager.cleanRepositories();
        this.errorReporter.getSession().clear();

        try {
            if (this.builtinFile != null) {
                Files.delete(this.builtinFile);
            }
        } catch (IOException e) {
            HelixLogger.error(e);
        }
    }

    private void loadAll() {
        try(var resource = getClass().getResourceAsStream("/builtin.jar")) {
            if (resource == null) return;

            this.builtinFile = this.getDataFolder().toPath().resolve("plugins/builtin.jar");
            var stream = Files.newOutputStream(this.builtinFile, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            resource.transferTo(stream);
        } catch (IOException e) {
            HelixLogger.reportError(e);
        }

        this.helixPluginLoader.loadAll();
        this.helixPluginLoader.enableAll();
    }

    @Override
    public @NotNull HelixPluginLoader pluginLoader() {
        return this.helixPluginLoader;
    }

    @Override
    public @NotNull HelixEventRegistry events() {
        return this.listenerRegistry;
    }

    @Override
    public @NotNull HelixItemRegistry items() {
        return this.helixItemRegistry;
    }

    @Override
    public @NotNull HelixCommandRegistry commands() {
        return this.helixCommandRegistry;
    }

    @Override
    public @NotNull BukkitHelixErrorReporter errors() {
        return this.errorReporter;
    }

    @Override
    public @NotNull HelixConfigurationProvider config() {
        return this.configManager;
    }

    @Override
    public @NotNull HelixScheduler scheduler() {
        return this.scheduler;
    }

    @Override
    public @NotNull HelixTagManager tags() {
        return this.tagManager;
    }

    @Override
    public @NotNull Version version() {
        return VERSION;
    }

    @Override
    public @NotNull File providerFile() {
        return getFile();
    }

    @Override
    public @NotNull HelixScreenRegistry screens() {
        return this.screenRegistry;
    }

    @Override
    public @NotNull HelixWorldManager worlds() {
        return this.worldManager;
    }

    @Override
    public @NotNull HelixPlayerManager players() {
        return this.playerManager;
    }
}
