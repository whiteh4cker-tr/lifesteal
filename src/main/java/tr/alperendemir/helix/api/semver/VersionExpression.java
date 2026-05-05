package tr.alperendemir.helix.api.semver;

public record VersionExpression(VersionCompareOperator operator, Version version) {

    public static VersionExpression parse(String expression) {
        var trimmed = expression.trim();
        if (trimmed.isEmpty()) return null;

        var pattern = VersionCompareOperator.getMatchPattern();

        var matcher = pattern.matcher(trimmed);

        String versionOperatorString;
        int substringOffset;
        if (!matcher.find()) {
            versionOperatorString = VersionCompareOperator.EQUAL.getOperator();
            substringOffset = 0;
        } else {
            versionOperatorString = matcher.group(1);
            substringOffset = matcher.end(1);
        }

        var versionOperator = VersionCompareOperator.fromString(versionOperatorString);
        var versionString = trimmed.substring(substringOffset);
        var version = Version.parse(versionString);

        return new VersionExpression(versionOperator, version);
    }

    @Override
    public String toString() {
        return this.operator.getOperator() + this.version.toString();
    }
}
