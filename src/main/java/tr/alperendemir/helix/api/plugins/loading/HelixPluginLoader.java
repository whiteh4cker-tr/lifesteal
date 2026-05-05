package tr.alperendemir.helix.api.plugins.loading;

import tr.alperendemir.helix.api.plugins.HelixPlugin;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

public interface HelixPluginLoader {

    File getPluginFolder();

    /**
     * @return If the plugin directory was changed since the last load.
     * */
    boolean isPluginDirectoryDirty();

    @Nullable
    String getPluginId(File file);

    boolean isLoaded(File file);

    /**
     * Attempts to load a single plugin file
     * @return The loaded plugin, null if it was unable to be loaded.
     * */
    @Nullable
    HelixPlugin load(File file) throws InvalidPluginException, InvalidDescriptionException;

    /**
     * Attempts to unload a single plugin file
     * */
    void unload(HelixPlugin plugin);

    /**
     * Attempts to load a helix plugin.
     * */
    void enable(HelixPlugin plugin);

    /**
     * Attempts to disable a helix plugin.
     * */
    void disable(HelixPlugin plugin);

    /**
     * Attempts to reload a helix plugin.
     * */
    HelixPlugin reload(HelixPlugin plugin) throws URISyntaxException, InvalidPluginException, InvalidDescriptionException;

    /**
     * Attempts to unload a single plugin file
     * */
    default boolean unload(String key) {
        var plugin = getPlugin(key);
        if (plugin != null) {
            unload(plugin);
        }

        return plugin != null;
    }

    /**
     * Attempts to load a helix plugin.
     * */
    default boolean enable(String key) {
        var plugin = getPlugin(key);
        if (plugin != null) {
            enable(plugin);
        }

        return plugin != null;
    }

    /**
     * Attempts to disable a helix plugin.
     * */
    default boolean disable(String key) {
        var plugin = getPlugin(key);
        if (plugin != null) {
            disable(plugin);
        }

        return plugin != null;
    }

    default HelixPlugin reload(String key) throws URISyntaxException, InvalidPluginException, InvalidDescriptionException {
        var plugin = getPlugin(key);
        if (plugin != null) {
            return reload(plugin);
        }

        return null;
    }

    Map<String, HelixPlugin> getPlugins();

    default HelixPlugin getPlugin(String key) {
        return getPlugins().get(key);
    }

}
