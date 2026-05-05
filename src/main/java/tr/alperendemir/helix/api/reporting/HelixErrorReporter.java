package tr.alperendemir.helix.api.reporting;

import java.util.Map;

public interface HelixErrorReporter extends ErrorSession {

    void beginSession();

    ErrorSession getSession();

    void endSession();

    @Override
    default int errorCount(ErrorType type) {
        return getSession().errorCount(type);
    }

    @Override
    default Map<String, ErrorType> getErrors() {
        return getSession().getErrors();
    }

    @Override
    default void reportError(String message, ErrorType type) {
        getSession().reportError(message, type);
    }
}
