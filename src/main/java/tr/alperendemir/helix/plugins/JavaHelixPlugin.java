package tr.alperendemir.helix.plugins;

import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.plugins.HelixPlugin;
import tr.alperendemir.helix.api.semver.Version;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class JavaHelixPlugin implements HelixPlugin {

    private final Plugin plugin;
    private final Set<UUID> allowedWorlds = new HashSet<>();
    private final Set<UUID> allowedWorldsView = Collections.unmodifiableSet(this.allowedWorlds);
    private final boolean builtin;
    private final PluginMetadata metadata;

    public JavaHelixPlugin(Plugin plugin, PluginMetadata metadata) {
        this.plugin = plugin;

        this.builtin = plugin.getName().equals("helix-builtins");
        this.metadata = metadata;
    }

    @Override
    public Plugin getBukkitPlugin() {
        return plugin;
    }

    @Override
    public boolean isWorldAllowed(UUID id) {
        return this.builtin || id == null || this.allowedWorlds.contains(id);
    }

    @Override
    public void addAllowedWorld(UUID id) {
        this.allowedWorlds.add(id);
    }

    @Override
    public void removeAllowedWorld(UUID id) {
        this.allowedWorlds.remove(id);
    }

    @Override
    public String getOriginRepository() {
        return this.metadata.originRepository();
    }

    @Override
    public String getResourceId() {
        return this.metadata.id();
    }

    @Override
    public File getPluginFile() {
        return new File(this.plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
    }

    @Override
    public Version getPluginVersion() {
        return this.metadata.version();
    }

    @Override
    public boolean isBuiltin() {
        return this.builtin;
    }

    @Override
    public String getId() {
        return this.getBukkitPlugin().getName().toLowerCase(Locale.ROOT);
    }

    @Override
    public Set<UUID> getAllowedWorlds() {
        return this.allowedWorldsView;
    }

    public void saveAllowedWorlds() {
        this.saveAllowedWorlds(new File(this.plugin.getDataFolder(), "worldList.yml"));
    }

    public void reloadAllowedWorlds() {
        this.reloadAllowedWorlds(new File(this.plugin.getDataFolder(), "worldList.yml"));
    }

    public void saveAllowedWorlds(File worldFile) {
        if (this.builtin) return;

        var yamlFile = new YamlConfiguration();
        yamlFile.set("worlds", this.allowedWorlds.stream()
                .map(Bukkit::getWorld)
                .filter(Objects::nonNull)
                .map(World::getName)
                .toList());

        try {
            yamlFile.save(worldFile);
            Helix.commands().syncAll();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reloadAllowedWorlds(File worldList) {
        if (this.builtin) return;

        var dataFolder = worldList.getParentFile();
        if (!dataFolder.isDirectory() && !dataFolder.mkdirs()) {
            return; // TODO I don't know
        }

        this.allowedWorlds.clear();

        try {
            var yamlFile = new YamlConfiguration();

            if (!worldList.isFile()) {
                // yamlFile.set("list-mode", "whitelist");
                yamlFile.set("worlds", Bukkit.getWorlds().stream().map(World::getName).toList());
                yamlFile.save(worldList);

                for (var world : Bukkit.getWorlds()) {
                    this.addAllowedWorld(world.getUID());
                }
            } else {
                yamlFile.load(worldList);
                for (var worldName : yamlFile.getStringList("worlds")) {
                    var bukkitWorld = getWorld(worldName);
                    if (bukkitWorld == null) continue;

                    this.addAllowedWorld(bukkitWorld.getUID());
                }
            }
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private World getWorld(String name) {
        try {
            return Bukkit.getWorld(UUID.fromString(name));
        } catch (IllegalArgumentException ignored) {
            return Bukkit.getWorld(name);
        }
    }
}
