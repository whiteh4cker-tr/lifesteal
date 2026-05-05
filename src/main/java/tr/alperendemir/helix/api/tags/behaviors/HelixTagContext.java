package tr.alperendemir.helix.api.tags.behaviors;

import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public interface HelixTagContext {

    /**
     * Gets the world for this context, used to filter tag events.
     * */
    @Nullable
    World world();

}
