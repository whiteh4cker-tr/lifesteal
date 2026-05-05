package tr.alperendemir.helix.api.items.display;

import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

public record ItemDisplayData(Material material, int customModelData, ItemTextDisplayData textDisplayData) implements ItemDisplayHolder {
    public ItemDisplayData(Material material) {
        this(material, 0, null);
    }

    public ItemDisplayData(Material material, int customModelData) {
        this(material, customModelData, null);
    }

    public ItemDisplayData(Material material, ItemTextDisplayData textDisplayData) {
        this(material, 0, textDisplayData);
    }

    @Override
    public void apply(ItemMeta meta) {
        meta.setCustomModelData(this.customModelData);

        if (this.textDisplayData != null) {
            this.textDisplayData.apply(meta);
        }
    }
}
