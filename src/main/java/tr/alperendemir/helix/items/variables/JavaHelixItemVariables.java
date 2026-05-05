package tr.alperendemir.helix.items.variables;

import tr.alperendemir.helix.api.items.variables.HelixItemVariables;
import tr.alperendemir.helix.api.storage.UnknownVariableException;
import tr.alperendemir.helix.api.messages.colors.MinecraftColor;
import tr.alperendemir.helix.api.plugins.HelixPlugin;
import tr.alperendemir.helix.storage.BukkitHelixDataStorage;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class JavaHelixItemVariables extends BukkitHelixDataStorage implements HelixItemVariables {

    private static final PersistentDataType<?, ?>[] DATA_TYPES = new PersistentDataType[] {
            PersistentDataType.BYTE,
            PersistentDataType.SHORT,
            PersistentDataType.INTEGER,
            PersistentDataType.LONG,
            PersistentDataType.FLOAT,
            PersistentDataType.DOUBLE,
            PersistentDataType.STRING,
            PersistentDataType.BYTE_ARRAY,
            PersistentDataType.INTEGER_ARRAY,
            PersistentDataType.LONG_ARRAY
    };

    private final JavaHelixItemVariables parent;
    private final HelixPlugin plugin;
    private final NamespacedKey childKey;
    private final ItemStack stack;
    private final ItemMeta meta; // We want to save the item meta again, I guess.
    private final PersistentDataContainer container;

    private final Map<String, ItemValue<?, ?>> values = new HashMap<>();
    private final Set<String> keyView = Collections.unmodifiableSet(this.values.keySet());

    private boolean autoSave = true;

    public JavaHelixItemVariables(NamespacedKey variableKey, HelixPlugin plugin, final ItemStack stack) {
        this.parent = null;
        this.plugin = plugin;
        this.stack = stack;
        this.meta = stack.getItemMeta();
        if (this.meta == null) {
            throw new IllegalStateException();
        }

        this.container = this.meta.getPersistentDataContainer();
        this.childKey = variableKey;

        this.setPlugin(this.plugin.getBukkitPlugin());
        this.setKeys(this.keyView);
        this.setContainer(this.createChildContainer());

        this.initCache();

        this.saveLore();
    }

    JavaHelixItemVariables(JavaHelixItemVariables parent, NamespacedKey variableKey, HelixPlugin plugin, ItemStack stack, ItemMeta meta, final PersistentDataContainer container) {
        this.parent = parent;
        this.plugin = plugin;
        this.stack = stack;
        this.meta = meta;
        this.container = container;
        this.childKey = variableKey;

        this.setPlugin(this.plugin.getBukkitPlugin());
        this.setKeys(this.keyView);
        this.setContainer(this.createChildContainer());
    }

    private void saveLore() {
        if (this.getKeys().contains("___lore___")) return;

        this.setString("___LORE___", String.join("\n", Objects.requireNonNull(this.meta.getLore())));
    }

    private Stream<String> loadLore() {
        try {
            return Arrays.stream(this.getString("___LORE___").split("\n"));
        } catch (UnknownVariableException e) {
            throw new RuntimeException(e);
        }
    }

    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }

    public void forceSave() {
        if (!this.autoSave) {
            this.autoSave = true;
            this.save();
            this.autoSave = false;
            return;
        }

        this.save();
    }

    @Override
    public HelixItemVariables getOrCreateSection(String key) {
        return new JavaHelixItemVariables(this, this.makeKey(key), this.plugin, this.stack, this.meta, this.getContainer());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, Z> PersistentDataType<T, Z> getType(String key) {
        return (PersistentDataType<T, Z>) this.values.get(key).type();
    }

    @SuppressWarnings("unchecked")
    public <T, Z> ItemValue<T, Z> getValueInternal(String key) {
        return (ItemValue<T, Z>) this.values.get(key);
    }

    @Override
    public  <T, Z> void set(String key, PersistentDataType<T, Z> type, Z value) {
        var found = false;
        for (var t : DATA_TYPES) {
            if (type != t) continue;

            found = true;
            break;
        }

        if (!found) return;

        this.set(makeKey(key), type, value);
        this.save();
    }

    @Override
    public <Z> Z getValue(String key) {
        var value = this.<Object, Z>getValueInternal(key);
        if (value == null) {
            return null;
        }

        return value.value();
    }

    public record ItemValue<T, Z>(Z value, PersistentDataType<T, Z> type) {}

    private void initCache() {
        var keys = this.getContainer().getKeys();

        for (var key : keys) {
            for (var type : DATA_TYPES) {
                if (this.getContainer().has(key, type)) {
                    this.cache(key, this.getContainer().get(key, type), type);
                    break;
                }
            }
        }
    }

    @Override
    protected <T, Z> void set(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        this.getContainer().set(key, type, value);
        this.cache(key, value, type);
    }

    @SuppressWarnings("unchecked")
    private <T, Z> void cache(NamespacedKey key, Object value, PersistentDataType<?, ?> type) {
        this.values.put(key.getKey(), new ItemValue<>((Z) value, (PersistentDataType<T, Z>) type));
    }

    @Override
    protected void save() {
        if (!this.autoSave) return;

        this.container.set(this.childKey, PersistentDataType.TAG_CONTAINER, this.getContainer());

        if (this.parent != null) {
            this.parent.save();
            return;
        }

        var lore = this.loadLore().map(s -> {
            var colored = MinecraftColor.replaceColorCodes('&', s);
            var keys = this.getKeys();
            for (var variableKey : keys) {
                colored = colored.replace("{{" + variableKey + "}}", String.valueOf(this.<Object>getValue(variableKey)));
            }

            return colored;
        }).toList();

        this.meta.setLore(lore);

        this.stack.setItemMeta(this.meta);
    }

    private PersistentDataContainer createChildContainer() {
        if (this.container.has(this.childKey, PersistentDataType.TAG_CONTAINER)) {
            return this.container.get(this.childKey, PersistentDataType.TAG_CONTAINER);
        }

        return this.container.getAdapterContext().newPersistentDataContainer();
    }
}
