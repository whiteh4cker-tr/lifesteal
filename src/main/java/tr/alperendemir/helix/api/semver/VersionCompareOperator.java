package tr.alperendemir.helix.api.semver;

import java.util.regex.Pattern;

public enum VersionCompareOperator {

    GREATER_THAN(">") {
        @Override
        public boolean compare(Version version1, Version version2) {
            if (version1.major() > version2.major()) {
                return true;
            }

            if (version1.major() != version2.major()) {
                return false;
            }

            if (version1.minor() > version2.minor()) {
                return true;
            }

            if (version1.minor() != version2.minor()) {
                return false;
            }

            return version1.patch() > version2.patch();
        }
    },
    GREATER_EQUAL(">=") {
        @Override
        public boolean compare(Version version1, Version version2) {
            return EQUAL.compare(version1, version2) || GREATER_THAN.compare(version1, version2);
        }
    },
    LESS_THAN("<") {
        @Override
        public boolean compare(Version version1, Version version2) {
            return !GREATER_THAN.compare(version1, version2) && NOT_EQUAL.compare(version1, version2);
        }
    },
    LESS_EQUAL("<=") {
        @Override
        public boolean compare(Version version1, Version version2) {
            return EQUAL.compare(version1, version2) || !GREATER_THAN.compare(version1, version2);
        }
    },
    EQUAL("==") {
        @Override
        public boolean compare(Version version1, Version version2) {
            return version1.major() == version2.major() && version1.patch() == version2.patch() && version1.minor() == version2.minor();
        }
    },
    NOT_EQUAL("!=") {
        @Override
        public boolean compare(Version version1, Version version2) {
            return !EQUAL.compare(version1, version2);
        }
    };

    private static Pattern MATCH_PATTERN;

    private final String operator;

    VersionCompareOperator(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return this.operator;
    }

    public static Pattern getMatchPattern() {
        if (MATCH_PATTERN == null) {
            var sb = new StringBuilder("^(");
            for (var value : values()) {
                sb.append(value.getOperator()).append('|');
            }
            sb.setCharAt(sb.length() - 1, ')');
            MATCH_PATTERN = Pattern.compile(sb.toString());
        }

        return MATCH_PATTERN;
    }

    public static VersionCompareOperator fromString(String operator) {
        for (var value : values()) {
            if (value.getOperator().equals(operator)) {
                return value;
            }
        }

        return null;
    }

    public abstract boolean compare(Version version1, Version version2);

}
