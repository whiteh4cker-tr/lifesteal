package tr.alperendemir.helix.plugins;

import tr.alperendemir.helix.api.plugins.HelixPlugin;
import tr.alperendemir.helix.api.semver.Version;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FallbackHelixPlugin implements HelixPlugin {

    private static final Map<Plugin, FallbackHelixPlugin> CACHE = new ConcurrentHashMap<>();

    private final Plugin plugin;
    private final Version version;

    private FallbackHelixPlugin(Plugin plugin) {
        this.plugin = plugin;
        var parsed = Version.parse(plugin.getDescription().getVersion());
        this.version = parsed == null ? Version.of(0, 0, 0) : parsed;
    }

    public static FallbackHelixPlugin of(Plugin plugin) {
        return CACHE.computeIfAbsent(plugin, FallbackHelixPlugin::new);
    }

    @Override
    public String getId() {
        return this.plugin.getName().toLowerCase();
    }

    @Override
    public Set<UUID> getAllowedWorlds() {
        return Collections.emptySet();
    }

    @Override
    public Plugin getBukkitPlugin() {
        return this.plugin;
    }

    @Override
    public boolean isWorldAllowed(UUID id) {
        return true;
    }

    @Override
    public void addAllowedWorld(UUID id) {
        // Non-Helix plugins are always allowed.
    }

    @Override
    public void removeAllowedWorld(UUID id) {
        // Non-Helix plugins are always allowed.
    }

    @Override
    public String getOriginRepository() {
        return "";
    }

    @Override
    public String getResourceId() {
        return "";
    }

    @Override
    public File getPluginFile() {
        return new File(this.plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
    }

    @Override
    public Version getPluginVersion() {
        return this.version;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }
}

