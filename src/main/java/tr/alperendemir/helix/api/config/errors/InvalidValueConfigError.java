package tr.alperendemir.helix.api.config.errors;

import info.debatty.java.stringsimilarity.LongestCommonSubsequence;

import java.util.Locale;

public class InvalidValueConfigError implements ConfigError {

    private static final LongestCommonSubsequence LCS = new LongestCommonSubsequence();

    private final Object expected;
    private final Object received;
    private final Object suggestion;

    public InvalidValueConfigError(Object expected, Object received, Object suggestion) {
        this.expected = expected;
        this.received = received;
        this.suggestion = suggestion;
    }

    public InvalidValueConfigError(Object expected, Object received) {
        this(expected, received, null);
    }

    public static <T> T getSuggestion(Class<T> type, Object value) {
        if (!type.isEnum()) {
            return null;
        }

        var bestScore = Double.MAX_VALUE;
        Object bestValue = null;

        var strValue = String.valueOf(value).replaceAll(" +", "_").toLowerCase(Locale.ROOT);

        for (var field : type.getDeclaredFields()) {
            if (field.isEnumConstant() && !field.isAnnotationPresent(Deprecated.class)) {
                var name = field.getName().toLowerCase(Locale.ROOT);

                var score = LCS.distance(strValue, name) - (name.contains(strValue) ? name.length() / 2D : 0);
                if (score < bestScore) {
                    bestScore = score;
                    try {
                        bestValue = field.get(null);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return bestValue == null ? null : type.cast(bestValue);
    }

    @Override
    public ConfigErrorType getErrorType() {
        return ConfigErrorType.RECOVERABLE;
    }

    @Override
    public String getMessage() {
        return "<light:yellow>Received invalid value <light:red>%s <light:yellow>using default value: <light:green>%s <light:yellow>(Did you mean <light:cyan>%s<light:yellow>?)".formatted(
                this.received,
                this.expected,
                this.suggestion
        );
    }

    public Object getExpected() {
        return this.expected;
    }

    public Object getReceived() {
        return this.received;
    }

    public Object getSuggestion() {
        return this.suggestion;
    }

    @Override
    public String toString() {
        return "InvalidValueConfigError[" +
                "expected=" + getExpected() +
                ", received=" + getReceived() +
                ", suggestion=" + getSuggestion() +
                ']';
    }
}
