package tr.alperendemir.helix.api.config;

import tr.alperendemir.helix.api.config.builder.ConfigurationBuilder;

public interface HelixConfigurationProvider {

    ConfigurationBuilder createBuilder();
}
