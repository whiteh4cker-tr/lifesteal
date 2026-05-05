package tr.alperendemir.helix.api.config;

import tr.alperendemir.helix.api.config.components.arrays.CompoundArrayConfigComponent;
import tr.alperendemir.helix.api.config.components.arrays.ParsedArrayConfigComponent;
import tr.alperendemir.helix.api.config.components.maps.ParsedMapConfigComponent;
import tr.alperendemir.helix.api.config.components.values.CompoundConfigComponent;
import tr.alperendemir.helix.api.config.components.values.ParsedValueConfigComponent;
import tr.alperendemir.helix.api.config.errors.ConfigError;
import tr.alperendemir.helix.api.logging.HelixLogger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Map;

public interface Configuration {

    Configuration child(String key);

    <C> ParsedValueConfigComponent<?, C> value(String key);

    <C> ParsedArrayConfigComponent<?, C> valueArray(String key);

    <T> CompoundConfigComponent<T> compound(String key);

    <T> CompoundArrayConfigComponent<T> compoundArray(String key);

    <T> ParsedMapConfigComponent<String, T> map(String key);

    @SuppressWarnings("unchecked")
    default <C> C getValue(String key) {
        var value = this.value(key);
        try {
            return (C) value.value();
        } catch (ClassCastException exception) {
            var ex = new IllegalArgumentException("Invalid type for key '%s'".formatted(key), exception);
            HelixLogger.reportError(ex);
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    default <C> C[] getValueArray(String key) {
        var value = this.valueArray(key);
        try {
            return (C[]) value.values();
        } catch (ClassCastException exception) {
            var ex = new IllegalArgumentException("Invalid type for key '%s'".formatted(key), exception);
            HelixLogger.reportError(ex);
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    default <T> T getCompound(String key) {
        var value = this.compound(key);
        try {
            return (T) value.value();
        } catch (ClassCastException exception) {
            var ex = new IllegalArgumentException("Invalid type for key '%s'".formatted(key), exception);
            HelixLogger.reportError(ex);
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    default <T> T[] getCompoundArray(String key) {
        var value = this.compoundArray(key);
        try {
            return (T[]) value.values();
        } catch (ClassCastException exception) {
            var ex = new IllegalArgumentException("Invalid type for key '%s'".formatted(key), exception);
            HelixLogger.reportError(ex);
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    default <T> Map<String, T> getMap(String key) {
        var value = this.map(key);
        try {
            return (Map<String, T>) value.values();
        } catch (ClassCastException exception) {
            var ex = new IllegalArgumentException("Invalid type for key '%s'".formatted(key), exception);
            HelixLogger.reportError(ex);
            throw ex;
        }
    }

    void write(StringBuilder stringBuilder, int indent);

    default void write(StringBuilder stringBuilder) {
        write(stringBuilder, 0);
    }

    void write(OutputStream outputStream, int indent, boolean length) throws IOException;

    default void write(OutputStream outputStream, boolean length) throws IOException {
        write(outputStream, 0, length);
    }

    default void write(OutputStream outputStream, int indent) throws IOException {
        write(outputStream, indent, false);
    }

    void write(Path path, int indent) throws IOException;

    default void write(Path path) throws IOException {
        write(path, 0);
    }

    void write(File file, int indent) throws IOException;

    default void write(File file) throws IOException {
        write(file, 0);
    }

    void writeError(StringBuilder stringBuilder, ConfigError error, int indent);

    default void writeError(StringBuilder stringBuilder, ConfigError error) {
        this.writeError(stringBuilder, error, 0);
    }

    void writeError(OutputStream outputStream, ConfigError error, int indent, boolean length) throws IOException;

    default void writeError(OutputStream outputStream, ConfigError error, boolean length) throws IOException {
        this.writeError(outputStream, error, 0, length);
    }

    default void writeError(OutputStream outputStream, ConfigError error, int indent) throws IOException {
        this.writeError(outputStream, error, indent, false);
    }

    void writeError(Path path, ConfigError error, int indent) throws IOException;

    default void writeError(Path path, ConfigError error) throws IOException {
        this.writeError(path, error, 0);
    }

    void writeError(File file, ConfigError error, int indent) throws IOException;

    default void writeError(File file, ConfigError error) throws IOException {
        this.writeError(file, error, 0);
    }

    ConfigError read(Map<?, ?> errors);

    File defaultFile();

    void save();

    boolean load();

    boolean loadOrCreate();

}
