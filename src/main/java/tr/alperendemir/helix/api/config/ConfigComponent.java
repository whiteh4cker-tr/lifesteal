package tr.alperendemir.helix.api.config;

import tr.alperendemir.helix.api.config.errors.ConfigError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ConfigComponent {

    @NotNull
    ConfigEntry entry();

    String id();

    void write(@NotNull final StringBuilder builder, int indent);

    void writeError(@NotNull final StringBuilder builder, @NotNull final ConfigError error, int indent);

    @Nullable
    ConfigError read(@NotNull final Map<?, ?> data);

    default boolean canWriteError() {
        return true;
    }

    default boolean canRead() {
        return true;
    }


}
