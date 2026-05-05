package tr.alperendemir.helix.events.wrappers;

import tr.alperendemir.helix.events.WrapperEvent;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.UUID;

public class PlayerChangedWorldWrapperEvent extends PlayerChangedWorldEvent implements WrapperEvent {
    public PlayerChangedWorldWrapperEvent(Player player, World from) {
        super(player, from);
    }

    @SuppressWarnings("unused")
    public PlayerChangedWorldWrapperEvent(PlayerChangedWorldEvent event, UUID unused) {
        this(event.getPlayer(), event.getFrom());
    }

    @Override
    public UUID getWorld() {
        return getFrom().getUID();
    }
}
