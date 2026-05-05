package tr.alperendemir.helix.utils;

import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class EntityUtils {

    private EntityUtils() {

    }

    public static Player findRootPlayer(Entity entity) {
        if (entity == null) return null;

        if (entity instanceof Projectile arrow && (arrow.getShooter() instanceof Entity e))
            return EntityUtils.findRootPlayer(e);

        if (entity instanceof TNTPrimed primed) {
            while (primed.getSource() instanceof TNTPrimed tntPrimed)
                primed = tntPrimed;

            return EntityUtils.findRootPlayer(primed.getSource());
        }

        if (entity instanceof EnderCrystal crystal) {
            var cause = crystal.getLastDamageCause();
            if (cause instanceof EntityDamageByEntityEvent event) {
                return EntityUtils.findRootPlayer(event.getDamager());
            }
        }

        if (entity instanceof Tameable tameable) {
            var owner = tameable.getOwner();
            if (owner instanceof Entity e) {
                return EntityUtils.findRootPlayer(e);
            }
        }

        return entity instanceof Player player ? player : null;
    }

}
