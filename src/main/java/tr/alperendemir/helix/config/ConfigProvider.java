package tr.alperendemir.helix.config;

import tr.alperendemir.helix.api.config.HelixConfigurationProvider;
import tr.alperendemir.helix.api.config.builder.ConfigurationBuilder;
import tr.alperendemir.helix.config.builder.SimpleConfigurationBuilder;

public class ConfigProvider implements HelixConfigurationProvider {

    @Override
    public ConfigurationBuilder createBuilder() {
        return new SimpleConfigurationBuilder("root");
    }
}
