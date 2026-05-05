package tr.alperendemir.helix.api.config.parsing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface TypeParser<S, C> {

    @NotNull
    Class<C> complexType();

    @NotNull
    Class<S> simpleType();

    @NotNull
    C deserialize(@NotNull final S value);

    @NotNull
    S serialize(@NotNull final C value);

    @Nullable
    default List<C> getComplexExamples() {
        return null;
    }

    @Nullable
    default List<S> getSimpleExamples() {
        return null;
    }

}
