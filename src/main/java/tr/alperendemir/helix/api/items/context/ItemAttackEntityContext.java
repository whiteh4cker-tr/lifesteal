package tr.alperendemir.helix.api.items.context;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ItemAttackEntityContext extends ItemUseContext {

    private final Entity attacked;

    public ItemAttackEntityContext(Player whoUsed, Entity attacked) {
        super(whoUsed, ItemUseHand.MAIN_HAND);
        this.attacked = attacked;
    }

    public Entity attacked() {
        return attacked;
    }
}
