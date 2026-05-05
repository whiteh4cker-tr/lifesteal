package tr.alperendemir.helix.api.screens;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public enum ScreenAction {

    /**
     * Called whenever ANY pickup action happens.
     * */
    PICKUP,

    /**
     * Called when the player left-clicks a slot to pick up the entire stack that's contained within it.
     * */
    PICKUP_FULL_STACK {
        @Override
        public ScreenAction[] getGenericActions() {
            return new ScreenAction[] { PICKUP };
        }
    }, // PICKUP ALL

    /**
     * Called when the player right-clicks the stack to pick up half of the stack.
     * */
    PICKUP_HALF_STACK {
        @Override
        public ScreenAction[] getGenericActions() {
            return new ScreenAction[] { PICKUP };
        }
    }, // PICKUP HALF

    /**
     * Called whenever ANY place action happens.
     * */
    PLACE,

    /**
     * Called when the player left-clicks a slot to place up the entire stack that's on their cursor.
     * */
    PLACE_FULL_STACK {
        @Override
        public ScreenAction[] getGenericActions() {
            return new ScreenAction[] { PLACE };
        }
    }, // PLACE ALL

    /**
     * This event is triggered when a player attempts to place items they have on their cursor into a slot.
     * However, if the slot already contains items of the same type, and adding the items from the cursor
     * would exceed the maximum stack size for that item, the game places the maximum amount of items it can in the slot,
     * and keeps the rest of the items on the cursor.
     * */
    PLACE_MAXIMUM {
        @Override
        public ScreenAction[] getGenericActions() {
            return new ScreenAction[] { PLACE };
        }
    }, // PLACE_SOME

    /**
     * Called whenever the player right clicks with an item on a non-full slot that has the same type of item.
     * */
    PLACE_ONE {
        @Override
        public ScreenAction[] getGenericActions() {
            return new ScreenAction[] { PLACE };
        }
    }, // PLACE ONE

    /**
     * Called when a player double-clicks on a non-full slot and there are other items of the same type in any of the
     * currently open inventories.
     * */
    COLLECT_SAME_TYPE {
        @Override
        public ScreenAction[] getGenericActions() {
            return new ScreenAction[] { PICKUP };
        }
    }, // COLLECT_TO_CURSOR

    /**
     * Called when a player tries to left-click or right-click an item into a slot that has a different item type inside.
     * */
    SWAP_CURSOR_ITEMS {
        @Override
        public ScreenAction[] getGenericActions() {
            return new ScreenAction[] { PICKUP, PLACE };
        }
    }, // SWAP_WITH_CURSOR

    DROP,

    DROP_STACK_OFF_SCREEN {
        @Override
        public ScreenAction[] getGenericActions() {
            return new ScreenAction[] { DROP };
        }
    }, // DROP_ALL_CURSOR

    DROP_ONE_OFF_SCREEN {
        @Override
        public ScreenAction[] getGenericActions() {
            return new ScreenAction[] { DROP };
        }
    }, // DROP_ONE_CURSOR

    DROP_STACK {
        @Override
        public ScreenAction[] getGenericActions() {
            return new ScreenAction[] { DROP };
        }
    }, // DROP_ALL_SLOT

    DROP_ONE {
        @Override
        public ScreenAction[] getGenericActions() {
            return new ScreenAction[] { DROP };
        }
    }, // DROP_ONE_SLOT

    QUICK_MOVE {
        @Override
        public ScreenAction[] getGenericActions() {
            return new ScreenAction[] { PICKUP, PLACE };
        }
    },
    QUICK_MOVE_TO_INVENTORY {
        @Override
        public ScreenAction[] getGenericActions() {
            return new ScreenAction[] { QUICK_MOVE };
        }
    }, // MOVE_TO_OTHER_INVENTORY
    QUICK_MOVE_TO_CONTAINER {
        @Override
        public ScreenAction[] getGenericActions() {
            return new ScreenAction[] { QUICK_MOVE };
        }
    }, // MOVE_TO_OTHER_INVENTORY

    MOVE_WITH_KEYS {
        @Override
        public ScreenAction[] getGenericActions() {
            return new ScreenAction[] { PICKUP, PLACE };
        }
    },
    MOVE_TO_HOTBAR {
        @Override
        public ScreenAction[] getGenericActions() {
            return new ScreenAction[] { MOVE_WITH_KEYS };
        }
    }, // HOTBAR_SWAP
    MOVE_TO_OFFHAND {
        @Override
        public ScreenAction[] getGenericActions() {
            return new ScreenAction[] { MOVE_WITH_KEYS };
        }
    }, // HOTBAR_SWAP

    SWAP_WITH_KEYS {
        @Override
        public ScreenAction[] getGenericActions() {
            return new ScreenAction[] { PICKUP, PLACE };
        }
    },
    SWAP_WITH_HOTBAR {
        @Override
        public ScreenAction[] getGenericActions() {
            return new ScreenAction[] { SWAP_WITH_KEYS };
        }
    }, // HOTBAR_MOVE_AND_READD
    SWAP_WITH_OFFHAND {
        @Override
        public ScreenAction[] getGenericActions() {
            return new ScreenAction[] { SWAP_WITH_KEYS };
        }
    }, // HOTBAR_MOVE_AND_READD

    CLONE_STACK,

    NOTHING,

    UNKNOWN;

    public ItemStack getPlacingStack(InventoryClickEvent event) {
        if (event.getClick() == ClickType.NUMBER_KEY) {
            return event.getCurrentItem();
        }

        return event.getWhoClicked().getItemOnCursor();
    }

    public ItemStack getClickedStack(InventoryClickEvent event) {
        if (event.getClick() == ClickType.NUMBER_KEY) {
            return event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
        }

        return event.getCurrentItem();
    }

    public ScreenAction[] getGenericActions() {
        return null;
    }

    public static ScreenAction getScreenAction(InventoryClickEvent event) {
        return switch (event.getAction()) {
            case UNKNOWN -> UNKNOWN;
            case NOTHING -> NOTHING;
            case CLONE_STACK -> CLONE_STACK;
            case HOTBAR_MOVE_AND_READD -> null; // TODO
            case HOTBAR_SWAP -> null; // TODO
            case MOVE_TO_OTHER_INVENTORY -> {
                if (event.getClickedInventory() instanceof PlayerInventory) {
                    yield QUICK_MOVE_TO_CONTAINER;
                }

                yield QUICK_MOVE_TO_INVENTORY;
            }
            case DROP_ONE_SLOT -> DROP_ONE;
            case DROP_ALL_SLOT -> DROP_STACK;
            case DROP_ONE_CURSOR -> DROP_ONE_OFF_SCREEN;
            case DROP_ALL_CURSOR -> DROP_STACK_OFF_SCREEN;
            case SWAP_WITH_CURSOR -> SWAP_CURSOR_ITEMS;
            case COLLECT_TO_CURSOR -> COLLECT_SAME_TYPE;
            case PLACE_ONE -> PLACE_ONE;
            case PLACE_SOME -> PLACE_MAXIMUM;
            case PLACE_ALL -> PLACE_FULL_STACK;
            case PICKUP_HALF -> PICKUP_HALF_STACK;
            case PICKUP_ALL -> PICKUP_FULL_STACK;
            default -> throw new UnsupportedOperationException(event.getAction().name() + " has been fired but not implemented!");
        };
    }

}
