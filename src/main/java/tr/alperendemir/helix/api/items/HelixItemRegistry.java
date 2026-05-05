package tr.alperendemir.helix.api.items;

import tr.alperendemir.helix.api.items.display.ItemDisplayData;
import tr.alperendemir.helix.api.items.instance.HelixItemInstance;
import tr.alperendemir.helix.api.namespaced.UniqueIdentifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public interface HelixItemRegistry {

    boolean register(final UniqueIdentifier id, final HelixItem helixItem);
    void unregister(final UniqueIdentifier id);

    HelixItem getItem(final UniqueIdentifier id);
    UniqueIdentifier getKey(final HelixItem instance);

    HelixItemInstance getItemFromStack(final ItemStack stack);
    Set<HelixItem> getItems(final Plugin plugin);
    Set<UniqueIdentifier> getKeys(final Plugin plugin);

    Set<UniqueIdentifier> getAllKeys();
    Collection<HelixItem> getAllItems();

    int removeFromPlayer(final Player player, UniqueIdentifier id, int max);

    ItemStack createItem(final UniqueIdentifier id, @Nullable final ItemDisplayData itemDisplayData);


}
