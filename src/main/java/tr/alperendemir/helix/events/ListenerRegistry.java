package tr.alperendemir.helix.events;

import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.events.HelixEventRegistry;
import tr.alperendemir.helix.api.events.annotations.CaptureChildEvents;
import tr.alperendemir.helix.api.logging.HelixLogger;
import tr.alperendemir.helix.api.plugins.HelixPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;

public final class ListenerRegistry implements HelixEventRegistry {

    public void callEvent(HelixPlugin plugin, WrapperEvent event) {
        this.callEventInternal(plugin, (Event) event);
    }

    @Override
    public void broadcast(Event event) {
        for (var plugin : Helix.pluginLoader().getPlugins().values()) {
            this.callEventInternal(plugin, event);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void register(Listener listener) {
        var helixPlugin = HelixPlugin.getProvidingPlugin(listener.getClass());
        var bukkitPlugin = helixPlugin.getBukkitPlugin();

        var methods = Arrays.stream(listener.getClass().getDeclaredMethods())
                .filter(method -> method.getParameterCount() > 0)
                .filter(method -> Event.class.isAssignableFrom(method.getParameterTypes()[0]))
                .toList();

        for (var method : methods) {
            var annotation = method.getAnnotation(EventHandler.class);
            if (annotation == null) continue;

            var captureChildren = method.isAnnotationPresent(CaptureChildEvents.class);

            method.setAccessible(true);

            var event = method.getParameterTypes()[0];
            registerListener(
                    (Class<? extends Event>) event,
                    listener,
                    annotation,
                    new ListenerExecutor(event, method, helixPlugin, captureChildren),
                    bukkitPlugin
            );
        }
    }

    @Override
    public void unregister(Listener listener) {
        HandlerList.unregisterAll(listener);
    }

    private void registerListener(Class<? extends Event> eventClass, Listener listener, EventHandler handler, EventExecutor executor, Plugin plugin) {
        Bukkit.getPluginManager().registerEvent(
                eventClass,
                listener,
                handler.priority(),
                executor,
                plugin,
                handler.ignoreCancelled()
        );
    }

    private void callEventInternal(HelixPlugin plugin, Event event) {
        var bukkitPlugin = plugin.getBukkitPlugin();
        var registeredListeners = HandlerList.getRegisteredListeners(bukkitPlugin);
        for (var registered : registeredListeners) {
            try {
                registered.callEvent(event);
            } catch (EventException e) {
                bukkitPlugin.getLogger().log(Level.SEVERE, "Error occurred while handling event!", e);
            }
        }
    }

    private record ListenerExecutor(Class<?> eventType, Method method, HelixPlugin plugin, boolean captureChildren) implements EventExecutor {

        @Override
        public void execute(@NotNull Listener listener, @NotNull Event event) {
            if (!this.allowEvent(event)) return;

            if (shouldFilterEvent(event, this.plugin())) {
                return;
            }

            var unwrappedEvent = event instanceof WrapperEvent wrapperEvent
                    ? (Event) this.eventType().cast(wrapperEvent)
                    : event;

            try {
                this.method().invoke(listener, unwrappedEvent);
            } catch (IllegalAccessException | InvocationTargetException e) {
                var bukkitPlugin = this.plugin.getBukkitPlugin();

                HelixLogger.error("Hey! There was an error while executing listener '%s' (from plugin '%s'), important details:",
                        unwrappedEvent.getClass().getName(), bukkitPlugin.getDescription().getFullName());
                HelixLogger.errorWithSuppressed(e);
            }
        }

        private boolean allowEvent(Event event) {
            if (this.captureChildren) {
                return this.eventType().isAssignableFrom(event.getClass());
            }

            return event.getClass() == this.eventType();
        }

        private boolean shouldFilterEvent(Event event, HelixPlugin plugin) {
            if (event instanceof WrapperEvent wrapperEvent) {
                return !plugin.isWorldAllowed(wrapperEvent.getWorld());
            }

            if (shouldSkipEvent(event)) return true;

            World world = null;

            if (event instanceof BlockEvent blockEvent) {
                world = blockEvent.getBlock().getWorld();
            }

            if (event instanceof EntityEvent entityEvent) {
                world = entityEvent.getEntity().getWorld();
            }

            if (event instanceof HangingEvent hangingEvent) {
                world = hangingEvent.getEntity().getWorld();
            }

            if (event instanceof InventoryEvent inventoryEvent) {
                world = inventoryEvent.getView().getPlayer().getWorld();
            }

            if (event instanceof InventoryMoveItemEvent inventoryMoveItemEvent) {
                world = this.getWorldForHolder(inventoryMoveItemEvent.getSource().getHolder());
            }

            if (event instanceof InventoryPickupItemEvent inventoryPickupItemEvent) {
                world = inventoryPickupItemEvent.getItem().getWorld();
            }

            if (event instanceof PlayerEvent playerEvent) {
                world = playerEvent.getPlayer().getWorld();
            }

            if (event instanceof PlayerLeashEntityEvent leashEntityEvent) {
                world = leashEntityEvent.getPlayer().getWorld();
            }

            if (event instanceof TabCompleteEvent tabCompleteEvent) {
                world = this.getWorldForSender(tabCompleteEvent.getSender());
            }

            if (event instanceof VehicleEvent vehicleEvent) {
                world = vehicleEvent.getVehicle().getWorld();
            }

            if (event instanceof WeatherEvent weatherEvent) {
                world = weatherEvent.getWorld();
            }

            if (event instanceof WorldEvent worldEvent) {
                world = worldEvent.getWorld();
            }

            return world != null && !plugin.isWorldAllowed(world.getUID());
        }

        private boolean shouldSkipEvent(Event event) {
            return event instanceof PlayerTeleportEvent || event instanceof PlayerChangedWorldEvent;
        }

        private World getWorldForHolder(InventoryHolder holder) {
            if (holder instanceof BlockInventoryHolder blockInventoryHolder) {
                return blockInventoryHolder.getBlock().getWorld();
            }

            if (holder instanceof Entity entity) {
                return entity.getWorld();
            }

            return null;
        }

        private World getWorldForSender(CommandSender sender) {
            if (sender instanceof BlockCommandSender blockCommandSender) {
                return blockCommandSender.getBlock().getWorld();
            }

            if (sender instanceof Entity entity) {
                return entity.getWorld();
            }

            return null;
        }
    }

}
