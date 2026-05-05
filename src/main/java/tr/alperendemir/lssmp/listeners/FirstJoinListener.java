package tr.alperendemir.lssmp.listeners;

import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.config.Configuration;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class FirstJoinListener implements Listener {

    private final Configuration generalConfig;
    private final NamespacedKey initKey;

    public FirstJoinListener(Configuration generalConfig, NamespacedKey initKey) {
        this.generalConfig = generalConfig;
        this.initKey = initKey;
    }

    @EventHandler(ignoreCancelled = true)
    @SuppressWarnings("deprecation")
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();

        var oldKey = new NamespacedKey("lifesteal-smp-plugin", "eliminated_player");

        var pdc = player.getPersistentDataContainer();

        if (pdc.has(this.initKey, PersistentDataType.BYTE)) {
            return;
        }

        if (pdc.has(oldKey, PersistentDataType.BYTE)) {
            pdc.remove(oldKey);

            var tag = Helix.tags().get("elimination");
            tag.add(player.getUniqueId(), (uuid, storage) -> {});
            return;
        }

        if (player.isOp()) {
            this.sendWelcome(player);
        }

        pdc.set(this.initKey, PersistentDataType.BYTE, (byte) 1);

        double max = (double) this.generalConfig.getValue("defaultHearts") * 2;

        Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(max);
        player.setHealth(max);
    }

    private void sendWelcome(Player player) {
        player.sendMessage("§aWelcome to §cLifeSteal §3v1.0.0§a!");
        player.sendMessage("§aHere's everything you need to get started:");

        player.sendMessage("§6❖ §aRun §f/lssetup §ato set up your server. §e(Do this first!)");

        player.sendMessage("§6❖ §aFind the config here: §fplugins §7-> §flifesteal");
        player.sendMessage("§7This is where all your settings are located.");
    }

}
