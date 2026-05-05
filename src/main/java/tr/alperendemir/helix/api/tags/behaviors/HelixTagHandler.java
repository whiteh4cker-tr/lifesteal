package tr.alperendemir.helix.api.tags.behaviors;

import tr.alperendemir.helix.api.storage.HelixDataStorage;

public interface HelixTagHandler<T extends HelixTagContext> {

    TagResult handle(T context, HelixDataStorage storage, HelixTagInstance instance);

}
