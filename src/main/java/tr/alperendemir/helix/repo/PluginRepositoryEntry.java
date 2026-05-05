package tr.alperendemir.helix.repo;

import com.google.gson.JsonObject;
import tr.alperendemir.helix.api.semver.Version;
import tr.alperendemir.helix.utils.JsonUtils;
import tr.alperendemir.helix.utils.VersionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

public record PluginRepositoryEntry(
        String resourceId,
        String description,
        String displayName,
        List<String> authors,

        Date lastReleaseDate,
        Date firstReleaseDate,

        Version requestedVersion,
        Version latestVersion,

        long resourceSize,

        PluginRepositoryVersions versions
) {

    public static PluginRepositoryEntry fromJson(InputStream stream) throws IOException {
        return fromJson(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
    }

    public static PluginRepositoryEntry fromJson(String json) {
        var tree = JsonUtils.GSON.fromJson(json, JsonObject.class);
        return fromJson(tree);
    }

    public static PluginRepositoryEntry fromJson(JsonObject tree) {
        return JsonUtils.GSON.fromJson(tree, PluginRepositoryEntry.class);
    }

    public Version highestSupportedVersion() {
        return this.versions.checkSupported(VersionUtils.getMinecraftVersionSemver());
    }

    public boolean isSupported() {
        return this.highestSupportedVersion() != null;
    }

    public String authorString() {
        return String.join(", ", this.authors());
    }

}
