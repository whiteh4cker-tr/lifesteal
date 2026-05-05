package tr.alperendemir.helix.reporting;

import tr.alperendemir.helix.api.reporting.ErrorSession;
import tr.alperendemir.helix.api.reporting.ErrorType;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class HelixErrorSession implements ErrorSession {

    private final Map<String, ErrorType> errors = new LinkedHashMap<>();
    private final Map<String, ErrorType> errorView = Collections.unmodifiableMap(this.errors);

    private final int[] errorCounts = new int[ErrorType.INTERNAL.ordinal()];

    @Override
    public void reportError(String message, ErrorType type) {
        if (type == ErrorType.INTERNAL) {
            throw new IllegalArgumentException("INTERNAL may not be reported as an error!");
        }

        Objects.requireNonNull(message, "Message must not be null");
        Objects.requireNonNull(type, "Type must not be null");

        this.errors.put(message, type);
        this.errorCounts[type.ordinal()]++;
    }

    @Override
    public int errorCount(ErrorType type) {
        if (type == ErrorType.INTERNAL) {
            throw new IllegalArgumentException("INTERNAL has no report count!");
        }

        return this.errorCounts[type.ordinal()];
    }

    @Override
    public Map<String, ErrorType> getErrors() {
        return this.errorView;
    }

    public void clear() {
        Arrays.fill(this.errorCounts, 0);
        this.errors.clear();
    }
}
