package tr.alperendemir.helix.api.config.errors;

public record ExceptionConfigError(Throwable throwable) implements ConfigError {

    @Override
    public ConfigErrorType getErrorType() {
        return ConfigErrorType.NON_RECOVERABLE;
    }

    @Override
    public String getMessage() {
        return this.throwable.getMessage();
    }


    @Override
    public String toString() {
        return "ExceptionConfigError[" +
                "throwable=" + throwable +
                ']';
    }
}
