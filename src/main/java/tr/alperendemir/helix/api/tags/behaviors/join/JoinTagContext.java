package tr.alperendemir.helix.api.tags.behaviors.join;

import tr.alperendemir.helix.api.tags.behaviors.HelixTagContext;
import org.bukkit.World;
import org.bukkit.entity.Player;

public record JoinTagContext(World world, Player player) implements HelixTagContext {


}
