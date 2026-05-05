package tr.alperendemir.helix.api.tags.behaviors.add;

import tr.alperendemir.helix.api.tags.behaviors.HelixTagContext;
import org.bukkit.World;

import java.util.UUID;

public record AddTagContext(World world, UUID target) implements HelixTagContext {


}
