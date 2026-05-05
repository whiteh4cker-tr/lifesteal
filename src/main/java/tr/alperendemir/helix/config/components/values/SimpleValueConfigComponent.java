package tr.alperendemir.helix.config.components.values;

import tr.alperendemir.helix.api.config.ConfigEntry;
import tr.alperendemir.helix.api.config.components.values.ValueConfigComponent;
import tr.alperendemir.helix.api.config.errors.ConfigError;
import tr.alperendemir.helix.api.config.errors.InvalidValueConfigError;
import tr.alperendemir.helix.api.config.errors.MismatchedTypeConfigError;
import tr.alperendemir.helix.api.config.errors.MissingValueConfigError;
import tr.alperendemir.helix.api.logging.HelixLogger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class SimpleValueConfigComponent<T> implements ValueConfigComponent<T> {

    private static final Map<Class<?>, Integer> SIZE_MAP = new HashMap<>();
    private static final Map<Class<?>, Function<Number, Number>> CONVERSION_MAP = new HashMap<>();


    static {
        SIZE_MAP.clear();
        CONVERSION_MAP.clear();

        SIZE_MAP.put(Byte.class, 1);
        SIZE_MAP.put(Short.class, 2);
        SIZE_MAP.put(Integer.class, 4);
        SIZE_MAP.put(Long.class, 8);
        SIZE_MAP.put(Float.class, 16);
        SIZE_MAP.put(Double.class, 32);

        CONVERSION_MAP.put(Byte.class, Number::byteValue);
        CONVERSION_MAP.put(Short.class, Number::shortValue);
        CONVERSION_MAP.put(Integer.class, Number::intValue);
        CONVERSION_MAP.put(Long.class, Number::longValue);
        CONVERSION_MAP.put(Float.class, Number::floatValue);
        CONVERSION_MAP.put(Double.class, Number::doubleValue);
    }

    private final ConfigEntry entry;
    private final String id;
    private final Class<T> type;
    private final T defaultValue;
    private T value;

    public SimpleValueConfigComponent(ConfigEntry entry, String id, Class<T> type, T defaultValue) {
        this.entry = entry;
        this.id = id;
        this.type = type;
        this.defaultValue = defaultValue;

        this.value(this.defaultValue);
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
        this.write(builder, indent, this.value());
    }

    @Override
    public void writeError(@NotNull StringBuilder builder, @NotNull ConfigError errors, int indent) {
        this.writeError(builder, errors, indent, this.defaultValue());
    }

    public void writeError(@NotNull StringBuilder builder, @NotNull ConfigError error, int indent, Object defaultValue) {
        if (error instanceof MissingValueConfigError) {
            builder.append("<light:green>+   ");
            this.write(builder, indent - 1, defaultValue);

            builder.setCharAt(builder.length() - 1, ' ');

            builder.append("# A value is required for this option, using default value: ").append(defaultValue);
            builder.append('\n');
            return;
        }

        if (error instanceof MismatchedTypeConfigError mismatch) {
            var space = "    ".repeat(indent);

            builder.append(space).append(mismatch.getMessage()).append('\n');
            builder.append(space).append("Replace:\n<light:red>-   ");
            this.write(builder, Math.max(indent - 1, 0), mismatch.received());
            builder.append(space).append("With:\n<light:green>+   ");
            this.write(builder, Math.max(indent - 1, 0), mismatch.expected());
            return;
        }

        if (error instanceof InvalidValueConfigError invalid) {
            builder.append("<light:green>+   ");
            this.write(builder, indent - 1, defaultValue);

            builder.setCharAt(builder.length() - 1, ' ');

            builder.append("<light:yellow># ").append(invalid.getMessage()).append('\n');
            return;
        }

        this.writeError(builder, indent);
    }

    public void writeError(@NotNull StringBuilder builder, int indent) {
        builder.append("<light:green>");
        var index = builder.length();
        this.write(builder, indent, this.value());

        if (builder.charAt(index) == ' ') {
            builder.setCharAt(index, '+');
        }
    }

    public void write(@NotNull StringBuilder builder, int indent, Object value) {
        builder.append("    ".repeat(indent)).append(this.id()).append(": ");
        if (value instanceof CharSequence) {
            builder.append('"');
        }

        builder.append(value);

        if (value instanceof CharSequence) {
            builder.append('"');
        }

        builder.append('\n');
    }

    @Override
    public ConfigError read(@NotNull final Map<?, ?> data) {
        var value = data.get("value");

        if (value == null) {
            return new MissingValueConfigError();
        }

        try {
            var left = SIZE_MAP.getOrDefault(this.type(), -1);
            var right = SIZE_MAP.getOrDefault(value.getClass(), -1);

            if (value instanceof Number number && left > right) {
                this.value(this.type().cast(this.convert(number, (Class<? extends Number>) this.type())));
                return null;
            }

            this.value(this.convertValue(value));
        } catch (ClassCastException exception) {
            return new MismatchedTypeConfigError(
                    this.defaultValue(),
                    value
            );
        } catch (IllegalArgumentException exception) {
            return new InvalidValueConfigError(
                    this.defaultValue(),
                    value,
                    InvalidValueConfigError.getSuggestion(this.type, value)
            );
        } catch (Throwable throwable) {
            HelixLogger.reportError(throwable);
        }

        return null;
    }

    @Override
    public T defaultValue() {
        return this.defaultValue;
    }

    @Override
    public T value() {
        return this.value == null ? this.defaultValue() : this.value;
    }

    @Override
    public void value(T value) {
        this.value = value;
    }

    @Override
    public Class<T> type() {
        return this.type;
    }

    @SuppressWarnings("unchecked")
    private <Z extends Enum<Z>> T convertValue(Object value) {
        if (this.type.isEnum()) {
            var res = Enum.valueOf((Class<Z>) this.type, String.valueOf(value).toUpperCase(Locale.ROOT));
            return (T) res;
        }

        return this.type.cast(value);
    }

    private Number convert(Number value, Class<? extends Number> type) {
        var converter = CONVERSION_MAP.get(type);
        if (converter != null) {
            return type.cast(converter.apply(value));
        } else {
            throw new IllegalArgumentException("Unsupported type for conversion: " + type.getSimpleName());
        }
    }
}