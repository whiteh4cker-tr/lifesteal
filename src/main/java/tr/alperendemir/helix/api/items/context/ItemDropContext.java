package tr.alperendemir.helix.api.items.context;

import org.bukkit.entity.Player;
import org.bukkit.entity.Item;

public class ItemDropContext extends ItemUseContext {

    private final Item itemEntity;

    public ItemDropContext(Player whoUsed, Item itemEntity) {
        super(whoUsed, ItemUseHand.MAIN_HAND);
        this.itemEntity = itemEntity;
    }

    public Item item() {
        return itemEntity;
    }
}
