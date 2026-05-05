package tr.alperendemir.helix.listeners;

import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.items.ItemAction;
import tr.alperendemir.helix.api.items.context.ItemAttackEntityContext;
import tr.alperendemir.helix.api.items.context.ItemDropContext;
import tr.alperendemir.helix.api.items.context.ItemPickupContext;
import tr.alperendemir.helix.api.items.context.ItemUseContext;
import tr.alperendemir.helix.api.items.context.ItemUseHand;
import tr.alperendemir.helix.api.items.context.ItemUseOnBlockContext;
import tr.alperendemir.helix.api.items.context.ItemUseOnEntityContext;
import tr.alperendemir.helix.api.plugins.HelixPlugin;
import tr.alperendemir.helix.items.JavaHelixItemRegistry;
import tr.alperendemir.helix.items.variables.JavaHelixItemVariables;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class ItemUseListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        var itemRegistry = (JavaHelixItemRegistry) Helix.items();
        var clicked = event.getRightClicked();
        var player = event.getPlayer();
        var stack = player.getInventory().getItemInMainHand();

        var helixItemInstance = itemRegistry.getItemFromStack(stack);
        if (helixItemInstance == null) return;

        var helixItem = helixItemInstance.getItem();

        var plugin = HelixPlugin.getProvidingPlugin(helixItem.getClass());
        if (plugin == null) return;

        var useHand = event.getHand() == EquipmentSlot.HAND ? ItemUseHand.MAIN_HAND : ItemUseHand.OFF_HAND;

        var context = new ItemUseOnEntityContext(player, useHand, clicked, event.getClickedPosition());
        var instance = itemRegistry.createInstance(helixItem, stack);
        var variables = (JavaHelixItemVariables) instance.getVariables();
        variables.setAutoSave(false);

        var result = helixItem.fireCallback(ItemAction.INTERACT_ENTITY, context, instance);
        var general = helixItem.fireCallback(ItemAction.RIGHT_CLICK_GENERAL, context, instance);

        variables.forceSave();

        if (!result.isSuccess() || !general.isSuccess()) {
            event.setCancelled(true);
        }

        if (result.shouldSwingHand() || general.shouldSwingHand()) {
            player.swingMainHand();
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        var action = event.getAction();
        if (action == Action.PHYSICAL) return;

        var player = event.getPlayer();
        var stack = event.getItem();
        var itemRegistry = (JavaHelixItemRegistry) Helix.items();
        var helixItemInstance = itemRegistry.getItemFromStack(stack);
        if (helixItemInstance == null) return;

        var helixItem = helixItemInstance.getItem();

        var plugin = HelixPlugin.getProvidingPlugin(helixItem.getClass());
        if (plugin == null) return;

        if (!plugin.isWorldAllowed(player.getWorld().getUID())) return;

        var hand = event.getHand();

        var itemAction = switch (action) {
            case LEFT_CLICK_AIR -> ItemAction.LEFT_CLICK_AIR;
            case RIGHT_CLICK_AIR -> ItemAction.RIGHT_CLICK_AIR;
            case LEFT_CLICK_BLOCK -> ItemAction.LEFT_CLICK_BLOCK;
            case RIGHT_CLICK_BLOCK -> ItemAction.RIGHT_CLICK_BLOCK;
            default -> null;
        };

        var general = switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> ItemAction.LEFT_CLICK_GENERAL;
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> ItemAction.RIGHT_CLICK_GENERAL;
            default -> null;
        };

        var useHand = hand == EquipmentSlot.HAND ? ItemUseHand.MAIN_HAND : ItemUseHand.OFF_HAND;

        ItemUseContext context = null;
        if (itemAction == ItemAction.LEFT_CLICK_AIR || itemAction == ItemAction.RIGHT_CLICK_AIR) {
            context = new ItemUseContext(player, useHand);
        }

        if (itemAction == ItemAction.LEFT_CLICK_BLOCK || itemAction == ItemAction.RIGHT_CLICK_BLOCK) {
            context = new ItemUseOnBlockContext(player, useHand, event.getClickedBlock(), event.getBlockFace());
        }

        var instance = itemRegistry.createInstance(helixItem, stack);
        var variables = (JavaHelixItemVariables) instance.getVariables();
        variables.setAutoSave(false);

        var resultSpecific = helixItem.fireCallback(itemAction, context, instance);
        var resultGeneral = helixItem.fireCallback(general, context, instance);

        variables.forceSave();
        if (!resultSpecific.isSuccess() || !resultGeneral.isSuccess()) {
            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);
            event.setUseInteractedBlock(Event.Result.DENY);
        }

        if (resultSpecific.shouldSwingHand() || resultGeneral.shouldSwingHand()) {
            player.swingMainHand();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        var itemRegistry = (JavaHelixItemRegistry) Helix.items();
        var attacker = event.getDamager();
        if (!(attacker instanceof Player player)) return;
        var stack = player.getInventory().getItemInMainHand();
        var attacked = event.getEntity();

        var helixItemInstance = itemRegistry.getItemFromStack(stack);
        if (helixItemInstance == null) return;

        var helixItem = helixItemInstance.getItem();

        var plugin = HelixPlugin.getProvidingPlugin(helixItem.getClass());
        if (plugin == null) return;

        var context = new ItemAttackEntityContext(player, attacked);
        var instance = itemRegistry.createInstance(helixItem, stack);
        var variables = (JavaHelixItemVariables) instance.getVariables();
        variables.setAutoSave(false);

        var result = helixItem.fireCallback(ItemAction.ATTACK_ENTITY, context, instance);

        variables.forceSave();
        if (!result.isSuccess()) {
            event.setCancelled(true);
        }

        if (result.shouldSwingHand()) {
            player.swingMainHand();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        var itemRegistry = (JavaHelixItemRegistry) Helix.items();
        var dropped = event.getItemDrop();
        var stack = dropped.getItemStack();
        var player = event.getPlayer();

        var helixItemInstance = itemRegistry.getItemFromStack(stack);
        if (helixItemInstance == null) return;

        var helixItem = helixItemInstance.getItem();

        var plugin = HelixPlugin.getProvidingPlugin(helixItem.getClass());
        if (plugin == null) return;

        var context = new ItemDropContext(player, dropped);
        var instance = itemRegistry.createInstance(helixItem, stack);
        var variables = (JavaHelixItemVariables) instance.getVariables();
        variables.setAutoSave(false);

        var result = helixItem.fireCallback(ItemAction.DROP, context, instance);

        variables.forceSave();
        if (!result.isSuccess()) {
            event.setCancelled(true);
        }

        if (result.shouldSwingHand()) {
            player.swingMainHand();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        var itemRegistry = (JavaHelixItemRegistry) Helix.items();
        var dropped = event.getItem();
        var stack = dropped.getItemStack();
        var entity = event.getEntity();
        if (!(entity instanceof Player player)) return;

        var helixItemInstance = itemRegistry.getItemFromStack(stack);
        if (helixItemInstance == null) return;

        var helixItem = helixItemInstance.getItem();

        var plugin = HelixPlugin.getProvidingPlugin(helixItem.getClass());
        if (plugin == null) return;

        var context = new ItemPickupContext(player, dropped, event.getRemaining());
        var instance = itemRegistry.createInstance(helixItem, stack);
        var variables = (JavaHelixItemVariables) instance.getVariables();
        variables.setAutoSave(false);

        var result = helixItem.fireCallback(ItemAction.PICKUP, context, instance);

        variables.forceSave();
        if (!result.isSuccess()) {
            event.setCancelled(true);
        }

        if (result.shouldSwingHand()) {
            player.swingMainHand();
        }
    }
}
