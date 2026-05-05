package tr.alperendemir.helix.config.builder;

import tr.alperendemir.helix.api.config.Configuration;
import tr.alperendemir.helix.api.config.builder.ConfigurationBuilder;
import tr.alperendemir.helix.api.config.builder.ElementBuilder;
import tr.alperendemir.helix.api.config.parsing.CompoundTypeParser;
import tr.alperendemir.helix.api.config.parsing.TypeParser;
import tr.alperendemir.helix.config.ConfigSection;
import tr.alperendemir.helix.config.SimpleConfiguration;
import tr.alperendemir.helix.config.builder.arrays.ArrayElementBuilder;
import tr.alperendemir.helix.config.builder.arrays.CompoundArrayElementBuilder;
import tr.alperendemir.helix.config.builder.maps.MapElementBuilder;
import tr.alperendemir.helix.config.builder.values.CompoundElementBuilder;
import tr.alperendemir.helix.config.builder.values.ValueElementBuilder;
import tr.alperendemir.helix.config.components.SimpleCommentConfigComponent;
import tr.alperendemir.helix.config.components.SpaceConfigComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;

public class SimpleConfigurationBuilder implements ConfigurationBuilder {

    private final ConfigurationBuilder parent;
    private final ConfigSection entry;
    private final SimpleCommentConfigComponent component;

    SimpleConfigurationBuilder(ConfigurationBuilder parent, ConfigSection entry) {
        this.parent = parent;
        this.entry = entry;

        this.component = new SimpleCommentConfigComponent(entry, entry.id() + "____comments");

        var parentEntry = entry.parent();
        if (parentEntry == null) {
            return;
        }

        if (!parentEntry.components().isEmpty()) {
            parentEntry.addComponent(new SpaceConfigComponent(entry));
        }

        parentEntry.addComponent(this.component);
    }

    public SimpleConfigurationBuilder(String name) {
        this(null, new ConfigSection(name));
    }

    @Override
    public ConfigurationBuilder parent() {
        return this.parent;
    }

    @Override
    public ConfigurationBuilder child(String key) {
        var childEntry = new ConfigSection(this.entry, key);
        var builder = new SimpleConfigurationBuilder(this, childEntry);
        this.entry.addComponent(childEntry);
        return builder;
    }

    @Override
    public <T> ElementBuilder compound(String name, T defaultValue, CompoundTypeParser<T> parser) {
        return new CompoundElementBuilder<>(this, this.entry, name, defaultValue, parser);
    }

    @Override
    public <S, C> ElementBuilder value(String name, C defaultValue, TypeParser<S, C> parser) {
        return new ValueElementBuilder<>(this, this.entry, name, defaultValue, parser);
    }

    @Override
    public <S, C> ElementBuilder map(String name, Class<C> type, Map<String, C> defaultValue, TypeParser<S, C> parser) {
        return new MapElementBuilder<>(this, this.entry, name, defaultValue, parser, type);
    }

    @Override
    public <T> ElementBuilder compoundArray(String name, T[] defaultValue, CompoundTypeParser<T> parser) {
        return new CompoundArrayElementBuilder<>(this, this.entry, name, defaultValue, parser);
    }

    @Override
    public <S, C> ElementBuilder valueArray(String name, C[] defaultValue, TypeParser<S, C> parser) {
        return new ArrayElementBuilder<>(this, this.entry, name, defaultValue, parser);
    }

    @Override
    public Configuration build(File defaultFile) {
        return new SimpleConfiguration(defaultFile, this.entry);
    }

    @Override
    public @NotNull List<String> comments() {
        return this.component.comments();
    }

    @Override
    public @NotNull ConfigurationBuilder comment(@Nullable String comment) {
        this.component.comment(comment);
        return this;
    }

    @Override
    public @NotNull ConfigurationBuilder commentSpace() {
        this.component.commentSpace();
        return this;
    }

    @Override
    public @NotNull <Z extends Enum<Z>> ConfigurationBuilder commentEnum(Class<Z> enumClass) {
        this.component.commentEnum(enumClass);
        return this;
    }

    @Override
    public @NotNull <Z extends Enum<Z>> ConfigurationBuilder commentEnum(Class<Z> enumClass, Z value) {
        this.component.commentEnum(enumClass, value);
        return this;
    }
}
