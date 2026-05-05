package tr.alperendemir.helix.api.items.context;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class ItemUseOnBlockContext extends ItemUseContext {

    private final Block clickedBlock;
    private final BlockFace clickedFace;

    public ItemUseOnBlockContext(Player whoUsed, ItemUseHand hand, Block clickedBlock, BlockFace clickedFace) {
        super(whoUsed, hand);
        this.clickedBlock = clickedBlock;
        this.clickedFace = clickedFace;
    }

    public Block clickedBlock() {
        return clickedBlock;
    }

    public BlockFace clickedFace() {
        return clickedFace;
    }
}
