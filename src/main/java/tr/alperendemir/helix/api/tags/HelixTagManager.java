package tr.alperendemir.helix.api.tags;

import tr.alperendemir.helix.api.tags.behaviors.HelixTagContext;
import tr.alperendemir.helix.api.tags.behaviors.TagBehavior;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

public interface HelixTagManager {

    /**
     * Creates or retrieves a tag from an ID, this tag should be shared across all plugins as it allows for data exchange.
     * A tag can be used to indicate the state of an entity and applying behavior on that entity without having to rely on event listeners.
     *
     * @param id The id of the tag
     * @return A new or already existing tag.
     * */
    @NotNull HelixTag get(@NotNull String id);

    @NotNull
    Set<String> listAllKeys();

    @NotNull
    Collection<HelixTag> listAllTags();

    <T extends HelixTagContext> void fire(@NotNull TagBehavior<T> behavior, T context);

}
