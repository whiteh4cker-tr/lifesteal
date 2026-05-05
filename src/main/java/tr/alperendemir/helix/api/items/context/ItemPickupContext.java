package tr.alperendemir.helix.api.items.context;

import org.bukkit.entity.Player;
import org.bukkit.entity.Item;

public class ItemPickupContext extends ItemUseContext {

    private final Item itemEntity;
    private final int remaining;

    public ItemPickupContext(Player whoUsed, Item itemEntity, int remaining) {
        super(whoUsed, null);
        this.itemEntity = itemEntity;
        this.remaining = remaining;
    }

    public Item item() {
        return itemEntity;
    }

    public int remaining() {
        return remaining;
    }
}
