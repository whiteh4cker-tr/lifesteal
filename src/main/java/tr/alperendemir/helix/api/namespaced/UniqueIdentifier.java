package tr.alperendemir.helix.api.namespaced;

import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.namespaced.exceptions.InvalidFormatException;
import tr.alperendemir.helix.api.plugins.HelixPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Provides a container for <strong>namespace:key</strong> identifiers.
 * These identifiers are used throughout the library to uniquely identify resources,
 * both for internal (i.e. helix plugins) and external (i.e. vanilla, mods, non-helix plugins) entries.
 *
 * <p>1. Identifiers must be fully lowercase.</p>
 * <p>2. Namespaces may contain the following: a-z . _ -</p>
 * <p>3. Keys may contain the following: a-z . _ - /</p>
 */
public record UniqueIdentifier(String namespace, String key) {

    /**
     * Separator character used to separate namespace and key.
     */
    public static final String SEPARATOR = ":";

    /**
     * Namespace used to represent the vanilla namespace.
     */
    public static final String MINECRAFT_NAMESPACE = "minecraft";

    /**
     * Namespace used to represent helix data.
     */
    public static final String HELIX_NAMESPACE = "helix";

    @ApiStatus.Internal
    public UniqueIdentifier {
        var stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length >= 3) {
            if (!stackTrace[2].getClassName().startsWith("tr.alperendemir.helix")) {
                throw new RuntimeException(new IllegalAccessException());
            }
        }

        if (namespace.isEmpty() || key.isEmpty()) {
            throw new IndexOutOfBoundsException(toString());
        }

        validNamespaceOrThrow(namespace);
        validKeyOrThrow(key);
    }

    @NotNull
    @Override
    public String namespace() {
        return this.namespace;
    }

    @NotNull
    @Override
    public String key() {
        return this.key;
    }

    @Nullable
    public HelixPlugin namespaceAsPlugin() {
        var helixPlugin = Helix.pluginLoader().getPlugin(this.namespace);
        if (helixPlugin != null) {
            return helixPlugin;
        }

        var plugins = org.bukkit.Bukkit.getPluginManager().getPlugins();
        for (var plugin : plugins) {
            if (plugin.getName().equalsIgnoreCase(this.namespace)) {
                return tr.alperendemir.helix.plugins.FallbackHelixPlugin.of(plugin);
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return namespace() + SEPARATOR + key();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UniqueIdentifier that)) return false;
        return Objects.equals(key, that.key) && Objects.equals(namespace, that.namespace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, key);
    }

    @NotNull
    public static UniqueIdentifier minecraft(@NotNull String key) {
        Objects.requireNonNull(key, "Unable to create minecraft identifier from null key.");
        return new UniqueIdentifier(MINECRAFT_NAMESPACE, key);
    }

    @ApiStatus.Internal
    @NotNull
    public static UniqueIdentifier helix(@NotNull String key) {
        Objects.requireNonNull(key, "Unable to create helix identifier from null key.");
        return new UniqueIdentifier(HELIX_NAMESPACE, key);
    }

    @NotNull
    public static UniqueIdentifier plugin(@NotNull HelixPlugin plugin, @NotNull String key) {
        Objects.requireNonNull(key, "Unable to create plugin identifier from null key.");
        Objects.requireNonNull(plugin, "Unable to create plugin identifier from null plugin.");
        return new UniqueIdentifier(plugin.getId(), key);
    }

    public static UniqueIdentifier combine(@NotNull String namespace, @NotNull String key) {
        Objects.requireNonNull(key, "Unable to create combination identifier from null key.");
        Objects.requireNonNull(namespace, "Unable to create combination identifier from null namespace.");

        if (namespace.equals(HELIX_NAMESPACE)) {
            throw new IllegalArgumentException(namespace + SEPARATOR + key);
        }

        return new UniqueIdentifier(namespace, key);
    }

    /**
     * Attempts to parse a key to an instance of {@link UniqueIdentifier}, it may fail in the following scenarios:
     *
     * <p>1. The key is an invalid key; If it contains invalid characters, is empty or is malformed.</p>
     * <p>2. The key has the namespace <strong>helix</strong>; This is a reserved namespace.</p>
     * <p>3. The key is longer than 255 characters.</p>
     * <p>4. The key is null.</p>
     *
     * @param string The string to parse, must not be null.
     * @return A non-null unique identifier, if the string does not contain {@link #SEPARATOR} then {@link #MINECRAFT_NAMESPACE} is used as the namespace.
     * @throws InvalidFormatException    If the key has an invalid format.
     * @throws IllegalArgumentException  If the key uses the helix namespace.
     * @throws IndexOutOfBoundsException If the key is longer than 255 characters.
     * @throws NullPointerException      If the key is null.
     */
    @NotNull
    public static UniqueIdentifier parse(@NotNull String string) {
        Objects.requireNonNull(string, "Unable to parse a null string");

        if (string.length() > 255) {
            throw new IndexOutOfBoundsException(string);
        }

        var split = string.split(SEPARATOR);
        if (split.length == 1) {
            return minecraft(string);
        }

        var namespace = split[0];
        var key = split[1];

        if (namespace.equals(HELIX_NAMESPACE)) {
            throw new IllegalArgumentException(string);
        }

        return combine(namespace, key);
    }

    public static void validNamespaceOrThrow(@NotNull String string) throws InvalidFormatException {
        var error = isValidNamespace(string);

        if (error != -1) {
            throw new InvalidFormatException("Invalid namespace character: '" + string + "' in namespace: " + string, true);
        }
    }

    public static void validKeyOrThrow(@NotNull String string) throws InvalidFormatException {
        var error = isValidKey(string);

        if (error != -1) {
            throw new InvalidFormatException("Invalid key character: '" + string + "' in key: " + string, false);
        }
    }

    /**
     * Checks if the provided string is a valid namespace.
     *
     * @return {@code -1} if the namespace is valid, otherwise returns the index of the invalid character.
     */
    public static int isValidNamespace(@NotNull String string) {
        char[] charArray = string.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            var ch = charArray[i];
            if (!isValidNamespaceCharacter(ch)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Checks if the provided string is a valid key.
     *
     * @return {@code -1} if the key is valid, otherwise returns the index of the invalid character.
     */
    public static int isValidKey(@NotNull String string) {
        char[] charArray = string.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            var ch = charArray[i];
            if (!isValidKeyCharacter(ch)) {
                return i;
            }
        }

        return -1;
    }

    public static boolean isValidNamespaceCharacter(char c) {
        return (c >= 'a' && c <= 'z') || c == '.' || c == '_' || c == '-';
    }

    public static boolean isValidKeyCharacter(char c) {
        return isValidNamespaceCharacter(c) || c == '/';
    }

}
