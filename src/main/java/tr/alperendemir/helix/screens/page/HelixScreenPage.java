package tr.alperendemir.helix.screens.page;

import tr.alperendemir.helix.api.screens.ScreenDimensions;
import tr.alperendemir.helix.api.screens.SlotPosition;
import tr.alperendemir.helix.api.screens.components.HelixComponentContext;
import tr.alperendemir.helix.api.screens.components.HelixComponentEvent;
import tr.alperendemir.helix.api.screens.components.HelixComponentHandler;
import tr.alperendemir.helix.api.screens.components.HelixPageComponent;
import tr.alperendemir.helix.api.screens.pages.HelixPage;
import tr.alperendemir.helix.screens.components.HelixScreenComponent;
import tr.alperendemir.helix.screens.components.HelixScreenEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class HelixScreenPage implements HelixPage {

    private final String name;
    private final ScreenDimensions dimensions;
    private final Map<String, HelixScreenComponent> components = new HashMap<>();
    private final List<Inventory> inventories = new ArrayList<>();
    private ItemStack background;

    public HelixScreenPage(String name, ScreenDimensions dimensions) {
        this.name = name;
        this.dimensions = dimensions;
    }

    @Override
    public ScreenDimensions dimensions() {
        return this.dimensions;
    }

    @Override
    public @Nullable HelixPageComponent addComponent(String name, HelixComponentHandler handler) {
        var component = new HelixScreenComponent(handler, () -> this.renderAll(false));
        if(this.components.putIfAbsent(name, component) != null) {
            return null;
        }

        return component;
    }

    @Override
    public @Nullable HelixPageComponent getComponent(String name) {
        return this.components.get(name);
    }

    @Override
    public void setBackground(@Nullable ItemStack background) {
        this.background = background;
    }

    public void open(Player player) {
        var inv = switch (this.dimensions.type()) {
            case CHEST -> Bukkit.createInventory(null, this.dimensions.size(), this.name);
            default -> Bukkit.createInventory(null, this.dimensions.type());
        };

        this.render(inv, true);

        player.openInventory(inv);

        this.inventories.add(inv);
    }

    public void closed(Inventory inventory) {
        for (var component : this.components.values()) {
            var context = new ComponentContext(component, this.dimensions, inventory);
            component.getHandler().close(context, component);
        }

        this.inventories.remove(inventory);

    }

    public void renderAll(boolean force) {
        for (var inv : this.inventories) {
            this.render(inv, force);
        }
    }

    public void handleEvent(Inventory inventory, HelixComponentEvent event) {
        var update = false;
        var clickPos = event.clickPosition();

        for (var component : this.components.values()) {
            var pos = component.position();
            var dim = component.dimensions();

            if (clickPos.x() < pos.x() || clickPos.x() >= pos.x() + dim.width()) continue;
            if (clickPos.y() < pos.y() || clickPos.y() >= pos.y() + dim.height()) continue;

            var context = new ComponentContext(component, this.dimensions, inventory);

            var ev = new HelixScreenEvent(
                    event.action(),
                    event.player(),
                    SlotPosition.fromXY(clickPos.x() - pos.x(), clickPos.y() - pos.y(), component.dimensions())
            );

            component.getHandler().handleEvent(ev, context, component);

            update = component.dirty();

            if (ev.cancelled()) event.cancel();
        }

        if (this.background != null) {
            event.cancel();
        }

        if (update) {
            this.render(inventory, false);
        }
    }

    public void render(Inventory inventory, boolean force) {
        var allowedSlots = new java.util.ArrayList<>(IntStream.range(0, inventory.getSize()).boxed().toList());

        for (var component : this.components.values()) {
            var pos = component.position();
            var size = component.dimensions();

            for (int x = 0; x < size.width(); x++) {
                for (int y = 0; y < size.height(); y++) {
                    var slot = (pos.y() + y) * this.dimensions.width() + (pos.x() + x);

                    allowedSlots.remove((Integer) slot);
                }
            }

            if (!force && !component.dirty()) continue;

            component.getHandler().render(new ComponentContext(component, this.dimensions, inventory), component);
            component.markClean();
        }

        for (var slot : allowedSlots) {
            inventory.setItem(slot, this.background);
        }
    }

    private record ComponentContext(HelixScreenComponent component, ScreenDimensions dimensions, Inventory inventory) implements HelixComponentContext {
        @Override
        public void setItem(SlotPosition setPos, ItemStack stack) {
            var pos = this.component.position();
            var bounds = this.component.dimensions();

            if (setPos.x() < 0 || setPos.x() >= bounds.width()) return;
            if (setPos.y() < 0 || setPos.y() >= bounds.height()) return;

            var slot = (setPos.y() + pos.y()) * this.dimensions.width() + (setPos.x() + pos.x());

            this.inventory.setItem(slot, stack);
        }

        @Override
        public @Nullable ItemStack getItem(SlotPosition getPos) {
            var pos = this.component.position();
            var bounds = this.component.dimensions();

            if (getPos.x() < 0 || getPos.x() >= bounds.width()) return null;
            if (getPos.y() < 0 || getPos.y() >= bounds.height()) return null;

            var slot = (getPos.y() + pos.y()) * this.dimensions.width() + (getPos.x() + pos.x());

            return this.inventory.getItem(slot);
        }
    }
}
