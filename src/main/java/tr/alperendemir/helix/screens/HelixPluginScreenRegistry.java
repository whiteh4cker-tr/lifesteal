package tr.alperendemir.helix.screens;

import tr.alperendemir.helix.api.screens.HelixScreen;
import tr.alperendemir.helix.api.screens.HelixScreenOpenResult;
import tr.alperendemir.helix.screens.components.HelixScreenEvent;
import tr.alperendemir.helix.screens.page.HelixScreenPage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HelixPluginScreenRegistry {

    private final Map<String, HelixScreenStore> stores = new HashMap<>();
    private final Map<UUID, HelixScreenPage> openPages = new HashMap<>();

    public boolean register(String key, HelixScreen screen) {
        if (this.stores.containsKey(key)) return false;

        var store = new HelixScreenStore(screen);
        screen.setup(store);

        this.stores.put(key, store);
        return true;
    }

    public HelixScreen getScreen(String key) {
        var store = this.stores.get(key);

        if (store != null) {
            return store.getScreen();
        }

        return null;
    }

    public BukkitHelixScreenOpenResult open(@NotNull Player player, @NotNull String key, @Nullable String pageId) {
        var store = this.stores.get(key);
        if (store == null) return new BukkitHelixScreenOpenResult(HelixScreenOpenResult.OPEN_NO_SCREEN, null);

        var page = store.getPage(pageId);
        if (page == null) return new BukkitHelixScreenOpenResult(HelixScreenOpenResult.OPEN_NO_PAGE, null);


        var id = player.getUniqueId();

        var open = this.openPages.remove(id);
        if (open != null) {
            open.closed(player.getOpenInventory().getTopInventory());
        }

        var screenPage = (HelixScreenPage) page;

        screenPage.open(player);

        this.openPages.put(id, screenPage);

        return new BukkitHelixScreenOpenResult(HelixScreenOpenResult.OPEN_SUCCESS, screenPage);
    }

    public HelixScreenPage getOpenPage(Player player) {
        return this.openPages.get(player.getUniqueId());
    }

    public void handleEvent(Inventory inventory, HelixScreenEvent event) {
        var page = this.openPages.get(event.player().getUniqueId());
        if (page == null) return;

        page.handleEvent(inventory, event);
    }

    public void close(UUID uuid, Inventory inventory) {
        var open = this.openPages.remove(uuid);
        if (open == null) return;

        open.closed(inventory);
    }

    public void unregisterAll() {
        this.stores.clear();

        for (var key : this.openPages.keySet()) {
            var player = Bukkit.getPlayer(key);
            if (player == null) continue;

            player.closeInventory();
        }

        this.openPages.clear();
    }

}
