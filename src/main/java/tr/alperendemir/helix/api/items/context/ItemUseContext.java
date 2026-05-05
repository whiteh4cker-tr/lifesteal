package tr.alperendemir.helix.api.items.context;

import org.bukkit.entity.Player;

public class ItemUseContext {

    private final Player whoUsed;
    private final ItemUseHand hand;

    public ItemUseContext(final Player whoUsed, ItemUseHand hand) {
        this.whoUsed = whoUsed;
        this.hand = hand;
    }

    public ItemUseHand hand() {
        return this.hand;
    }

    public Player player() {
        return this.whoUsed;
    }

}
