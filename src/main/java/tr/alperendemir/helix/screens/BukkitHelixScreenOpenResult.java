package tr.alperendemir.helix.screens;

import tr.alperendemir.helix.api.screens.HelixScreenOpenResult;
import tr.alperendemir.helix.api.screens.components.HelixPageComponent;
import tr.alperendemir.helix.screens.page.HelixScreenPage;
import org.jetbrains.annotations.Nullable;

public class BukkitHelixScreenOpenResult implements HelixScreenOpenResult {

    private final int state;
    private final HelixScreenPage page;

    public BukkitHelixScreenOpenResult(int state, HelixScreenPage page) {
        this.state = state;
        this.page = page;
    }

    @Override
    public int state() {
        return this.state;
    }

    @Override
    public @Nullable HelixPageComponent component(String id) {
        return this.page.getComponent(id);
    }

    @Override
    public String toString() {
        return "BukkitHelixScreenOpenResult[" +
                "page=" + page +
                ", state=" + state +
                ']';
    }
}
