package tr.alperendemir.helix.events.wrappers;

import tr.alperendemir.helix.events.WrapperEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerQuitWrapperEvent extends PlayerQuitEvent implements WrapperEvent {

    private final UUID worldId;

    public PlayerQuitWrapperEvent(Player playerJoined, String quitMessage, UUID worldId) {
        super(playerJoined, quitMessage);
        this.worldId = worldId;
    }

    public PlayerQuitWrapperEvent(PlayerQuitEvent event, UUID worldId) {
        this(event.getPlayer(), event.getQuitMessage(), worldId);
    }

    @Override
    public UUID getWorld() {
        return worldId;
    }
}
