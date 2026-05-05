package tr.alperendemir.helix.api.config.parsing;

import tr.alperendemir.helix.api.config.Configuration;
import tr.alperendemir.helix.api.config.builder.ConfigurationBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface CompoundTypeParser<T> {

    @NotNull
    Class<T> complexType();

    @NotNull
    T deserialize(@NotNull final Configuration value);

    void serialize(@NotNull final Configuration section, @NotNull final T value);

    void setup(@NotNull final ConfigurationBuilder template, @NotNull final T value);

    default void setup(@NotNull final ConfigurationBuilder template, @NotNull final Map<?, ?> map) {

    }

    default boolean useMapSetup() {
        return false;
    }

    @Nullable
    default List<T> getExamples() {
        return null;
    }

}
