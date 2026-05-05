package tr.alperendemir.helix.api.config.components.values;

import tr.alperendemir.helix.api.config.ConfigComponent;
import tr.alperendemir.helix.api.config.parsing.CompoundTypeParser;

public interface CompoundConfigComponent<T> extends ConfigComponent {

    CompoundTypeParser<T> parser();

    CompoundConfigComponent<T> parser(CompoundTypeParser<T> parser);

    T defaultValue();

    T value();

    CompoundConfigComponent<T> value(T value);

    Class<T> type();

}
