package tr.alperendemir.helix.api.config.components.values;

import tr.alperendemir.helix.api.config.ConfigComponent;

public interface ValueConfigComponent<T> extends ConfigComponent {

    T defaultValue();

    T value();

    void value(T value);

    Class<T> type();
}
