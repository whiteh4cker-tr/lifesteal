package tr.alperendemir.helix.config.components.values;

import tr.alperendemir.helix.api.config.ConfigEntry;
import tr.alperendemir.helix.api.config.components.values.ParsedValueConfigComponent;
import tr.alperendemir.helix.api.config.errors.ConfigError;
import tr.alperendemir.helix.api.config.errors.MismatchedTypeConfigError;
import tr.alperendemir.helix.api.config.errors.MissingValueConfigError;
import tr.alperendemir.helix.api.config.parsing.TypeParser;
import tr.alperendemir.helix.api.logging.HelixLogger;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SimpleParsedValueConfigComponent<S, C> implements ParsedValueConfigComponent<S, C> {

    private final SimpleValueConfigComponent<C> component;
    private TypeParser<S, C> parser;

    public SimpleParsedValueConfigComponent(ConfigEntry entry, String id, Class<C> type, C defaultValue) {
        this.component = new SimpleValueConfigComponent<>(entry, id, type, defaultValue);
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
        if (this.parser() == null) {
            this.component.write(builder, indent);
            return;
        }

        this.component.write(builder, indent, this.parser().serialize(this.value()));
    }

    @Override
    public void writeError(@NotNull StringBuilder builder, @NotNull ConfigError error, int indent) {
        if (this.parser() == null) {
            this.component.writeError(builder, error, indent);
            return;
        }

        this.component.writeError(builder, error, indent, this.parser().serialize(this.defaultValue()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConfigError read(@NotNull final Map<?, ?> data) {
        if (this.parser() == null) {
            return this.component.read(data);
        }

        var value = data.get("value");
        if (value == null) {
            return new MissingValueConfigError();
        }

        try {
            return this.component.read(Map.of("value", this.parser().deserialize((S) value)));
        } catch (ClassCastException exception) {
            exception.printStackTrace();
            return new MismatchedTypeConfigError(
                    this.defaultValue(),
                    value
            );
        } catch (Throwable throwable) {
            HelixLogger.reportError(throwable);
        }

        return null;
    }

    @Override
    public C defaultValue() {
        return this.component.defaultValue();
    }

    @Override
    public C value() {
        return this.component.value();
    }

    @Override
    public void value(C value) {
        this.component.value(value);
    }

    @Override
    public Class<C> type() {
        return this.component.type();
    }

    @Override
    public TypeParser<S, C> parser() {
        return this.parser;
    }

    @Override
    public ParsedValueConfigComponent<S, C> parser(TypeParser<S, C> parser) {
        this.parser = parser;
        return this;
    }

}