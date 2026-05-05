package tr.alperendemir.helix.api.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Commentable<T extends Commentable<T>> {

    @NotNull
    List<String> comments();

    @NotNull
    T comment(@Nullable String comment);

    @NotNull
    T commentSpace();

    @NotNull
    <Z extends Enum<Z>> T commentEnum(Class<Z> enumClass);

    @NotNull
    <Z extends Enum<Z>> T commentEnum(Class<Z> enumClass, Z value);

}
