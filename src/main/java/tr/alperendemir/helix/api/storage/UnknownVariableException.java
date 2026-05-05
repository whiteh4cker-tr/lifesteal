package tr.alperendemir.helix.api.storage;

public class UnknownVariableException extends RuntimeException {

    private final String variableId;
    private final Class<?> variableType;

    public UnknownVariableException(String variableId, Class<?> variableType) {
        super();
        this.variableId = variableId;
        this.variableType = variableType;
    }

    public UnknownVariableException(Throwable cause, String variableId, Class<?> variableType) {
        super(cause);
        this.variableId = variableId;
        this.variableType = variableType;
    }

    public UnknownVariableException(String message, String variableId, Class<?> variableType) {
        super(message);
        this.variableId = variableId;
        this.variableType = variableType;
    }

    public UnknownVariableException(String message, Throwable cause, String variableId, Class<?> variableType) {
        super(message, cause);
        this.variableId = variableId;
        this.variableType = variableType;
    }

    public UnknownVariableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String variableId, Class<?> variableType) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.variableId = variableId;
        this.variableType = variableType;
    }
}
