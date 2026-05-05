package tr.alperendemir.helix.listeners;

import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.events.ListenerRegistry;
import tr.alperendemir.helix.events.wrappers.PlayerChangedWorldWrapperEvent;
import tr.alperendemir.helix.events.wrappers.PlayerJoinWrapperEvent;
import tr.alperendemir.helix.events.wrappers.PlayerQuitWrapperEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;
import java.util.UUID;

public class PlayerMoveWorldsListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        var from = event.getFrom();
        var to = event.getTo();

        var fromWorld = getWorldId(from);
        var toWorld = getWorldId(to);

        var listenerRegistry = (ListenerRegistry) Helix.events();

        if (Objects.equals(fromWorld, toWorld)) {
            //TODO listenerRegistry.callEvent(event, fromWorld);
            return;
        }

        var player = event.getPlayer();

        var quit = new PlayerQuitWrapperEvent(player, "", fromWorld);
        var join = new PlayerJoinWrapperEvent(player, "", toWorld);

        var switchWorld = new PlayerChangedWorldWrapperEvent(player, from.getWorld());

        for (var plugin : Helix.pluginLoader().getPlugins().values()) {
            var allowFrom = plugin.isWorldAllowed(fromWorld);
            var allowTo = plugin.isWorldAllowed(toWorld);

            if (allowFrom) {
                if (!allowTo) {
                    listenerRegistry.callEvent(plugin, quit);
                    continue;
                }

                listenerRegistry.callEvent(plugin, switchWorld);
            }

            if (!allowFrom && allowTo) {
                listenerRegistry.callEvent(plugin, join);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        event.getPlayer().updateCommands();
    }

    private UUID getWorldId(Location location) {
        if (location == null) return null;

        var world = location.getWorld();
        return world == null ? null : world.getUID();
    }
}
