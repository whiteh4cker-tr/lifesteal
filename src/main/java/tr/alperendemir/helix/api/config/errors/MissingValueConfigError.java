package tr.alperendemir.helix.api.config.errors;

public class MissingValueConfigError implements ConfigError {

    public MissingValueConfigError() {

    }

    @Override
    public ConfigErrorType getErrorType() {
        return ConfigErrorType.RECOVERABLE;
    }

    @Override
    public String getMessage() {
        return null;
    }
}
