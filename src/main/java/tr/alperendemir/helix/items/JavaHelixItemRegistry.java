package tr.alperendemir.helix.items;

import tr.alperendemir.helix.api.items.HelixItem;
import tr.alperendemir.helix.api.items.HelixItemRegistry;
import tr.alperendemir.helix.api.items.display.ItemDisplayData;
import tr.alperendemir.helix.api.items.instance.HelixItemInstance;
import tr.alperendemir.helix.api.namespaced.UniqueIdentifier;
import tr.alperendemir.helix.api.plugins.HelixPlugin;
import tr.alperendemir.helix.items.instance.JavaHelixItemInstance;
import tr.alperendemir.helix.items.variables.JavaHelixItemVariables;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JavaHelixItemRegistry implements HelixItemRegistry {

    private final Map<UniqueIdentifier, HelixItem> items = new HashMap<>();
    private final Map<HelixItem, UniqueIdentifier> itemToKey = new HashMap<>();

    private final Set<UniqueIdentifier> keySet = Collections.unmodifiableSet(this.items.keySet());
    private final Collection<HelixItem> itemList = Collections.unmodifiableCollection(this.items.values());

    private final NamespacedKey variableKey;
    private final NamespacedKey itemIdKey;

    public JavaHelixItemRegistry(UniqueIdentifier variableKey, UniqueIdentifier itemIdKey) {
        this.variableKey = NamespacedKey.fromString(variableKey.toString());
        this.itemIdKey = NamespacedKey.fromString(itemIdKey.toString());
    }

    @Override
    public boolean register(UniqueIdentifier id, HelixItem helixItem) {
        if (this.items.containsKey(id)) return false;

        this.items.put(id, helixItem);
        this.itemToKey.put(helixItem, id);

        return true;
    }

    @Override
    public void unregister(UniqueIdentifier id) {
        var item = this.items.remove(id);
        this.itemToKey.remove(item);
    }

    @Override
    public HelixItem getItem(UniqueIdentifier id) {
        return this.items.get(id);
    }

    @Override
    public UniqueIdentifier getKey(HelixItem instance) {
        return this.itemToKey.get(instance);
    }

    @Override
    public HelixItemInstance getItemFromStack(ItemStack stack) {
        if (stack == null) return null;

        var meta = stack.getItemMeta();
        if (meta == null) return null;

        var pdc = meta.getPersistentDataContainer();

        var stringId = pdc.getOrDefault(this.itemIdKey, PersistentDataType.STRING, "");
        if (stringId.isEmpty()) return null;

        var id = UniqueIdentifier.parse(stringId);
        var item = this.getItem(id);

        return createInstance(item, stack);
    }

    @Override
    public Set<HelixItem> getItems(Plugin plugin) {
        var id = plugin.getName().toLowerCase(Locale.ROOT);

        return this.items.entrySet().stream()
                .filter(entry -> entry.getKey().namespace().equals(id))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<UniqueIdentifier> getKeys(Plugin plugin) {
        var id = plugin.getName().toLowerCase(Locale.ROOT);

        return this.items.keySet().stream()
                .filter(helixItem -> helixItem.namespace().equals(id))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<UniqueIdentifier> getAllKeys() {
        return this.keySet;
    }

    @Override
    public Collection<HelixItem> getAllItems() {
        return this.itemList;
    }

    @Override
    public int removeFromPlayer(Player player, UniqueIdentifier id, int max) {
        var total = 0;
        for (var stack : player.getInventory().getContents()) {
            var res = handleRemove(stack, id, max);
            total += res;
            max -= res;

            if (max <= 0) {
                player.updateInventory();
                return total;
            }
        }

        for (var stack : player.getInventory().getArmorContents()) {
            var res = handleRemove(stack, id, max);
            total += res;
            max -= res;
            if (max <= total) {
                player.updateInventory();
                return total;
            }
        }

        return total;
    }

    private int handleRemove(ItemStack stack, UniqueIdentifier id, int max) {
        if (stack == null) return 0;

        var meta = stack.getItemMeta();
        if (meta == null) return 0;

        var pdc = meta.getPersistentDataContainer();
        var stringId = pdc.getOrDefault(this.itemIdKey, PersistentDataType.STRING, "");
        if (stringId.isEmpty()) return 0;

        var uniqueId = UniqueIdentifier.parse(stringId);

        if(!uniqueId.equals(id)) return 0;

        var toRemove = Math.min(stack.getAmount(), max);
        stack.setAmount(stack.getAmount() - toRemove);

        return toRemove;
    }

    @Override
    public ItemStack createItem(UniqueIdentifier id, @Nullable ItemDisplayData data) {
        var item = this.getItem(id);
        if (item == null) {
            return null;
        }

        if (data == null) {
            data = item.defaultDisplayData();
        }

        var plugin = id.namespaceAsPlugin();
        if (plugin == null) return null;

        var stack = new ItemStack(data.material());
        var meta = stack.getItemMeta();
        if (meta == null) return null;

        var pdc = meta.getPersistentDataContainer();
        pdc.set(this.itemIdKey, PersistentDataType.STRING, id.toString());

        data.apply(meta);

        stack.setItemMeta(meta);

        // (Sub-par) performance!
        var variables = new JavaHelixItemVariables(this.variableKey, plugin, stack);
        variables.setAutoSave(false);
        item.setupItemStack(stack, variables);
        variables.forceSave();

//        meta = stack.getItemMeta();
//
//        var lore = meta.getLore();
//        if (lore != null) {
//            lore = lore.stream().map(s -> {
//                var colored = MinecraftColor.replaceColorCodes('&', s);
//                var keys = variables.getKeys();
//                for (var variableKey : keys) {
//                    colored = colored.replace("{{" + variableKey + "}}", String.valueOf(variables.<Object>getValue(variableKey)));
//                }
//
//                return colored;
//            }).toList();
//
//            meta.setLore(lore);
//
//            stack.setItemMeta(meta);
//        }

        return stack;
    }

    public HelixItemInstance createInstance(HelixItem helixItem, ItemStack stack) {
        var plugin = HelixPlugin.getProvidingPlugin(helixItem.getClass());
        return new JavaHelixItemInstance(plugin, helixItem, stack, this.variableKey);
    }
}
