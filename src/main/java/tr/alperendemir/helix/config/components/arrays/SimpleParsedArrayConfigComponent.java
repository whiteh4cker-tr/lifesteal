package tr.alperendemir.helix.config.components.arrays;

import tr.alperendemir.helix.api.config.ConfigEntry;
import tr.alperendemir.helix.api.config.components.arrays.ParsedArrayConfigComponent;
import tr.alperendemir.helix.api.config.errors.ArrayConfigError;
import tr.alperendemir.helix.api.config.errors.ConfigError;
import tr.alperendemir.helix.api.config.errors.InvalidValueConfigError;
import tr.alperendemir.helix.api.config.errors.MismatchedTypeConfigError;
import tr.alperendemir.helix.api.config.errors.MissingValueConfigError;
import tr.alperendemir.helix.api.config.parsing.TypeParser;
import tr.alperendemir.helix.api.logging.HelixLogger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

public class SimpleParsedArrayConfigComponent<S, C> implements ParsedArrayConfigComponent<S, C> {

    private final SimpleArrayConfigComponent<C> component;
    private TypeParser<S, C> parser;

    @SuppressWarnings("unchecked")
    public SimpleParsedArrayConfigComponent(ConfigEntry entry, String id, C[] defaultValue) {
        this.component = new SimpleArrayConfigComponent<>(entry, id, (Class<C>) defaultValue.getClass().componentType(), defaultValue);
    }

    @Override
    public TypeParser<S, C> parser() {
        return this.parser;
    }

    @Override
    public ParsedArrayConfigComponent<S, C> parser(TypeParser<S, C> parser) {
        this.parser = parser;
        return this;
    }

    @Override
    public C[] defaultValues() {
        return this.component.defaultValues();
    }

    @Override
    public C[] values() {
        return this.component.values();
    }

    @Override
    public ParsedArrayConfigComponent<S, C> values(C[] value) {
        this.component.values(value);
        return this;
    }

    @Override
    public Class<C> type() {
        return this.component.type();
    }

    @Override
    public @NotNull ConfigEntry entry() {
        return this.component.entry();
    }

    @Override
    public @NotNull String id() {
        return this.component.id();
    }

    @Override
    public void write(@NotNull final StringBuilder builder, int indent) {
        var space = "    ".repeat(indent);

        builder.append(space).append(this.id()).append(':');
        if (this.values().length == 0) {
            builder.append(" []\n");
            return;
        }

        if (this.parser() == null) {
            builder.append("\n");
            for (var value : this.values()) {
                builder.append(space).append("  - ").append(value).append('\n');
            }
            return;
        }

        builder.append('\n');

        for (var value : this.values()) {
            builder.append(space).append("  - ").append(this.parser.serialize(value)).append('\n');
        }
    }

    @Override
    public void writeError(@NotNull StringBuilder builder, @NotNull ConfigError error, int indent) {
        System.out.println("CONFIG ERROR TYPE: " + error);
        if (this.parser() == null) {
            this.component.writeError(builder, error, indent);
            return;
        }

        this.component.writeError(builder, error, indent, c -> String.valueOf(this.parser.serialize(c)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConfigError read(@NotNull final Map<?, ?> data) {
        if (this.parser() == null) {
            return this.component.read(data);
        }

        var maps = data.get("value");
        if (!(maps instanceof List<?> mapList)) {
            return new MissingValueConfigError();
        }

        var arr = (C[]) Array.newInstance(this.type(), mapList.size());
        this.component.values(arr);

        var errorArr = new ConfigError[arr.length];
        var hasError = false;

        var defaults = this.defaultValues();

        for (int i = 0; i < mapList.size(); i++) {
            var value = mapList.get(i);

            var def = defaults.length > i ? defaults[i] : null;

            try {
                arr[i] = this.parser.deserialize((S) value);
            } catch (ClassCastException e) {
                errorArr[i] = new MismatchedTypeConfigError(
                        def,
                        value
                );
                hasError = true;
            } catch (IllegalArgumentException e) {
                errorArr[i] = new InvalidValueConfigError(
                        def,
                        value,
                        InvalidValueConfigError.getSuggestion(this.type(), value)
                );
                hasError = true;
            } catch (Throwable throwable) {
                HelixLogger.reportError(throwable);
            }
        }

        return hasError ? new ArrayConfigError(errorArr) : null;
    }
}
