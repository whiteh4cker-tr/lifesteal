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
import org.jetbrains.annotations.NotNull;

import java.io.File;

public interface HelixProvider {

    @NotNull HelixPluginLoader pluginLoader();

    @NotNull HelixEventRegistry events();

    @NotNull HelixItemRegistry items();

    @NotNull HelixCommandRegistry commands();

    @NotNull HelixScreenRegistry screens();

    @NotNull HelixWorldManager worlds();

    @NotNull HelixPlayerManager players();

    @NotNull HelixConfigurationProvider config();

    @NotNull HelixErrorReporter errors();

    @NotNull HelixScheduler scheduler();

    @NotNull
    HelixTagManager tags();

    @NotNull
    Version version();

    @NotNull
    File providerFile();
}
