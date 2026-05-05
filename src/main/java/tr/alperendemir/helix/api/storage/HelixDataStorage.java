package tr.alperendemir.helix.api.storage;

import java.util.Set;

public interface HelixDataStorage {

    byte getByte(String key) throws UnknownVariableException;
    byte getByte(String key, byte defaultValue);
    void setByte(String key, byte value);

    short getShort(String key) throws UnknownVariableException;
    short getShort(String key, short defaultValue);
    void setShort(String key, short value);

    int getInt(String key) throws UnknownVariableException;
    int getInt(String key, int defaultValue);
    void setInt(String key, int value);

    long getLong(String key) throws UnknownVariableException;
    long getLong(String key, long defaultValue);
    void setLong(String key, long value);

    float getFloat(String key) throws UnknownVariableException;
    float getFloat(String key, float defaultValue);
    void setFloat(String key, float value);

    double getDouble(String key) throws UnknownVariableException;
    double getDouble(String key, double defaultValue);
    void setDouble(String key, double value);

    String getString(String key) throws UnknownVariableException;
    String getString(String key, String defaultValue);
    void setString(String key, String value);

    boolean getBoolean(String key) throws UnknownVariableException;
    boolean getBoolean(String key, boolean defaultValue);
    void setBoolean(String key, boolean value);

    byte[] getByteArray(String key) throws UnknownVariableException;
    byte[] getByteArray(String key, byte[] defaultValue);
    void setByteArray(String key, byte[] value);

    int[] getIntArray(String key) throws UnknownVariableException;
    int[] getIntArray(String key, int[] defaultValue);
    void setIntArray(String key, int[] value);

    long[] getLongArray(String key) throws UnknownVariableException;
    long[] getLongArray(String key, long[] defaultValue);
    void setLongArray(String key, long[] value);

    boolean frozen();

    void clear();

    Set<String> getKeys();

    boolean has(String key);

}
