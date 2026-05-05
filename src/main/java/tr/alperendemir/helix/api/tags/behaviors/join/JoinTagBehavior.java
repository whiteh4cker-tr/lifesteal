package tr.alperendemir.helix.api.tags.behaviors.join;

import tr.alperendemir.helix.api.tags.behaviors.TagBehavior;
import org.jetbrains.annotations.ApiStatus;

public class JoinTagBehavior extends TagBehavior<JoinTagContext> {

    @ApiStatus.Internal
    public JoinTagBehavior() {

    }

    @Override
    public boolean async() {
        return false;
    }

}
