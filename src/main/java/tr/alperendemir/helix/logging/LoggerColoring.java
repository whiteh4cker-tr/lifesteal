package tr.alperendemir.helix.logging;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class LoggerColoring {

    private static final Pattern RGB = Pattern.compile("<rgb:(\\d+):(\\d+):(\\d+)>");
    private static final Pattern ANSI = Pattern.compile("\033[^m]+m");

    private static final Map<String, String> COLOR_MAP = new LinkedHashMap<>();
    private static final Map<String, String> COLOR_CODES = new LinkedHashMap<>();

    static {
        // colors that use other colors
        COLOR_MAP.put("<gray>", "<rgb:45:55:79>");
        COLOR_MAP.put("<light:gray>", "<rgb:56:67:94>");
        COLOR_MAP.put("<lighter:gray>", "<rgb:72:82:107>");

        // All colors now
        COLOR_MAP.put("<reset>", "\033[0m");

        COLOR_MAP.put("<black>", "\033[0;30m");
        COLOR_MAP.put("<red>", "\033[0;31m");
        COLOR_MAP.put("<green>", "\033[0;32m");
        COLOR_MAP.put("<yellow>", "\033[0;33m");
        COLOR_MAP.put("<blue>", "\033[0;34m");
        COLOR_MAP.put("<purple>", "\033[0;35m");
        COLOR_MAP.put("<cyan>", "\033[0;36m");
        COLOR_MAP.put("<white>", "\033[0;37m");

        COLOR_MAP.put("<bold:black>", "\033[1;30m");
        COLOR_MAP.put("<bold:red>", "\033[1;31m");
        COLOR_MAP.put("<bold:green>", "\033[1;32m");
        COLOR_MAP.put("<bold:yellow>", "\033[1;33m");
        COLOR_MAP.put("<bold:blue>", "\033[1;34m");
        COLOR_MAP.put("<bold:purple>", "\033[1;35m");
        COLOR_MAP.put("<bold:cyan>", "\033[1;36m");
        COLOR_MAP.put("<bold:white>", "\033[1;37m");

        COLOR_MAP.put("<background:black>", "\033[7;30m");
        COLOR_MAP.put("<background:red>", "\033[7;31m");
        COLOR_MAP.put("<background:green>", "\033[7;32m");
        COLOR_MAP.put("<background:yellow>", "\033[7;33m");
        COLOR_MAP.put("<background:blue>", "\033[7;34m");
        COLOR_MAP.put("<background:purple>", "\033[7;35m");
        COLOR_MAP.put("<background:cyan>", "\033[7;36m");
        COLOR_MAP.put("<background:white>", "\033[7;37m");

        COLOR_MAP.put("<light:black>", "\033[0;90m");
        COLOR_MAP.put("<light:red>", "\033[0;91m");
        COLOR_MAP.put("<light:green>", "\033[0;92m");
        COLOR_MAP.put("<light:yellow>", "\033[0;93m");
        COLOR_MAP.put("<light:blue>", "\033[0;94m");
        COLOR_MAP.put("<light:purple>", "\033[0;95m");
        COLOR_MAP.put("<light:cyan>", "\033[0;96m");
        COLOR_MAP.put("<light:white>", "\033[0;97m");

        COLOR_MAP.put("<light:bold:black>", "\033[1;90m");
        COLOR_MAP.put("<light:bold:red>", "\033[1;91m");
        COLOR_MAP.put("<light:bold:green>", "\033[1;92m");
        COLOR_MAP.put("<light:bold:yellow>", "\033[1;93m");
        COLOR_MAP.put("<light:bold:blue>", "\033[1;94m");
        COLOR_MAP.put("<light:bold:purple>", "\033[1;95m");
        COLOR_MAP.put("<light:bold:cyan>", "\033[1;96m");
        COLOR_MAP.put("<light:bold:white>", "\033[1;97m");

        COLOR_MAP.put("<light:background:black>", "\033[0;100m");
        COLOR_MAP.put("<light:background:red>", "\033[0;101m");
        COLOR_MAP.put("<light:background:green>", "\033[0;102m");
        COLOR_MAP.put("<light:background:yellow>", "\033[0;103m");
        COLOR_MAP.put("<light:background:blue>", "\033[0;104m");
        COLOR_MAP.put("<light:background:purple>", "\033[0;105m");
        COLOR_MAP.put("<light:background:cyan>", "\033[0;106m");
        COLOR_MAP.put("<light:background:white>", "\033[0;107m");

        COLOR_CODES.put("\033[0;30m", "§0");
        COLOR_CODES.put("\033[0;31m", "§4");
        COLOR_CODES.put("\033[0;32m", "§2");
        COLOR_CODES.put("\033[0;33m", "§6");
        COLOR_CODES.put("\033[0;34m", "§1");
        COLOR_CODES.put("\033[0;35m", "§5");
        COLOR_CODES.put("\033[0;36m", "§3");
        COLOR_CODES.put("\033[0;37m", "§f");

        COLOR_CODES.put("\033[0;90m", "§8");
        COLOR_CODES.put("\033[0;91m", "§c");
        COLOR_CODES.put("\033[0;92m", "§a");
        COLOR_CODES.put("\033[0;93m", "§e");
        COLOR_CODES.put("\033[0;94m", "§9");
        COLOR_CODES.put("\033[0;95m", "§d");
        COLOR_CODES.put("\033[0;96m", "§b");
        COLOR_CODES.put("\033[0;97m", "§7");
    }

    public static String replaceColoring(String message) {
        var originalLen = message.length();
        for (var entry : COLOR_MAP.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }

        var matcher = RGB.matcher(message);
        if (matcher.find()) {
            message = matcher.replaceAll(m -> "\033[38;2;%s;%s;%sm".formatted(m.group(1), m.group(2), m.group(3)));
        }

        if (message.length() != originalLen) {
            message += "\033[0m"; // Reset coloring
        }

        return message;
    }

    public static String toColorCodes(String message) {
        message = replaceColoring(message);
        for (var entry : COLOR_CODES.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }

        var matcher = ANSI.matcher(message);
        if (matcher.find()) {
            message = matcher.replaceAll("");
        }

        return message;
    }

    public static String chatColor(String message) {
        return toColorCodes(replaceColoring(message));
    }

}
