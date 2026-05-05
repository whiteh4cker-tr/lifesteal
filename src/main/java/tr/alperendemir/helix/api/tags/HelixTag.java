package tr.alperendemir.helix.api.tags;

import tr.alperendemir.helix.api.namespaced.UniqueIdentifier;
import tr.alperendemir.helix.api.storage.HelixDataStorage;
import tr.alperendemir.helix.api.tags.behaviors.HelixTagContext;
import tr.alperendemir.helix.api.tags.behaviors.HelixTagHandler;
import tr.alperendemir.helix.api.tags.behaviors.TagBehavior;

import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface HelixTag {

    void add(UUID uuid, BiConsumer<UUID, HelixDataStorage> storage);

    void remove(UUID uuid);

    boolean has(UUID uuid);

    <T extends HelixTagContext> void on(TagBehavior<T> behavior, UniqueIdentifier id, HelixTagHandler<T> handler);

    <T extends HelixTagContext> void fire(TagBehavior<T> behavior, T context);

    HelixDataStorage getData(UUID uuid);

    void editData(UUID uuid, Consumer<HelixDataStorage> consumer);

    Set<UUID> listAll();

    void clearAll();

}
