package tr.alperendemir.helix.tags;

import tr.alperendemir.helix.api.storage.UnknownVariableException;
import tr.alperendemir.helix.storage.BukkitHelixDataStorage;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BukkitHelixTagStorage extends BukkitHelixDataStorage {

    private final Map<String, Object> values = new HashMap<>();
    private final Set<String> keys = Collections.unmodifiableSet(this.values.keySet());

    private boolean edited = false;

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public boolean isEdited() {
        return this.edited;
    }

    public void fromBytes(InputStream stream) throws IOException {
        try(var din = new DataInputStream(stream)) {
            while (din.available() > 0) {
                var key = din.readUTF();
                var type = din.readByte();

                var value = switch (type) {
                    case 0 -> din.readByte();
                    case 1 -> din.readShort();
                    case 2 -> din.readInt();
                    case 3 -> din.readLong();
                    case 4 -> din.readFloat();
                    case 5 -> din.readDouble();
                    case 6 -> din.readUTF();
                    case 7 -> din.readBoolean();
                    case 8 -> {
                        var len = din.readInt();
                        yield din.readNBytes(len);
                    }
                    case 9 -> {
                        var len = din.readInt();
                        var intArr = new int[len];
                        for (int i = 0; i < intArr.length; i++) {
                            intArr[i] = din.readInt();
                        }
                        yield intArr;
                    }
                    case 10 -> {
                        var len = din.readInt();
                        var longArr = new long[len];
                        for (int i = 0; i < longArr.length; i++) {
                            longArr[i] = din.readLong();
                        }
                        yield longArr;
                    }
                    default -> throw new IllegalStateException("Corrupted stream");
                };

                this.set(key, value);
            }
        }
    }

    public void toBytes(OutputStream stream) throws IOException {
        try(var dop = new DataOutputStream(stream)) {
            for (var entry : this.values.entrySet()) {
                dop.writeUTF(entry.getKey());

                var type = entry.getValue().getClass();

                if (type == Byte.class) {
                    dop.writeByte(0);
                    dop.writeByte((Byte) entry.getValue());
                }

                if (type == Short.class) {
                    dop.writeByte(1);
                    dop.writeShort((Short) entry.getValue());
                }

                if (type == Integer.class) {
                    dop.writeByte(2);
                    dop.writeInt((Integer) entry.getValue());
                }

                if (type == Long.class) {
                    dop.writeByte(3);
                    dop.writeLong((Long) entry.getValue());
                }

                if (type == Float.class) {
                    dop.writeByte(4);
                    dop.writeFloat((Float) entry.getValue());
                }

                if (type == Double.class) {
                    dop.writeByte(5);
                    dop.writeDouble((Double) entry.getValue());
                }

                if (type == String.class) {
                    dop.writeByte(6);
                    dop.writeUTF((String) entry.getValue());
                }

                if (type == Boolean.class) {
                    dop.writeByte(7);
                    dop.writeBoolean((Boolean) entry.getValue());
                }

                if (type.isArray() && (type.componentType() == Byte.class || type.componentType() == byte.class)) {
                    dop.writeByte(8);

                    var len = Array.getLength(entry.getValue());

                    dop.writeInt(len);

                    for (int i = 0; i < len; i++) {
                        dop.writeInt(Array.getInt(entry.getValue(), i));
                    }
                }

                if (type.isArray() && (type.componentType() == Integer.class || type.componentType() == int.class)) {
                    dop.writeByte(9);

                    var len = Array.getLength(entry.getValue());

                    dop.writeInt(len);

                    for (int i = 0; i < len; i++) {
                        dop.writeInt(Array.getInt(entry.getValue(), i));
                    }
                }

                if (type.isArray() && (type.componentType() == Long.class || type.componentType() == long.class)) {
                    dop.writeByte(10);

                    var len = Array.getLength(entry.getValue());

                    dop.writeInt(len);

                    for (int i = 0; i < len; i++) {
                        dop.writeLong(Array.getLong(entry.getValue(), i));
                    }
                }
            }
        }
    }

    @Override
    public byte getByte(String key) throws UnknownVariableException {
        return this.get(null, key, Byte.class);
    }

    @Override
    public byte getByte(String key, byte defaultValue) {
        return this.get(defaultValue, key, Byte.class);
    }

    @Override
    public void setByte(String key, byte value) {
        this.set(key, value);
    }

    @Override
    public short getShort(String key) throws UnknownVariableException {
        return this.get(null, key, Short.class);
    }

    @Override
    public short getShort(String key, short defaultValue) {
        return this.get(defaultValue, key, Short.class);
    }

    @Override
    public void setShort(String key, short value) {
        this.set(key, value);
    }

    @Override
    public int getInt(String key) throws UnknownVariableException {
        return this.get(null, key, Integer.class);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return this.get(defaultValue, key, Integer.class);
    }

    @Override
    public void setInt(String key, int value) {
        this.set(key, value);
    }

    @Override
    public long getLong(String key) throws UnknownVariableException {
        return this.get(null, key, Long.class);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return this.get(defaultValue, key, Long.class);
    }

    @Override
    public void setLong(String key, long value) {
        this.set(key, value);
    }

    @Override
    public float getFloat(String key) throws UnknownVariableException {
        return this.get(null, key, Float.class);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return this.get(defaultValue, key, Float.class);
    }

    @Override
    public void setFloat(String key, float value) {
        this.set(key, value);
    }

    @Override
    public double getDouble(String key) throws UnknownVariableException {
        return this.get(null, key, Double.class);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return this.get(defaultValue, key, Double.class);
    }

    @Override
    public void setDouble(String key, double value) {
        this.set(key, value);
    }

    @Override
    public String getString(String key) throws UnknownVariableException {
        return this.get(null, key, String.class);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return this.get(defaultValue, key, String.class);
    }

    @Override
    public void setString(String key, String value) {
        this.set(key, value);
    }

    @Override
    public boolean getBoolean(String key) throws UnknownVariableException {
        return this.get(null, key, Boolean.class);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return this.get(defaultValue, key, Boolean.class);
    }

    @Override
    public void setBoolean(String key, boolean value) {
        this.set(key, value);
    }

    @Override
    public byte[] getByteArray(String key) throws UnknownVariableException {
        return this.get(null, key, byte[].class);
    }

    @Override
    public byte[] getByteArray(String key, byte[] defaultValue) {
        return this.get(defaultValue, key, byte[].class);
    }

    @Override
    public void setByteArray(String key, byte[] value) {
        this.set(key, value);
    }

    @Override
    public int[] getIntArray(String key) throws UnknownVariableException {
        return this.get(null, key, int[].class);
    }

    @Override
    public int[] getIntArray(String key, int[] defaultValue) {
        return this.get(defaultValue, key, int[].class);
    }

    @Override
    public void setIntArray(String key, int[] value) {
        this.set(key, value);
    }

    @Override
    public long[] getLongArray(String key) throws UnknownVariableException {
        return this.get(null, key, long[].class);
    }

    @Override
    public long[] getLongArray(String key, long[] defaultValue) {
        return this.get(defaultValue, key, long[].class);
    }

    @Override
    public void setLongArray(String key, long[] value) {
        this.set(key, value);
    }

    @Override
    public void clear() {
        if (this.frozen()) throw new IllegalStateException("Cannot clear frozen data storage!");
        this.values.clear();
    }

    @Override
    public Set<String> getKeys() {
        return this.keys;
    }

    @Override
    protected <T, Z> void set(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {

    }

    @Override
    protected void save() {

    }

    private <T> T get(Object def, String key, Class<T> type) {
        var value = this.values.getOrDefault(key, def);
        this.checkValue(value, key, type);

        return type.cast(value);
    }

    private void checkValue(Object obj, String key, Class<?> type) {
        if (obj != null) {
            if (!type.isAssignableFrom(obj.getClass())) {
                throw new IllegalArgumentException("Unsupported type: " + obj.getClass() + " for key: " + key);
            }

            return;
        }

        throw new UnknownVariableException(key, type);
    }

    private void set(String key, Object value) {
        if (this.frozen()) throw new IllegalStateException("Cannot edit frozen data storage!");

        this.values.put(key, value);
        this.edited = true;
    }
}
