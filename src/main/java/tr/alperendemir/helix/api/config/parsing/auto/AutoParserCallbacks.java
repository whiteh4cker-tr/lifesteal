package tr.alperendemir.helix.api.config.parsing.auto;

public interface AutoParserCallbacks {

    /**
     * Allows you to see and edit what a field is being set to when deserializing the type.
     * @param fieldName The name of the field, as it is in the config;
     *                  This is usually the field's actual name or the name defined in {@link ParserTarget}
     * @param fieldValue The value that is being set to the field
     * @return The value to set, can be anything as long as it is of the same type as the field.
     * @see ParserTarget
     * */
    Object fieldLoaded(Class<?> fieldType, String fieldName, Object fieldValue);

    /**
     * Allows you to run code once the {@link AutoParser} is finished setting up the object.
     * @see AutoParser
     * */
    void autoParserComplete();

}
