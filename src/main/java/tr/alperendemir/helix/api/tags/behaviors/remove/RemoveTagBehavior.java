package tr.alperendemir.helix.api.tags.behaviors.remove;

import tr.alperendemir.helix.api.tags.behaviors.TagBehavior;
import org.jetbrains.annotations.ApiStatus;

public class RemoveTagBehavior extends TagBehavior<RemoveTagContext> {

    @ApiStatus.Internal
    public RemoveTagBehavior() {

    }

    @Override
    public boolean async() {
        return false;
    }

}
