package tr.alperendemir.helix.listeners.custom;

import tr.alperendemir.helix.api.events.player.PlayerKilledEvent;
import tr.alperendemir.helix.utils.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDamageListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) != 0) {
            return; // Ignore if the entity is blocking
        }

        var attacked = event.getEntity();
        if (!(attacked instanceof Player killedPlayer)) return;

        if (killedPlayer.getHealth() - event.getFinalDamage() > 0) return;

        var directAttacker = event instanceof EntityDamageByEntityEvent entityEvent ? entityEvent.getDamager() : null;
        var killer = EntityUtils.findRootPlayer(directAttacker);

        var killedEvent = new PlayerKilledEvent(killedPlayer, directAttacker, killer);
        Bukkit.getPluginManager().callEvent(killedEvent);

        if (killedEvent.isCancelled()) {
            event.setCancelled(true);
            event.setDamage(0);
        }
    }


}
