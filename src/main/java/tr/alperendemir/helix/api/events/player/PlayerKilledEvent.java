package tr.alperendemir.helix.api.events.player;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerKilledEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Entity directKiller;

    @Nullable
    private final Player killer;
    private boolean cancelled;

    public PlayerKilledEvent(Player who, Entity directKiller, @Nullable Player killer) {
        super(who);
        this.directKiller = directKiller;
        this.killer = killer;
    }

    public Entity getDirectKiller() {
        return directKiller;
    }

    public @Nullable Player getKiller() {
        return killer;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
