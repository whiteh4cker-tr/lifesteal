package tr.alperendemir.helix.api.config.components;

import tr.alperendemir.helix.api.config.ConfigComponent;
import tr.alperendemir.helix.api.config.ConfigEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TreeConfigComponent extends ConfigComponent, ConfigEntry {

    @NotNull
    String path();

    @Nullable
    TreeConfigComponent parent();

}
