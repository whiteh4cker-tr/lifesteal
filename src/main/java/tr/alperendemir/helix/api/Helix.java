package tr.alperendemir.helix.api;

import tr.alperendemir.helix.api.commands.HelixCommandRegistry;
import tr.alperendemir.helix.api.config.HelixConfigurationProvider;
import tr.alperendemir.helix.api.events.HelixEventRegistry;
import tr.alperendemir.helix.api.items.HelixItemRegistry;
import tr.alperendemir.helix.api.players.HelixPlayerManager;
import tr.alperendemir.helix.api.plugins.loading.HelixPluginLoader;
import tr.alperendemir.helix.api.reporting.HelixErrorReporter;
import tr.alperendemir.helix.api.scheduling.HelixScheduler;
import tr.alperendemir.helix.api.screens.HelixScreenRegistry;
import tr.alperendemir.helix.api.semver.Version;
import tr.alperendemir.helix.api.tags.HelixTagManager;
import tr.alperendemir.helix.api.words.HelixWorldManager;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;

public final class Helix {

    private static HelixProvider helixProvider;

    @ApiStatus.Internal
    public static void setHelixProvider(HelixProvider helixProvider) {
        if (Helix.helixProvider != null) {
            throw new IllegalStateException("HelixProvider was already set!");
        }

        Helix.helixProvider = helixProvider;
    }

    public static HelixProvider provider() {
        return Helix.helixProvider;
    }

    public static HelixPluginLoader pluginLoader() {
        return provider().pluginLoader();
    }

    public static HelixEventRegistry events() {
        return provider().events();
    }

    public static HelixItemRegistry items() { return provider().items(); }

    public static HelixCommandRegistry commands() {
        return provider().commands();
    }

    public static HelixErrorReporter errors() {
        return provider().errors();
    }

    public static HelixConfigurationProvider config() {
        return provider().config();
    }

    public static HelixScheduler scheduler() {
        return provider().scheduler();
    }

    public static HelixScreenRegistry screens() {
        return provider().screens();
    }

    public static HelixWorldManager worlds() {
        return provider().worlds();
    }

    public static HelixPlayerManager players() {
        return provider().players();
    }

    public static HelixTagManager tags() {
        return provider().tags();
    }

    public static Version version() {
        return provider().version();
    }

    public static File providerFile() {
        return provider().providerFile();
    }
}
