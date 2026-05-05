package tr.alperendemir.helix.worlds;

import tr.alperendemir.helix.api.words.HelixWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class BukkitHelixWorldManager implements HelixWorldManager {
    @Override
    public @NotNull Map<String, World> listAllNamed() {
        return Bukkit.getWorlds()
                .stream()
                .map(world -> Map.entry(world.getName(), world))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public @NotNull Collection<World> listWorlds() {
        return Bukkit.getWorlds();
    }

    @Override
    public @Nullable World get(@NotNull String name) {
        return Bukkit.getWorld(name);
    }

    @Override
    public @Nullable World get(@NotNull UUID uuid) {
        return Bukkit.getWorld(uuid);
    }
}
