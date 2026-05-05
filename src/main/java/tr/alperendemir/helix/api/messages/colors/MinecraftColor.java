package tr.alperendemir.helix.api.messages.colors;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public final class MinecraftColor {

    private static final Map<String, MinecraftColor> COLORS_MAP = new HashMap<>();
    private static final Collection<MinecraftColor> COLOR_LIST = Collections.unmodifiableCollection(COLORS_MAP.values());
    private static final Pattern REPLACE_PATTERN = Pattern.compile("§[0123456789abcdefklmnor]|§x(?:§[a-fA-F]){6}");

    public static final char COLOR_CHAR = '§';

    public static final MinecraftColor BLACK         = new MinecraftColor("0", 0x000000_00);
    public static final MinecraftColor DARK_BLUE     = new MinecraftColor("1", 0x0000AA_FF);
    public static final MinecraftColor DARK_GREEN    = new MinecraftColor("2", 0x00AA00_FF);
    public static final MinecraftColor DARK_AQUA     = new MinecraftColor("3", 0x00AAAA_FF);
    public static final MinecraftColor DARK_RED      = new MinecraftColor("4", 0xAA0000_FF);
    public static final MinecraftColor DARK_PURPLE   = new MinecraftColor("5", 0xAA00AA_FF);
    public static final MinecraftColor GOLD          = new MinecraftColor("6", 0xFFAA00_FF);
    public static final MinecraftColor GRAY          = new MinecraftColor("7", 0xAAAAAA_FF);
    public static final MinecraftColor DARK_GRAY     = new MinecraftColor("8", 0x555555_FF);
    public static final MinecraftColor BLUE          = new MinecraftColor("9", 0x5555FF_FF);
    public static final MinecraftColor GREEN         = new MinecraftColor("a", 0x55FF55_FF);
    public static final MinecraftColor AQUA          = new MinecraftColor("b", 0x55FFFF_FF);
    public static final MinecraftColor RED           = new MinecraftColor("c", 0xFF5555_FF);
    public static final MinecraftColor LIGHT_PURPLE  = new MinecraftColor("d", 0xFF55FF_FF);
    public static final MinecraftColor YELLOW        = new MinecraftColor("e", 0xFFFF55_FF);
    public static final MinecraftColor WHITE         = new MinecraftColor("f", 0xFFFFFF_FF);

    public static final MinecraftColor MAGIC         = new MinecraftColor("k");
    public static final MinecraftColor BOLD          = new MinecraftColor("l");
    public static final MinecraftColor STRIKETHROUGH = new MinecraftColor("m");
    public static final MinecraftColor UNDERLINE     = new MinecraftColor("n");
    public static final MinecraftColor ITALIC        = new MinecraftColor("o");
    public static final MinecraftColor RESET         = new MinecraftColor("r");

    private final String color;
    private final int hexColor;

    private MinecraftColor(final String color, final int hexColor, boolean cache) {
        this.color = COLOR_CHAR + color;
        this.hexColor = hexColor;

        if (cache) {
            COLORS_MAP.put(this.color, this);
        }
    }

    private MinecraftColor(final String color, final int hexColor) {
        this(color, hexColor, true);
    }

    private MinecraftColor(final String color) {
        this(color, -1);
    }

    public static MinecraftColor fromRGB(int rgb) {
        var hexString = Integer.toHexString(rgb & 0xFFFFFF);
        var fullHexString = hexString.length() == 6 ? hexString.substring(0, 6) : hexString + "0".repeat(6 - hexString.length());

        var coloredHexString = COLOR_CHAR + "x" + fullHexString.replaceAll("(.)", COLOR_CHAR + "$1");

        return new MinecraftColor(coloredHexString, rgb, false);
    }

    public static MinecraftColor fromHex(String hex) {
        if (hex.charAt(0) != '#') return MinecraftColor.WHITE;

        return fromRGB(Integer.parseInt(hex.substring(1), 16));
    }

    public String getColor() {
        return this.color;
    }

    public int getHexColor() {
        return this.hexColor;
    }

    public boolean isHex() {
        return this.color.length() > 2;
    }

    public char getChar() {
        if (this.isHex()) {
            return 'x';
        }

        return this.color.charAt(1);
    }

    public boolean isFormat() {
        return this.hexColor < 0;
    }

    public boolean isColor() {
        return !this.isFormat() && this.getChar() != 'r';
    }

    public static MinecraftColor getByChar(final char ch) {
        return COLORS_MAP.get(Character.toString(ch));
    }

    public static MinecraftColor getByColor(final String color) {
        return COLORS_MAP.get(color);
    }

    @Contract("!null -> !null; null -> null")
    @Nullable
    public static String stripColor(@Nullable final String color) {
        if (color == null) return null;

        return REPLACE_PATTERN.matcher(color).replaceAll("");
    }

    public static String replaceColorCodes(char altChar, @NotNull String toReplace) {
        return REPLACE_PATTERN.matcher(toReplace).replaceAll(matchResult -> matchResult.group().replace(altChar, COLOR_CHAR));
    }

    public static Collection<MinecraftColor> allColors() {
        return COLOR_LIST;
    }

    @Override
    public String toString() {
        return this.color;
    }
}
