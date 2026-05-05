package tr.alperendemir.helix.storage;

import tr.alperendemir.helix.api.storage.HelixDataStorage;
import tr.alperendemir.helix.api.storage.UnknownVariableException;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public abstract class BukkitHelixDataStorage implements HelixDataStorage {

    private PersistentDataContainer container;
    private Plugin plugin;
    private Set<String> keys;
    private boolean frozen;

    protected void setContainer(PersistentDataContainer container) {
        this.container = container;
    }

    protected void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    protected void setKeys(Set<String> keys) {
        this.keys = keys;
    }

    public PersistentDataContainer getContainer() {
        return this.container;
    }

    public void unfreeze() {
        this.frozen = false;
    }

    public void freeze() {
        this.frozen = true;
    }

    @Override
    public boolean frozen() {
        return this.frozen;
    }

    @Override
    public boolean has(String key) {
        return this.keys.contains(key);
    }

    @Override
    public byte getByte(String key) throws UnknownVariableException {
        var namespaced = this.makeKey(key);
        this.checkExists(namespaced, PersistentDataType.BYTE);

        return this.container.getOrDefault(namespaced, PersistentDataType.BYTE, (byte) 0);
    }

    @Override
    public byte getByte(String key, byte defaultValue) {
        var namespaced = this.makeKey(key);

        return this.container.getOrDefault(namespaced, PersistentDataType.BYTE, defaultValue);
    }

    @Override
    public void setByte(String key, byte value) {
        var namespaced = this.makeKey(key);

        this.setInternal(namespaced, PersistentDataType.BYTE, value);
        this.save();
    }

    @Override
    public short getShort(String key) throws UnknownVariableException {
        var namespaced = this.makeKey(key);
        this.checkExists(namespaced, PersistentDataType.SHORT);

        return this.container.getOrDefault(namespaced, PersistentDataType.SHORT, (short) 0);
    }

    @Override
    public short getShort(String key, short defaultValue) {
        var namespaced = this.makeKey(key);

        return this.container.getOrDefault(namespaced, PersistentDataType.SHORT, defaultValue);
    }

    @Override
    public void setShort(String key, short value) {
        var namespaced = this.makeKey(key);

        this.setInternal(namespaced, PersistentDataType.SHORT, value);
        this.save();
    }

    @Override
    public int getInt(String key) throws UnknownVariableException {
        var namespaced = this.makeKey(key);
        this.checkExists(namespaced, PersistentDataType.INTEGER);

        return this.container.getOrDefault(namespaced, PersistentDataType.INTEGER, 0);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        var namespaced = this.makeKey(key);

        return this.container.getOrDefault(namespaced, PersistentDataType.INTEGER, defaultValue);
    }

    @Override
    public void setInt(String key, int value) {
        var namespaced = this.makeKey(key);

        this.setInternal(namespaced, PersistentDataType.INTEGER, value);
        this.save();
    }

    @Override
    public long getLong(String key) throws UnknownVariableException {
        var namespaced = this.makeKey(key);
        this.checkExists(namespaced, PersistentDataType.LONG);

        return this.container.getOrDefault(namespaced, PersistentDataType.LONG, 0L);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        var namespaced = this.makeKey(key);

        return this.container.getOrDefault(namespaced, PersistentDataType.LONG, defaultValue);
    }

    @Override
    public void setLong(String key, long value) {
        var namespaced = this.makeKey(key);

        this.setInternal(namespaced, PersistentDataType.LONG, value);
        this.save();
    }

    @Override
    public float getFloat(String key) throws UnknownVariableException {
        var namespaced = this.makeKey(key);
        this.checkExists(namespaced, PersistentDataType.FLOAT);

        return this.container.getOrDefault(namespaced, PersistentDataType.FLOAT, 0F);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        var namespaced = this.makeKey(key);

        return this.container.getOrDefault(namespaced, PersistentDataType.FLOAT, defaultValue);
    }

    @Override
    public void setFloat(String key, float value) {
        var namespaced = this.makeKey(key);

        this.setInternal(namespaced, PersistentDataType.FLOAT, value);
        this.save();
    }

    @Override
    public double getDouble(String key) throws UnknownVariableException {
        var namespaced = this.makeKey(key);
        this.checkExists(namespaced, PersistentDataType.DOUBLE);

        return this.container.getOrDefault(namespaced, PersistentDataType.DOUBLE, 0D);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        var namespaced = this.makeKey(key);

        return this.container.getOrDefault(namespaced, PersistentDataType.DOUBLE, defaultValue);
    }

    @Override
    public void setDouble(String key, double value) {
        var namespaced = this.makeKey(key);

        this.setInternal(namespaced, PersistentDataType.DOUBLE, value);
        this.save();
    }

    @Override
    public String getString(String key) throws UnknownVariableException {
        var namespaced = this.makeKey(key);
        this.checkExists(namespaced, PersistentDataType.STRING);

        return this.container.getOrDefault(namespaced, PersistentDataType.STRING, "");
    }

    @Override
    public String getString(String key, String defaultValue) {
        var namespaced = this.makeKey(key);

        return this.container.getOrDefault(namespaced, PersistentDataType.STRING, defaultValue);
    }

    @Override
    public void setString(String key, String value) {
        var namespaced = this.makeKey(key);

        this.setInternal(namespaced, PersistentDataType.STRING, value);
        this.save();
    }

    @Override
    public boolean getBoolean(String key) throws UnknownVariableException {
        return this.getByte(key) == 1;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return this.getByte(key, (byte) (defaultValue ? 1 : 0)) == 1;
    }

    @Override
    public void setBoolean(String key, boolean value) {
        this.setByte(key, (byte) (value ? 1 : 0));
    }

    @Override
    public byte[] getByteArray(String key) throws UnknownVariableException {
        var namespaced = this.makeKey(key);
        this.checkExists(namespaced, PersistentDataType.BYTE_ARRAY);

        return this.container.getOrDefault(namespaced, PersistentDataType.BYTE_ARRAY, new byte[0]);
    }

    @Override
    public byte[] getByteArray(String key, byte[] defaultValue) {
        var namespaced = this.makeKey(key);

        return this.container.getOrDefault(namespaced, PersistentDataType.BYTE_ARRAY, defaultValue);
    }

    @Override
    public void setByteArray(String key, byte[] value) {
        var namespaced = this.makeKey(key);

        this.setInternal(namespaced, PersistentDataType.BYTE_ARRAY, value);
        this.save();
    }

    @Override
    public int[] getIntArray(String key) throws UnknownVariableException {
        var namespaced = this.makeKey(key);
        this.checkExists(namespaced, PersistentDataType.INTEGER_ARRAY);

        return this.container.getOrDefault(namespaced, PersistentDataType.INTEGER_ARRAY, new int[0]);
    }

    @Override
    public int[] getIntArray(String key, int[] defaultValue) {
        var namespaced = this.makeKey(key);

        return this.container.getOrDefault(namespaced, PersistentDataType.INTEGER_ARRAY, defaultValue);
    }

    @Override
    public void setIntArray(String key, int[] value) {
        var namespaced = this.makeKey(key);

        this.setInternal(namespaced, PersistentDataType.INTEGER_ARRAY, value);
        this.save();
    }

    @Override
    public long[] getLongArray(String key) throws UnknownVariableException {
        var namespaced = this.makeKey(key);
        this.checkExists(namespaced, PersistentDataType.LONG_ARRAY);

        return this.container.getOrDefault(namespaced, PersistentDataType.LONG_ARRAY, new long[0]);
    }

    @Override
    public long[] getLongArray(String key, long[] defaultValue) {
        var namespaced = this.makeKey(key);

        return this.container.getOrDefault(namespaced, PersistentDataType.LONG_ARRAY, defaultValue);
    }

    @Override
    public void setLongArray(String key, long[] value) {
        var namespaced = this.makeKey(key);

        this.setInternal(namespaced, PersistentDataType.LONG_ARRAY, value);
        this.save();
    }

    @Override
    public void clear() {
        this.container.getKeys().forEach(this.container::remove);
        this.save();
    }

    @Override
    public Set<String> getKeys() {
        return this.keys;
    }

    protected abstract <T, Z> void set(NamespacedKey key, PersistentDataType<T, Z> type, Z value);

    protected abstract void save();

    protected final NamespacedKey makeKey(String key) {
        return new NamespacedKey(this.plugin, key);
    }

    private <T, Z> void setInternal(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        if (this.frozen) throw new IllegalStateException("Cannot edit frozen data storage!");

        this.set(key, type, value);
    }

    private void checkExists(NamespacedKey key, PersistentDataType<?, ?> type) throws UnknownVariableException {
        if (!this.container.has(key, type)) {
            var id = key.toString();
            var complex = type.getComplexType();
            var message = "Unknown variable '%s' of type '%s'".formatted(id, complex.getName());
            throw new UnknownVariableException(message, id, complex);
        }
    }
}
