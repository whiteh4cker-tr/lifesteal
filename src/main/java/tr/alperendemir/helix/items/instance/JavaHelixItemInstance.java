package tr.alperendemir.helix.items.instance;

import tr.alperendemir.helix.api.items.HelixItem;
import tr.alperendemir.helix.api.items.instance.HelixItemInstance;
import tr.alperendemir.helix.api.items.variables.HelixItemVariables;
import tr.alperendemir.helix.api.plugins.HelixPlugin;
import tr.alperendemir.helix.items.variables.JavaHelixItemVariables;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class JavaHelixItemInstance extends HelixItemInstance {
    private final NamespacedKey variableKey;
    private HelixItemVariables variables;

    public JavaHelixItemInstance(HelixPlugin plugin, HelixItem item, ItemStack stack, NamespacedKey variableKey) {
        super(plugin, item, stack);
        this.variableKey = variableKey;
    }

    @Override
    public HelixItemVariables getVariables() {
        if (this.variables == null) {
            this.variables = new JavaHelixItemVariables(this.variableKey, this.getPlugin(), this.getStack());
        }

        return this.variables;
    }
}
