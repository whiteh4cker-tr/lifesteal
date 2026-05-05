package tr.alperendemir.helix.events.wrappers;

import tr.alperendemir.helix.events.WrapperEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoinWrapperEvent extends PlayerJoinEvent implements WrapperEvent {

    private final UUID worldId;

    public PlayerJoinWrapperEvent(Player playerJoined, String joinMessage, UUID worldId) {
        super(playerJoined, joinMessage);
        this.worldId = worldId;
    }

    public PlayerJoinWrapperEvent(PlayerJoinEvent event, UUID worldId) {
        this(event.getPlayer(), event.getJoinMessage(), worldId);
    }

    @Override
    public UUID getWorld() {
        return worldId;
    }
}
