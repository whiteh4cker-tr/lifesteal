package tr.alperendemir.helix.config.builder.arrays;

import tr.alperendemir.helix.api.config.ConfigEntry;
import tr.alperendemir.helix.api.config.builder.ConfigurationBuilder;
import tr.alperendemir.helix.api.config.parsing.CompoundTypeParser;
import tr.alperendemir.helix.config.builder.CommentableElementBuilder;
import tr.alperendemir.helix.config.components.arrays.SimpleCompoundArrayConfigComponent;

public class CompoundArrayElementBuilder<T> extends CommentableElementBuilder {

    private final ConfigurationBuilder parent;
    private final ConfigEntry entry;

    private final String name;
    private final T[] defaultValue;
    private final CompoundTypeParser<T> parser;

    public CompoundArrayElementBuilder(ConfigurationBuilder parent, ConfigEntry entry, String name, T[] defaultValue, CompoundTypeParser<T> parser) {
        super(entry, name);
        this.parent = parent;
        this.entry = entry;
        this.name = name;
        this.defaultValue = defaultValue;
        this.parser = parser;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConfigurationBuilder next() {
        var value = new SimpleCompoundArrayConfigComponent<>(this.entry, this.name, (Class<T>) this.defaultValue.getClass().componentType(), this.defaultValue);
        value.parser(this.parser);

        this.entry.addComponent(value);
        return this.parent;
    }

}
