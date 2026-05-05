package tr.alperendemir.helix.config.components.arrays;

import tr.alperendemir.helix.api.config.ConfigEntry;
import tr.alperendemir.helix.api.config.Configuration;
import tr.alperendemir.helix.api.config.components.arrays.CompoundArrayConfigComponent;
import tr.alperendemir.helix.api.config.errors.ArrayConfigError;
import tr.alperendemir.helix.api.config.errors.ConfigError;
import tr.alperendemir.helix.api.config.errors.ExceptionConfigError;
import tr.alperendemir.helix.api.config.errors.MissingValueConfigError;
import tr.alperendemir.helix.api.config.errors.TreeConfigError;
import tr.alperendemir.helix.api.config.parsing.CompoundTypeParser;
import tr.alperendemir.helix.api.logging.HelixLogger;
import tr.alperendemir.helix.config.builder.SimpleConfigurationBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SimpleCompoundArrayConfigComponent<T> implements CompoundArrayConfigComponent<T> {

    private final ConfigEntry entry;
    private final String id;
    private final Class<T> type;
    private final T[] defaultValues;
    private T[] values;
    private CompoundTypeParser<T> parser;
    private String indexString;

    private Configuration configuration;

    public SimpleCompoundArrayConfigComponent(ConfigEntry entry, String id, Class<T> type, T[] defaultValues) {
        this.entry = entry;
        this.id = id;
        this.type = type;
        this.defaultValues = defaultValues;

        this.values(Arrays.copyOf(this.defaultValues, this.defaultValues.length));
    }

    @Override
    public CompoundTypeParser<T> parser() {
        return this.parser;
    }

    @Override
    public CompoundArrayConfigComponent<T> parser(CompoundTypeParser<T> parser) {
        this.parser = parser;

        this.setupParser(null);
        return this;
    }

    @Override
    public @NotNull String indexString() {
        return this.indexString == null ? "index" : this.indexString;
    }

    @Override
    public CompoundArrayConfigComponent<T> indexString(@Nullable String indexString) {
        this.indexString = indexString;
        return this;
    }

    @Override
    public T[] defaultValues() {
        return Arrays.copyOf(this.defaultValues, this.defaultValues.length);
    }

    @Override
    public T[] values() {
        return this.values == null ? this.defaultValues() : this.values;
    }

    @Override
    public CompoundArrayConfigComponent<T> values(T[] values) {
        this.values = values;

        this.setupParser(null);
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

        builder.append(space).append(this.id()).append(':');

        if (this.parser() == null) {
            builder.append(" {}\n");
            return;
        }

        builder.append('\n');

        for (int i = 0; i < this.values.length; i++) {
            var value = this.values[i];
            if (value == null) continue;

            builder.append(space).append("  - ").append(this.indexString()).append(": ").append(i).append('\n');

            this.setupParser(value);
            this.parser.serialize(this.configuration, value);
            this.configuration.write(builder, indent + 1);
        }
    }

    @Override
    public void writeError(@NotNull StringBuilder builder, @NotNull ConfigError error, int indent) {
        var space = "    ".repeat(indent);

        builder.append(space).append(this.id()).append(':');

        if (this.parser() == null) {
            var spacing = space + " ".repeat(this.id.length() + 1);
            builder.append(" {} # The devs made a mistake, report this to the plugin author, copy the first line:\n");
            builder.append(spacing).append(" # NO VALUE PARSER PRESENT FOR OPTION ").append(this.id()).append(" OF TYPE ").append(this.type).append('\n');
            builder.append(spacing).append(" # ");
            builder.append(spacing).append(" # ");
            builder.append(spacing).append(" # The plugin (may) not work while this remains unfixed, the developers are the only ones that can fix this.");
            return;
        }

        if (!(error instanceof ArrayConfigError configError)) {
            this.write(builder, indent);
            return;
        }

        builder.append('\n');

        for (int i = 0; i < this.values.length; i++) {
            var childError = configError.childErrors()[i];

            var hasError = childError != null;

            var value = this.values[i];

            if (value != null || hasError) {

                if (childError instanceof ExceptionConfigError ex) {
                    builder.append('\n').append(space).append("<light:white>  There was an error while reading this section of the config.");
                    builder.append('\n').append(space).append("<light:white>  The lines in yellow indicate that you should set the config values to the same values as indicated.");
                    builder.append('\n').append(space).append("<light:white>  Error message: <light:red>").append(ex.throwable().getMessage());
                    builder.append("\n<reset>");
                }

                var child = childError instanceof ExceptionConfigError
                        ? "<light:yellow>*" + space.substring(Math.min(1, space.length()))
                        : space;
                builder.append(child).append("  - ").append(this.indexString()).append(": ").append(i).append('\n');
            }

            this.setupParser(value);

            if (value == null) {
                if (childError instanceof TreeConfigError treeConfigError) {
                    this.parser.serialize(this.configuration, this.defaultValues()[0]);
                    this.configuration.writeError(builder, treeConfigError, indent + 1);
                }
                continue;
            }

            this.parser.serialize(this.configuration, value);

            if (childError instanceof TreeConfigError treeConfigError) {
                this.configuration.writeError(builder, treeConfigError, indent + 1);
                continue;
            }

            var start = builder.length() - 1;

            this.configuration.write(builder, indent + 1);

            if (childError instanceof ExceptionConfigError) {
                for (int j = start; j < builder.length(); j++) {
                    if (builder.charAt(j) == '\n' && j < builder.length() - 1) {
                        var next = builder.charAt(j + 1);

                        if (next == ' ') {
                            builder.setCharAt(j + 1, '*');
                        }

                        builder.replace(j, j + 1, "\n<light:yellow>");
                    }
                }
                builder.append('\n');
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConfigError read(@NotNull final Map<?, ?> data) {
        var maps = data.get("value");
        if (!(maps instanceof List<?> mapList)) {
            return new MissingValueConfigError();
        }

        var max = 0;
        for (int i = 0; i < mapList.size(); i++) {
            var dataMap = (Map<?, ?>) mapList.get(i);

            var idx = dataMap.get(this.indexString());
            var index = this.parseIndex(idx, i);

            if (index >= max) max = index + 1;
        }

        this.values = (T[]) Array.newInstance(this.type(), max);

        var errors = new ConfigError[max];
        var hasError = false;

        for (int i = 0; i < mapList.size(); i++) {
            var dataMap = (Map<?, ?>) mapList.get(i);

            var idx = dataMap.get(this.indexString());
            var index = this.parseIndex(idx, i);

            if (this.parser != null && this.parser.useMapSetup()) {
                var builder = new SimpleConfigurationBuilder(null);
                this.parser.setup(builder, dataMap);

                this.configuration = builder.build(null);
            }

            var childErrors = this.configuration.read(dataMap);
            if (childErrors != null) {
                errors[index] = childErrors;

                hasError = true;
            }

            try {
                var deserialized = this.parser().deserialize(this.configuration);
                this.values[index] = deserialized;
            } catch (Throwable exception) {
                errors[index] = new ExceptionConfigError(exception);
                HelixLogger.reportError(exception);

                hasError = true;

                var def = this.defaultValues.length > index ? this.defaultValues[index] : null;
                this.values[index] = def;
            }
        }

        return hasError ? new ArrayConfigError(errors) : null;
    }

    private void setupParser(T value) {
        if (value == null && this.defaultValues.length > 0) {
            value = this.defaultValues[0];
        }

        if (this.parser != null && value != null) {
            var builder = new SimpleConfigurationBuilder(null);
            this.parser.setup(builder, value);

            this.configuration = builder.build(null);
        }
    }

    private int parseIndex(Object object, int index) {
        if (object == null) return index;

        if (object instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(String.valueOf(object));
        } catch (Exception exception) {
            exception.printStackTrace();
            return index;
        }
    }
}
