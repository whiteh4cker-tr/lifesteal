package tr.alperendemir.helix.api.tags.behaviors.join.async;

import tr.alperendemir.helix.api.tags.behaviors.HelixTagContext;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.util.Objects;
import java.util.UUID;

public final class AsyncJoinTagContext implements HelixTagContext {
    private final UUID player;
    private final InetAddress address;
    private String kickMessage;

    public AsyncJoinTagContext(UUID player, InetAddress address) {
        this.player = player;
        this.address = address;
    }

    @Override
    public @Nullable World world() {
        return null;
    }

    public UUID player() {
        return player;
    }

    public InetAddress address() {
        return address;
    }

    public void kick(@NotNull String kickMessage) {
        this.kickMessage = kickMessage;
    }

    public String getKickMessage() {
        return this.kickMessage;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AsyncJoinTagContext) obj;
        return Objects.equals(this.player, that.player) &&
                Objects.equals(this.address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, address);
    }

    @Override
    public String toString() {
        return "AsyncJoinTagContext[" +
                "player=" + player + ", " +
                "address=" + address + ']';
    }


}
