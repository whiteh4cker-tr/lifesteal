package tr.alperendemir.helix.api.screens;

import tr.alperendemir.helix.api.namespaced.UniqueIdentifier;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HelixScreenRegistry {

    /**
     * Registers a screen to a specific key.
     *
     * @param id The key of the screen, will be used to identify the screen.
     * @param screen The screen to register.
     * */
    boolean register(UniqueIdentifier id, HelixScreen screen);

    /**
     * Opens a screen for a player on a specific page.
     *
     * @param player The player to open the inventory for
     * @param id The key of the inventory
     * @param page The page of the inventory, if null it will use the first registered page.
     * @return A {@link HelixScreenOpenResult} with one of the following states:
     * {@link HelixScreenOpenResult#OPEN_SUCCESS} if the screen could be opened.
     * If there is no associated screen to the key then {@link HelixScreenOpenResult#OPEN_NO_SCREEN} is returned.
     * If there is a screen associated to the screen, but there is no page that matches the provided id, {@link HelixScreenOpenResult#OPEN_NO_PAGE} is returned.
     * */
    @NotNull
    HelixScreenOpenResult open(@NotNull Player player, @NotNull UniqueIdentifier id, @Nullable String page);

    @Nullable HelixScreen getScreen(@NotNull UniqueIdentifier id);

}
