package tr.alperendemir.helix.api.config.builder;

import tr.alperendemir.helix.api.config.Commentable;
import tr.alperendemir.helix.api.config.Configuration;
import tr.alperendemir.helix.api.config.parsing.CompoundTypeParser;
import tr.alperendemir.helix.api.config.parsing.TypeParser;

import java.io.File;
import java.util.Map;

public interface ConfigurationBuilder extends Commentable<ConfigurationBuilder> {

    ConfigurationBuilder parent();

    ConfigurationBuilder child(String key);

    <T> ElementBuilder compound(String name, T defaultValue, CompoundTypeParser<T> parser);

    <S, C> ElementBuilder value(String name, C defaultValue, TypeParser<S, C> parser);

    default <C> ElementBuilder value(String name, C defaultValue) {
        return value(name, defaultValue, null);
    }

    <S, C> ElementBuilder map(String name, Class<C> type, Map<String, C> defaultValue, TypeParser<S, C> parser);

    /**
     * @param name The name of the option
     * @param defaultValue The default map, requires at least one entry.
     * @param parser The optional parser for the values
     * */
    @SuppressWarnings("unchecked")
    default <S, C> ElementBuilder map(String name, Map<String, C> defaultValue, TypeParser<S, C> parser) {
        if (defaultValue.isEmpty()) {
            throw new IllegalArgumentException("Default map must have at least one value if the type is not specified!");
        }

        var type = (Class<C>) defaultValue.values().iterator().next().getClass();
        return map(name, type, defaultValue, parser);
    }

    /**
     * @param name The name of the option
     * @param defaultValue The default map, requires at least one entry.
     * */
    default <C> ElementBuilder map(String name, Map<String, C> defaultValue) {
        return map(name, defaultValue, null);
    }

    <T> ElementBuilder compoundArray(String name, T[] defaultValue, CompoundTypeParser<T> parser);

    <S, C> ElementBuilder valueArray(String name, C[] defaultValue, TypeParser<S, C> parser);

    default <C> ElementBuilder valueArray(String name, C[] defaultValue) {
        return valueArray(name, defaultValue, null);
    }

    Configuration build(File defaultFile);

}
