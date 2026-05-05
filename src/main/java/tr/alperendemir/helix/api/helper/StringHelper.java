package tr.alperendemir.helix.api.helper;

@SuppressWarnings("unused")
public final class StringHelper {

    private StringHelper() {

    }

    public static String trimLength(String str, int length) {
        if (str == null) {
            return null;
        }

        if (str.length() <= length) {
            return str;
        }

        return str.substring(0, length);
    }

    public static String trimLengthContinued(String str, int length) {
        if (str == null) {
            return null;
        }

        var cutLength = Math.max(length - 3, 1);

        if (str.length() <= cutLength) {
            return str;
        }

        return str.substring(0, cutLength) + "...";
    }

}
