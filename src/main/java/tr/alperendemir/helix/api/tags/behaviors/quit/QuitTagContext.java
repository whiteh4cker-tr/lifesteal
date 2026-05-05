package tr.alperendemir.helix.api.tags.behaviors.quit;

import tr.alperendemir.helix.api.tags.behaviors.HelixTagContext;
import org.bukkit.World;
import org.bukkit.entity.Player;

public record QuitTagContext(World world, Player player) implements HelixTagContext {

}

