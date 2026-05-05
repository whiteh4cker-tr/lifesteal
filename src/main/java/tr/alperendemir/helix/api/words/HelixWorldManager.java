package tr.alperendemir.helix.api.words;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

// TODO
public interface HelixWorldManager {

    @NotNull
    Map<String, World> listAllNamed();

    @NotNull
    Collection<World> listWorlds();

    @Nullable
    World get(@NotNull String name);

    @Nullable
    World get(@NotNull UUID uuid);

}
