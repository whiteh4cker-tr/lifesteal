package tr.alperendemir.helix.api.config.components.arrays;

import tr.alperendemir.helix.api.config.parsing.TypeParser;

public interface ParsedArrayConfigComponent<S, C> extends ArrayConfigComponent<C> {

    TypeParser<S, C> parser();

    ParsedArrayConfigComponent<S, C> parser(TypeParser<S, C> parser);

}
