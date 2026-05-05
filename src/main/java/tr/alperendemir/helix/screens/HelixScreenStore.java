package tr.alperendemir.helix.screens;

import tr.alperendemir.helix.api.screens.HelixScreen;
import tr.alperendemir.helix.api.screens.ScreenDimensions;
import tr.alperendemir.helix.api.screens.pages.HelixPage;
import tr.alperendemir.helix.api.screens.setup.HelixScreenSetup;
import tr.alperendemir.helix.screens.page.HelixScreenPage;

import java.util.HashMap;
import java.util.Map;

public class HelixScreenStore implements HelixScreenSetup {

    private final Map<String, HelixPage> pages = new HashMap<>();
    private final HelixScreen screen;
    private String firstPage = null;

    public HelixScreenStore(HelixScreen screen) {
        this.screen = screen;
    }

    public HelixScreen getScreen() {
        return this.screen;
    }

    @Override
    public HelixPage createPage(String id, String name, ScreenDimensions dimensions) {
        var page = new HelixScreenPage(name, dimensions);
        this.pages.put(id, page);

        if (this.firstPage == null) {
            this.firstPage = id;
        }

        return page;
    }

    public HelixPage getPage(String id) {
        if (id == null) {
            return this.pages.get(this.firstPage);
        }

        return this.pages.get(id);
    }
}
