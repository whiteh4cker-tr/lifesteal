package tr.alperendemir.helix.config.components.maps;

import tr.alperendemir.helix.api.config.ConfigEntry;
import tr.alperendemir.helix.api.config.components.maps.MapConfigComponent;
import tr.alperendemir.helix.api.config.errors.ConfigError;
import tr.alperendemir.helix.api.config.errors.MismatchedTypeConfigError;
import tr.alperendemir.helix.api.config.errors.MissingValueConfigError;
import tr.alperendemir.helix.api.config.errors.TreeConfigError;
import tr.alperendemir.helix.api.logging.HelixLogger;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class SimpleMapConfigComponent<T> implements MapConfigComponent<T> {

    private final ConfigEntry entry;
    private final String id;
    private final Class<T> type;
    private final Map<String, T> defaultValue;
    private final Map<String, T> values = new LinkedHashMap<>();

    public SimpleMapConfigComponent(ConfigEntry entry, String id, Class<T> type, Map<String, T> defaultValue) {
        this.entry = entry;
        this.id = id;
        this.type = type;
        this.defaultValue = Collections.unmodifiableMap(defaultValue);

        this.values.putAll(defaultValue);
    }

    @Override
    public Map<String, T> defaultValues() {
        return this.defaultValue;
    }

    @Override
    public Map<String, T> values() {
        return this.values;
    }

    @Override
    public Class<T> type() {
        return this.type;
    }

    @Override
    public @NotNull ConfigEntry entry() {
        return this.entry;
    }

    @Override
    public @NotNull String id() {
        return this.id;
    }

    @Override
    public void write(@NotNull final StringBuilder builder, int indent) {
        var space = "    ".repeat(indent);
        var childSpace = "    ".repeat(indent + 1);

        builder.append(space).append(this.id()).append(":\n");

        for (var entry : this.values.entrySet()) {
            builder.append(childSpace).append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
        }
    }

    @Override
    public void writeError(@NotNull StringBuilder builder, @NotNull ConfigError error, int indent) {
        this.writeError(builder, error, indent, String::valueOf);
    }

    @Override
    public ConfigError read(@NotNull final Map<?, ?> data) {
        var maps = data.get("value");
        if (!(maps instanceof Map<?, ?> map)) {
            return new MissingValueConfigError();
        }

        this.values.clear();

        var treeError = new TreeConfigError(new HashMap<>());

        for (var entry : map.entrySet()) {
            var key = String.valueOf(entry.getKey());
            var rawValue = entry.getValue();
            try {
                var value = convertValue(rawValue);

                this.values.put(key, value);
            } catch (ClassCastException exception) {
                treeError.childErrors().put(key, new MismatchedTypeConfigError(
                        this.defaultValues(),
                        entry.getValue()
                ));
            } catch (Throwable throwable) {
                HelixLogger.reportError(throwable);
            }
        }

        return treeError.childErrors().isEmpty() ? null : treeError;
    }

    void writeError(@NotNull StringBuilder builder, @NotNull ConfigError error, int indent, Function<T, String> writer) {
        var space = "    ".repeat(indent).substring(indent == 0 ? 0 : 1);
        var childSpace = "    ".repeat(indent + 1).substring(indent == 0 ? 0 : 1);

        if (error instanceof MissingValueConfigError) {
            builder.append("<light:green>+").append(space).append(this.id()).append(":\n");

            for (var entry : this.values.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
                builder.append("<light:green>+").append(childSpace).append(key).append(": ").append(writer.apply(value)).append('\n');
            }

            return;
        }

        builder.append(space).append(' ').append(this.id()).append(":\n");

        if (error instanceof TreeConfigError treeConfigError) {
            for (var entry : this.values.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();

                var childError = treeConfigError.childErrors().get(key);
                if (childError instanceof MismatchedTypeConfigError mismatch) {
                    builder.append(childSpace).append(' ').append(mismatch.getMessage()).append('\n');
                    builder.append(childSpace).append(' ').append("Replace:\n<light:red>-");
                    builder.append(childSpace).append(key).append(": ").append(mismatch.received()).append('\n');
                    builder.append(childSpace).append(' ').append("With:\n<light:green>+   ");
                    builder.append(childSpace).append(key).append(": ").append(writer.apply(value)).append('\n');
                    continue;
                }

                builder.append(childSpace).append(' ').append(key).append(": ").append(writer.apply(value)).append('\n');
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <Z extends Enum<Z>> T convertValue(Object value) {
        if (this.type.isEnum()) {
            var res = Enum.valueOf((Class<Z>) this.type, String.valueOf(value).toUpperCase(Locale.ROOT));
            return (T) res;
        }

        return this.type.cast(value);
    }
}
