package tr.alperendemir.helix.api.config.errors;

import java.util.Map;

public record TreeConfigError(Map<String, ConfigError> childErrors) implements ConfigError {

    @Override
    public ConfigErrorType getErrorType() {
        return null;
    }

    @Override
    public String getMessage() {
        return null;
    }

}
