package tr.alperendemir.helix.api.namespaced.exceptions;

public class InvalidFormatException extends RuntimeException {

    private boolean namespace;

    public InvalidFormatException(String message, boolean namespace) {
        super(message);

        this.namespace = namespace;
    }

    public boolean isNamespace() {
        return this.namespace;
    }
}
