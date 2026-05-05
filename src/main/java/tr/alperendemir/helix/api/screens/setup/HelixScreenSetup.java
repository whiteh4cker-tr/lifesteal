package tr.alperendemir.helix.api.screens.setup;

import tr.alperendemir.helix.api.screens.pages.HelixPage;
import tr.alperendemir.helix.api.screens.ScreenDimensions;

public interface HelixScreenSetup {

    HelixPage createPage(String id, String name, ScreenDimensions dimensions);

}
