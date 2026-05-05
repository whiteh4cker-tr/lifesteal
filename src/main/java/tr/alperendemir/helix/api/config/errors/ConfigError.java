package tr.alperendemir.helix.api.config.errors;

public interface ConfigError {

    ConfigErrorType getErrorType();

    String getMessage();
}
