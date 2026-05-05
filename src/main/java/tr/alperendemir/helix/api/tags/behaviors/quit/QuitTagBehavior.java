package tr.alperendemir.helix.api.tags.behaviors.quit;

import tr.alperendemir.helix.api.tags.behaviors.TagBehavior;
import org.jetbrains.annotations.ApiStatus;

public class QuitTagBehavior extends TagBehavior<QuitTagContext> {

    @ApiStatus.Internal
    public QuitTagBehavior() {

    }

    @Override
    public boolean async() {
        return false;
    }

}

