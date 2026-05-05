package tr.alperendemir.helix.screens;

import tr.alperendemir.helix.api.namespaced.UniqueIdentifier;
import tr.alperendemir.helix.api.screens.HelixScreen;
import tr.alperendemir.helix.api.screens.HelixScreenOpenResult;
import tr.alperendemir.helix.api.screens.HelixScreenRegistry;
import tr.alperendemir.helix.screens.components.HelixScreenEvent;
import tr.alperendemir.helix.screens.page.HelixScreenPage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HelixScreenRegistryImpl implements HelixScreenRegistry {

    private final Map<String, HelixPluginScreenRegistry> registries = new HashMap<>();
    private final Map<UUID, String> playerToRegistry = new HashMap<>();

    @Override
    public boolean register(UniqueIdentifier id, HelixScreen screen) {
        return this.registries.computeIfAbsent(id.namespace(), s -> new HelixPluginScreenRegistry()).register(id.key(), screen);
    }

    @Override
    public @NotNull BukkitHelixScreenOpenResult open(@NotNull Player player, @NotNull UniqueIdentifier id, @Nullable String pageId) {
        var registry = this.registries.get(id.namespace());
        if (registry == null) return new BukkitHelixScreenOpenResult(HelixScreenOpenResult.OPEN_NO_REGISTRY, null);

        var uuid = player.getUniqueId();

        this.playerToRegistry.put(uuid, id.namespace());

        return registry.open(player, id.key(), pageId);
    }

    @Override
    public @Nullable HelixScreen getScreen(@NotNull UniqueIdentifier id) {
        var registry = this.registries.get(id.namespace());
        if (registry == null) return null;

        return registry.getScreen(id.key());
    }

    public HelixScreenPage getOpenPage(@NotNull Player player) {
        var current = this.playerToRegistry.get(player.getUniqueId());
        if (current == null) return null;

        var registry = this.registries.get(current);

        return registry.getOpenPage(player);
    }

    public void handleEvent(Inventory inventory, HelixScreenEvent event) {
        var current = this.playerToRegistry.get(event.player().getUniqueId());
        if (current == null) return;

        this.registries.get(current).handleEvent(inventory, event);
    }

    public void closed(@NotNull Player player, @NotNull Inventory inventory) {
        var id = player.getUniqueId();

        var registryId = this.playerToRegistry.get(id);
        if (registryId == null) return;

        var registry = this.registries.get(registryId);

        registry.close(id, inventory);
    }

    public void unregisterAll(String id) {
        var registry = this.registries.get(id);
        if (registry == null) return;

        registry.unregisterAll();
    }
}
