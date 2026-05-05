package tr.alperendemir.helix.api.items;

import com.google.common.collect.LinkedHashMultimap;
import tr.alperendemir.helix.api.items.callbacks.ItemUseCallback;
import tr.alperendemir.helix.api.items.callbacks.ItemUseResult;
import tr.alperendemir.helix.api.items.context.ItemUseContext;
import tr.alperendemir.helix.api.items.display.ItemDisplayData;
import tr.alperendemir.helix.api.items.instance.HelixItemInstance;
import tr.alperendemir.helix.api.items.variables.HelixItemVariables;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;

public abstract class HelixItem {

    private final LinkedHashMultimap<ItemAction, ItemUseCallback<?>> callbacks = LinkedHashMultimap.create();

    public final <T extends ItemUseContext> void addCallback(ItemAction action, ItemUseCallback<T> callback) {
        this.callbacks.put(action, callback);

        // TODO ADD SOMETHING TO ADD A CALLBACK AS FIRST
    }

    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    public final <T extends ItemUseContext> ItemUseResult fireCallback(ItemAction action, T context, HelixItemInstance instance) {
        var actions = this.callbacks.get(action);
        if (actions == null) return ItemUseResult.PASS;

        var out = ItemUseResult.SUCCEED;
        var iterator = actions.iterator();
        while (iterator.hasNext()) {
            try {
                var callback = iterator.next();
                var casted = (ItemUseCallback<T>) callback;
                var result = casted.onItemUse(context, instance);

                if (casted.isRemoved()) {
                    iterator.remove();
                }

                if (!result.isSuccess()) {
                    return result;
                }

                out = result;
            } catch (ClassCastException ignored) {
                // Ignore
            }
        }

        return out;
    }

    public abstract void setupItemStack(final ItemStack stack, final HelixItemVariables variables);

    public abstract ItemDisplayData defaultDisplayData();
}
