package tr.alperendemir.helix.api.config;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface ConfigEntry {

    @NotNull
    Map<String, ConfigComponent> components();

    ConfigEntry addComponent(@NotNull ConfigComponent child);

    <T extends ConfigComponent> T component(@NotNull String id);

}
