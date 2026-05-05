package tr.alperendemir.helix.tags;

import tr.alperendemir.helix.api.tags.HelixTag;
import tr.alperendemir.helix.api.tags.HelixTagManager;
import tr.alperendemir.helix.api.tags.behaviors.HelixTagContext;
import tr.alperendemir.helix.api.tags.behaviors.TagBehavior;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BukkitHelixTagManager implements HelixTagManager {

    private final Map<String, HelixTag> tags = new HashMap<>();
    private final Set<String> tagNames = Collections.unmodifiableSet(this.tags.keySet());
    private final Collection<HelixTag> tagList = Collections.unmodifiableCollection(this.tags.values());

    @Override
    public @NotNull HelixTag get(@NotNull String id) {
        return this.tags.computeIfAbsent(id, BukkitHelixTag::new);
    }

    @Override
    public @NotNull Set<String> listAllKeys() {
        return this.tagNames;
    }

    @Override
    public @NotNull Collection<HelixTag> listAllTags() {
        return this.tagList;
    }

    @Override
    public <T extends HelixTagContext> void fire(@NotNull TagBehavior<T> behavior, T context) {
        for (var tag : this.tagList) {
            tag.fire(behavior, context);
        }
    }
}
