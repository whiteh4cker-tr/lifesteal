package tr.alperendemir.helix.api.config.components.values;

import tr.alperendemir.helix.api.config.parsing.TypeParser;

public interface ParsedValueConfigComponent<S, C> extends ValueConfigComponent<C> {
    
    TypeParser<S, C> parser();

    ParsedValueConfigComponent<S, C> parser(TypeParser<S, C> parser);
    
}
