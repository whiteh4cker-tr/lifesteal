package tr.alperendemir.helix.api.reporting;

import tr.alperendemir.helix.api.logging.LoggerLevel;

public enum ErrorType {

    ERROR("<light:red>Error", "<light:red>Errors", LoggerLevel.ERROR),
    WARNING("<light:yellow>Warning", "<light:yellow>Warnings", LoggerLevel.WARNING),

    INTERNAL(null, null, null);

    private final String singular;
    private final String plural;
    private final LoggerLevel level;

    ErrorType(String singular, String plural, LoggerLevel level) {
        this.singular = singular;
        this.plural = plural;
        this.level = level;
    }

    public String getName(int count) {
        return count == 1 ? this.singular : this.plural;
    }

    public LoggerLevel getLevel() {
        return level;
    }
}
