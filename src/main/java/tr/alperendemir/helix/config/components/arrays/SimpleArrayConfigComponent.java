package tr.alperendemir.helix.config.components.arrays;

import tr.alperendemir.helix.api.config.ConfigEntry;
import tr.alperendemir.helix.api.config.components.arrays.ArrayConfigComponent;
import tr.alperendemir.helix.api.config.errors.ArrayConfigError;
import tr.alperendemir.helix.api.config.errors.ConfigError;
import tr.alperendemir.helix.api.config.errors.ExceptionConfigError;
import tr.alperendemir.helix.api.config.errors.InvalidValueConfigError;
import tr.alperendemir.helix.api.config.errors.MismatchedTypeConfigError;
import tr.alperendemir.helix.api.config.errors.MissingValueConfigError;
import tr.alperendemir.helix.api.logging.HelixLogger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class SimpleArrayConfigComponent<T> implements ArrayConfigComponent<T> {

    private final ConfigEntry entry;
    private final String id;
    private final Class<T> type;
    private final T[] defaultValues;
    private T[] values;

    public SimpleArrayConfigComponent(ConfigEntry entry, String id, Class<T> type, T[] defaultValue) {
        this.entry = entry;
        this.id = id;
        this.type = type;
        this.defaultValues = defaultValue;

        this.values(this.defaultValues);
    }

    @Override
    public T[] defaultValues() {
        return this.defaultValues;
    }

    @Override
    public T[] values() {
        return this.values == null ? this.defaultValues() : this.values;
    }

    @Override
    public ArrayConfigComponent<T> values(T[] values) {
        this.values = values;
        return this;
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

        builder.append(space).append(this.id()).append(":\n");

        for (var value : this.values) {
            builder.append(space).append("  - ").append(value).append('\n');
        }
    }

    @Override
    public void writeError(@NotNull StringBuilder builder, @NotNull ConfigError error, int indent) {
        this.writeError(builder, error, indent, String::valueOf);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConfigError read(@NotNull final Map<?, ?> data) {
        var maps = data.get("value");
        if (!(maps instanceof List<?> mapList)) {
            return new MissingValueConfigError();
        }

        this.values = (T[]) Array.newInstance(this.type, mapList.size());

        var errors = new ConfigError[mapList.size()];
        var hasError = false;

        for (int i = 0; i < mapList.size(); i++) {
            var value = mapList.get(i);
            try {
                this.values[i] = this.convertValue(value);
            } catch (ClassCastException e) {
                errors[i] = new MismatchedTypeConfigError(
                        this.defaultValues().length > i ? this.defaultValues()[i] : null,
                        value
                );
                hasError = true;
            } catch (IllegalArgumentException e) {
                errors[i] = new InvalidValueConfigError(
                        this.defaultValues().length < i ? null : this.defaultValues()[i],
                        value,
                        InvalidValueConfigError.getSuggestion(this.type(), value)
                );
                hasError = true;
            } catch (Throwable throwable) {
                HelixLogger.reportError(throwable);
            }
        }

        return hasError ? new ArrayConfigError(errors) : null;
    }

    @SuppressWarnings("unchecked")
    void writeError(@NotNull StringBuilder builder, @NotNull ConfigError error, int indent, Function<T, String> writer) {
        if (error instanceof MissingValueConfigError) {
            var space = "    ".repeat(indent).substring(indent == 0 ? 0 : 1);

            builder.append("<light:green>+").append(space).append(this.id()).append(":\n");

            for (var value : this.values) {
                builder.append("<light:green>+").append(space).append("  - ").append(writer.apply(value)).append('\n');
            }

            return;
        }

        if (error instanceof ExceptionConfigError) {
            var space = "    ".repeat(indent).substring(indent == 0 ? 0 : 1);

            for (var value : this.values) {
                builder.append("<light:red>-").append(space).append("  - ").append(writer.apply(value)).append('\n');
            }

            for (var defaultValue : this.defaultValues) {
                builder.append("<light:green>+").append(space).append("  - ").append(writer.apply(defaultValue)).append('\n');
            }

            return;
        }

        var space = "    ".repeat(indent);

        if (!(error instanceof ArrayConfigError configError)) return;

        for (int i = 0; i < this.values.length; i++) {
            var childError = configError.childErrors()[i];
            if (childError == null) {
                builder.append(space).append("  - ").append(writer.apply(this.values[i])).append('\n');
                continue;
            }

            if (childError instanceof MismatchedTypeConfigError mismatched) {
                var expected = mismatched.expected();
                if (expected == null) {
                    builder.append("<light:red>-");

                    var len = builder.length();
                    builder.append(space).append("  - ").append(mismatched.received()).append('\n');

                    builder.deleteCharAt(len);
                    continue;
                }

                builder.append(space).append("  - ").append(writer.apply((T) expected)).append(" # ").append(mismatched.getMessage()).append('\n');
            }

            if (childError instanceof InvalidValueConfigError invalid) {
                builder.append("<light:green>-");

                var len = builder.length();
                builder.append(space).append("  - ").append(invalid.getReceived()).append("<light:yellow> # ").append(invalid.getMessage()).append('\n');

                builder.deleteCharAt(len);
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
