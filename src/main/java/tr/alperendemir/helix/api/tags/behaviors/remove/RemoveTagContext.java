package tr.alperendemir.helix.api.tags.behaviors.remove;

import tr.alperendemir.helix.api.tags.behaviors.HelixTagContext;
import org.bukkit.World;

import java.util.UUID;

public record RemoveTagContext(World world, UUID target) implements HelixTagContext {


}
