package tr.alperendemir.helix.api.config.errors;

import java.util.Arrays;

public record ArrayConfigError(ConfigError[] childErrors) implements ConfigError {

    @Override
    public ConfigErrorType getErrorType() {
        return null;
    }

    @Override
    public String getMessage() {
        return null;
    }


    @Override
    public String toString() {
        return "ArrayConfigError[" +
                "childErrors=" + Arrays.toString(childErrors) +
                ']';
    }
}
