package tr.alperendemir.helix.config.components.maps;

import tr.alperendemir.helix.api.config.ConfigEntry;
import tr.alperendemir.helix.api.config.components.maps.ParsedMapConfigComponent;
import tr.alperendemir.helix.api.config.errors.ConfigError;
import tr.alperendemir.helix.api.config.errors.MismatchedTypeConfigError;
import tr.alperendemir.helix.api.config.errors.MissingValueConfigError;
import tr.alperendemir.helix.api.config.errors.TreeConfigError;
import tr.alperendemir.helix.api.config.parsing.TypeParser;
import tr.alperendemir.helix.api.logging.HelixLogger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SimpleParsedMapConfigComponent<S, C> implements ParsedMapConfigComponent<S, C> {

    private final SimpleMapConfigComponent<C> component;
    private TypeParser<S, C> parser;

    public SimpleParsedMapConfigComponent(ConfigEntry entry, String id, Class<C> type, Map<String, C> defaultValue) {
        this.component = new SimpleMapConfigComponent<>(entry, id, type, defaultValue);
    }

    @Override
    public TypeParser<S, C> parser() {
        return this.parser;
    }

    @Override
    public ParsedMapConfigComponent<S, C> parser(TypeParser<S, C> parser) {
        this.parser = parser;
        return this;
    }

    @Override
    public Map<String, C> defaultValues() {
        return this.component.defaultValues();
    }

    @Override
    public Map<String, C> values() {
        return this.component.values();
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
        if (this.values().isEmpty()) {
            builder.append(" {}\n");
            return;
        }

        var childSpace = "    ".repeat(indent + 1);

        if (this.parser() == null) {
            builder.append("\n");
            for (var value : this.values().entrySet()) {
                builder.append(childSpace).append(value.getKey()).append(": ").append(value.getValue()).append('\n');
            }
            return;
        }

        builder.append("\n");
        for (var value : this.values().entrySet()) {
            builder.append(childSpace).append(value.getKey()).append(": ").append(this.parser.serialize(value.getValue())).append('\n');
        }
    }

    @Override
    public void writeError(@NotNull StringBuilder builder, @NotNull ConfigError error, int indent) {
        if (this.parser == null) {
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
        if (!(maps instanceof Map<?, ?> map)) {
            return new MissingValueConfigError();
        }

        var values = this.component.values();

        var treeError = new TreeConfigError(new HashMap<>());

        for (var entry : map.entrySet()) {
            var key = String.valueOf(entry.getKey());
            try {
                values.put(key, this.parser.deserialize((S) entry.getValue()));
            } catch (ClassCastException e) {
                treeError.childErrors().put(key, new MismatchedTypeConfigError(
                        this.type(),
                        entry.getValue()
                ));
            } catch (Throwable throwable) {
                HelixLogger.reportError(throwable);
            }
        }

        return treeError.childErrors().isEmpty() ? null : treeError;
    }
}
