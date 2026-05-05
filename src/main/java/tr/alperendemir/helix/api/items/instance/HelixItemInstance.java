package tr.alperendemir.helix.api.items.instance;

import tr.alperendemir.helix.api.items.HelixItem;
import tr.alperendemir.helix.api.items.variables.HelixItemVariables;
import tr.alperendemir.helix.api.plugins.HelixPlugin;
import org.bukkit.inventory.ItemStack;

public abstract class HelixItemInstance {

    private final HelixPlugin plugin;
    private final HelixItem item;
    private final ItemStack stack;

    protected HelixItemInstance(HelixPlugin plugin, HelixItem item, ItemStack stack) {
        this.plugin = plugin;
        this.item = item;
        this.stack = stack;
    }

    public HelixPlugin getPlugin() {
        return plugin;
    }

    public HelixItem getItem() {
        return item;
    }

    public ItemStack getStack() {
        return stack;
    }

    /**
     * Will attempt to remove as many stacks as requested, returns the amount of removed stacks.
     *
     * @param amount The amount of stacks to remove.
     * @return The amount of removed stacks, may be lower than requested.
     * */
    public int consume(int amount) {
        if (this.stack.getAmount() < amount) {
            var oldAmount = this.stack.getAmount();
            this.stack.setAmount(0);
            return oldAmount;
        }

        var currentAmount = this.stack.getAmount() - amount;
        this.stack.setAmount(currentAmount);
        return amount;
    }

    public abstract HelixItemVariables getVariables();
}
