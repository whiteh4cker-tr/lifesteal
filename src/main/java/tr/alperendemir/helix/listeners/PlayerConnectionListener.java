package tr.alperendemir.helix.listeners;

import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.tags.behaviors.TagBehaviors;
import tr.alperendemir.helix.api.tags.behaviors.join.JoinTagContext;
import tr.alperendemir.helix.api.tags.behaviors.join.async.AsyncJoinTagContext;
import tr.alperendemir.helix.api.tags.behaviors.quit.QuitTagContext;
import tr.alperendemir.helix.events.ListenerRegistry;
import tr.alperendemir.helix.events.wrappers.PlayerJoinWrapperEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        var world = player.getWorld();
        var join = new PlayerJoinWrapperEvent(player, event.getJoinMessage(), world.getUID());

        var listenerRegistry = (ListenerRegistry) Helix.events();
        listenerRegistry.broadcast(join);

        Helix.tags().fire(TagBehaviors.JOIN, new JoinTagContext(world, player));
    }

    @EventHandler
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        var player = event.getUniqueId();

        var ctx = new AsyncJoinTagContext(player, event.getAddress());
        Helix.tags().fire(TagBehaviors.ASYNC_JOIN, ctx);

        if (ctx.getKickMessage() != null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ctx.getKickMessage());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        var world = player.getWorld();
        var join = new PlayerJoinWrapperEvent(player, event.getQuitMessage(), world.getUID());

        var listenerRegistry = (ListenerRegistry) Helix.events();
        listenerRegistry.broadcast(join);

        Helix.tags().fire(TagBehaviors.QUIT, new QuitTagContext(world, player));
    }
}
