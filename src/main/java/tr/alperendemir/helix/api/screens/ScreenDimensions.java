package tr.alperendemir.helix.api.screens;

import org.bukkit.event.inventory.InventoryType;

public record ScreenDimensions(InventoryType type, int width, int height, int size) {

    public static ScreenDimensions chest(int height) {
        return new ScreenDimensions(InventoryType.CHEST, 9, height, 9 * height);
    }

}
