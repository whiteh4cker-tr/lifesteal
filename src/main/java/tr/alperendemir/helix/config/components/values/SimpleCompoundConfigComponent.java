package tr.alperendemir.helix.config.components.values;

import tr.alperendemir.helix.api.config.ConfigEntry;
import tr.alperendemir.helix.api.config.Configuration;
import tr.alperendemir.helix.api.config.components.values.CompoundConfigComponent;
import tr.alperendemir.helix.api.config.errors.ConfigError;
import tr.alperendemir.helix.api.config.errors.TreeConfigError;
import tr.alperendemir.helix.api.config.parsing.CompoundTypeParser;
import tr.alperendemir.helix.api.logging.HelixLogger;
import tr.alperendemir.helix.config.builder.SimpleConfigurationBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SimpleCompoundConfigComponent<T> implements CompoundConfigComponent<T> {

    private final ConfigEntry entry;
    private final String id;
    private final T defaultValue;
    private T value;
    private CompoundTypeParser<T> parser;

    private Configuration configuration;

    public SimpleCompoundConfigComponent(ConfigEntry entry, String id, T defaultValue) {
        this.entry = entry;
        this.id = id;
        this.defaultValue = defaultValue;

        this.value(this.defaultValue);
    }

    @Override
    public CompoundTypeParser<T> parser() {
        return this.parser;
    }

    @Override
    public CompoundConfigComponent<T> parser(CompoundTypeParser<T> parser) {
        this.parser = parser;

        if (parser != null) {
            var builder = new SimpleConfigurationBuilder(null);
            parser.setup(builder, this.defaultValue);

            this.configuration = builder.build(null);
        }

        return this;
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
    public CompoundConfigComponent<T> value(T value) {
        this.value = value;

        return this;
    }

    @Override
    public Class<T> type() {
        return null;
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
        if (tryWriteHeader(builder, indent)) return;

        this.configuration.write(builder, indent + 1);
    }

    @Override
    public void writeError(@NotNull StringBuilder builder, @NotNull ConfigError error, int indent) {
        if (!(error instanceof TreeConfigError treeConfigError)) return;
        if (tryWriteHeader(builder, indent)) return;

        this.configuration.writeError(builder, treeConfigError, indent + 1);
    }

    private boolean tryWriteHeader(@NotNull StringBuilder builder, int indent) {
        var space = "    ".repeat(indent);

        builder.append(space).append(this.id()).append(':');

        if (this.parser() == null) {
            builder.append(" {}\n");
            return true;
        }

        builder.append('\n');
        this.parser().serialize(this.configuration, this.value());
        return false;
    }

    @Override
    public ConfigError read(@NotNull final Map<?, ?> data) {
        ConfigError error = null;
        T value;
        try {
            error = this.configuration.read(data);
            value = this.parser().deserialize(this.configuration);
        } catch (Throwable throwable) {
            value = this.defaultValue;
            HelixLogger.reportError(throwable);
        }

        this.value(value);

        return error;
    }
}
