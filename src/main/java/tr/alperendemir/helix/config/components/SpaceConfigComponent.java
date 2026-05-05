package tr.alperendemir.helix.config.components;

import tr.alperendemir.helix.api.config.ConfigComponent;
import tr.alperendemir.helix.api.config.ConfigEntry;
import tr.alperendemir.helix.api.config.errors.ConfigError;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class SpaceConfigComponent implements ConfigComponent {

    private final ConfigEntry entry;
    private final String id;

    public SpaceConfigComponent(ConfigEntry entry) {
        this.entry = entry;
        this.id = "space_" + ThreadLocalRandom.current().nextInt();
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
        builder.append('\n');
    }

    @Override
    public void writeError(@NotNull StringBuilder builder, @NotNull ConfigError error, int indent) {
        // Empty
    }

    @Override
    public ConfigError read(@NotNull final Map<?, ?> data) {
        return null;
    }

    @Override
    public boolean canRead() {
        return false;
    }
}
