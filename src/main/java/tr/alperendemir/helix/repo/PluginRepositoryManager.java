package tr.alperendemir.helix.repo;

import tr.alperendemir.helix.api.logging.HelixLogger;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class PluginRepositoryManager {

    private final Map<String, PluginRepository> repositories = new HashMap<>();
    private final Map<String, PluginRepository> repositoryView = Collections.unmodifiableMap(this.repositories);
    private final File pluginFolder;

    public PluginRepositoryManager(File pluginFolder) {
        this.pluginFolder = pluginFolder;
    }

    public void addRepositories(Map<String, String> urls) {
        this.repositories.clear();
    }

    public void cleanRepositories() {
        this.repositories.clear();
    }

    public void refreshListings(BiConsumer<String, String> repositoryFailedConsumer) {
        // Intentionally disabled: no update checking.
    }

    public void refreshListings() {
        // Intentionally disabled: no update checking.
    }

    public Map<String, PluginRepositorySearchResult> searchRepositories(String entry) {
        return Collections.emptyMap();
    }

    public int getPluginRepositoryCount() {
        return 0;
    }

    public Map<String, PluginRepository> getRepositories() {
        return Collections.emptyMap();
    }

    public File downloadPlugin(String repoUrl, String entry, RepoDownloadCallback callback) {
        if (callback != null) {
            callback.onFailure();
        }
        return null;
    }


}
