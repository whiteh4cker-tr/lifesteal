package tr.alperendemir.helix.api.items.context;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ItemUseOnEntityContext extends ItemUseContext {

    private final Entity clicked;
    private final Vector interactPosition;

    public ItemUseOnEntityContext(Player whoUsed, ItemUseHand hand, Entity clicked, Vector interactPosition) {
        super(whoUsed, hand);
        this.clicked = clicked;
        this.interactPosition = interactPosition;
    }

    public Entity clickedEntity() {
        return clicked;
    }

    public Vector clickedPosition() {
        return interactPosition.clone();
    }
}
