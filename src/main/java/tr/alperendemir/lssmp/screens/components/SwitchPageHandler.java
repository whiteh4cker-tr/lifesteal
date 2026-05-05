package tr.alperendemir.lssmp.screens.components;

import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.namespaced.UniqueIdentifier;
import tr.alperendemir.helix.api.screens.components.HelixComponentContext;
import tr.alperendemir.helix.api.screens.components.HelixComponentEvent;
import tr.alperendemir.helix.api.screens.components.HelixComponentHandler;
import tr.alperendemir.helix.api.screens.components.HelixPageComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SwitchPageHandler implements HelixComponentHandler {

    private final UniqueIdentifier screen;
    private final String page;

    public SwitchPageHandler(UniqueIdentifier screen, String page) {
        this.screen = screen;
        this.page = page;
    }

    @Override
    public void render(HelixComponentContext context, HelixPageComponent component) {
        context.setItem(component.position(0), new ItemStack(Material.GREEN_STAINED_GLASS_PANE));
    }

    @Override
    public void handleEvent(HelixComponentEvent event, HelixComponentContext context, HelixPageComponent component) {
        event.cancel();

        Helix.screens().open(event.player(), this.screen, this.page);
    }

    @Override
    public void close(HelixComponentContext context, HelixPageComponent component) {

    }
}
