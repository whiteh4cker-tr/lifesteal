package tr.alperendemir.helix.api.config.components.maps;

import tr.alperendemir.helix.api.config.ConfigComponent;

import java.util.Map;

public interface MapConfigComponent<T> extends ConfigComponent {

    Map<String, T> defaultValues();

    Map<String, T> values();

    Class<T> type();


}
