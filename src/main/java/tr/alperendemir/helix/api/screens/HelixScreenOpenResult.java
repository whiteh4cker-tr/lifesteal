package tr.alperendemir.helix.api.screens;

import tr.alperendemir.helix.api.screens.components.HelixPageComponent;
import org.jetbrains.annotations.Nullable;

public interface HelixScreenOpenResult {

    int OPEN_SUCCESS = 0;
    int OPEN_NO_SCREEN = 1;
    int OPEN_NO_PAGE = 2;
    int OPEN_NO_REGISTRY = 3;

    int state();

    default boolean success() {
        return state() == OPEN_SUCCESS;
    }

    @Nullable
    HelixPageComponent component(String id);

}
