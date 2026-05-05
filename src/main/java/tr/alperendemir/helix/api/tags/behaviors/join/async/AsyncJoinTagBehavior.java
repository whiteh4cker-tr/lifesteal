package tr.alperendemir.helix.api.tags.behaviors.join.async;

import tr.alperendemir.helix.api.tags.behaviors.TagBehavior;
import org.jetbrains.annotations.ApiStatus;

public class AsyncJoinTagBehavior extends TagBehavior<AsyncJoinTagContext> {

    @ApiStatus.Internal
    public AsyncJoinTagBehavior() {

    }

    @Override
    public boolean async() {
        return true;
    }

}
