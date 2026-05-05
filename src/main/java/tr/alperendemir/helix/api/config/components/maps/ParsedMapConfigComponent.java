package tr.alperendemir.helix.api.config.components.maps;

import tr.alperendemir.helix.api.config.parsing.TypeParser;

public interface ParsedMapConfigComponent<S, C> extends MapConfigComponent<C> {

    TypeParser<S, C> parser();

    ParsedMapConfigComponent<S, C> parser(TypeParser<S, C> parser);

}
