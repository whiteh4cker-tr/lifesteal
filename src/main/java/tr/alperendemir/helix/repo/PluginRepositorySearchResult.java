package tr.alperendemir.helix.repo;

import java.util.Map;

public record PluginRepositorySearchResult(Map<PluginRepositoryEntry, Integer> entries, PluginRepositoryEntry best, int bestScore) {


}
