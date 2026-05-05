package tr.alperendemir.helix.api.screens.components;

import tr.alperendemir.helix.api.screens.SlotPosition;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HelixComponentContext {

    /**
     * @param position The position of the item, relative to the component.
     * @param stack The actual item to set.
     * */
    void setItem(SlotPosition position, ItemStack stack);

    /**
     * @param position The position of the item, relative to the component.
     * @return The item at the position, if and only if it is within bounds, otherwise returns null.
     * */
    @Nullable
    ItemStack getItem(SlotPosition position);

}
