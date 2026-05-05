package tr.alperendemir.helix.tags;

import tr.alperendemir.helix.api.tags.behaviors.HelixTagInstance;

import java.util.UUID;

public class BukkitHelixTagInstance implements HelixTagInstance {

    private final UUID uuid;
    private boolean removed;

    public BukkitHelixTagInstance(UUID uuid) {
        this.uuid = uuid;
    }


    @Override
    public UUID attachedTo() {
        return this.uuid;
    }

    @Override
    public void remove() {
        this.removed = true;
    }

    public boolean isRemoved() {
        return this.removed;
    }
}
