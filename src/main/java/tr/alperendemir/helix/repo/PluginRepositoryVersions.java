package tr.alperendemir.helix.repo;

import tr.alperendemir.helix.api.semver.Version;
import tr.alperendemir.helix.api.semver.VersionExpression;
import tr.alperendemir.helix.utils.VersionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public final class PluginRepositoryVersions {

    private final @NotNull Map<Version, VersionExpression> supportedVersions;

    public PluginRepositoryVersions(Map<String, String> versionMap) {
        this.supportedVersions = createVersionMap(versionMap);
    }

    public Version checkSupported(@NotNull Version version) {
        for (var entry : this.supportedVersions.entrySet()) {
            if (version.satisfies(entry.getValue())) {
                return entry.getKey();
            }
        }

        return null;
    }

    private static Map<Version, VersionExpression> createVersionMap(Map<String, String> versionMap) {
        var versions = new TreeMap<Version, VersionExpression>((o1, o2) -> -o1.compareTo(o2));

        var current = VersionUtils.getMinecraftVersionSemver();

        for (var entry : versionMap.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();


            var check = VersionExpression.parse(value);
            if (check == null) {
                continue;
            }

            if (!current.satisfies(check)) {
                continue;
            }

            versions.put(Version.parse(key), check);

        }

        return versions;
    }

    public @NotNull Map<Version, VersionExpression> supportedVersions() {
        return supportedVersions;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PluginRepositoryVersions) obj;
        return Objects.equals(this.supportedVersions, that.supportedVersions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supportedVersions);
    }

    @Override
    public String toString() {
        return "PluginRepositoryVersions[" +
                "supportedVersions=" + supportedVersions + ']';
    }


}
