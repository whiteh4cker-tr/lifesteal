package tr.alperendemir.helix.api.screens.pages;

import tr.alperendemir.helix.api.screens.ScreenDimensions;
import tr.alperendemir.helix.api.screens.components.HelixComponentHandler;
import tr.alperendemir.helix.api.screens.components.HelixPageComponent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface HelixPage {

    ScreenDimensions dimensions();

    /**
     * Adds and registers a component to this page.
     *
     * @param name The unique name of the component
     * @param handler The handler that will handle the component
     * @return Null if a component already exists with this name, otherwise it returns the wrapped component.
     * */
    @Nullable
    HelixPageComponent addComponent(String name, HelixComponentHandler handler);

    @Nullable
    HelixPageComponent getComponent(String name);

    /**
     * Sets the background of the page, used where there are no components to display.
     *
     * @param background The background to use, or null to unset.
     * */
    void setBackground(@Nullable ItemStack background);

}
