package tr.alperendemir.helix.plugins;

import tr.alperendemir.helix.api.semver.Version;

import java.util.Objects;

public final class PluginMetadata {
    private String originRepository;
    private String id;
    private Version version;

    public String originRepository() {
        return originRepository;
    }

    public String id() {
        return id;
    }

    public Version version() {
        return this.version;
    }

    public void setOriginRepository(String originRepository) {
        this.originRepository = originRepository;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PluginMetadata) obj;
        return Objects.equals(this.originRepository, that.originRepository) &&
                Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originRepository, id);
    }

    @Override
    public String toString() {
        return "PluginMetadata[" +
                "originRepository=" + originRepository + ", " +
                "id=" + id + ']';
    }

}
