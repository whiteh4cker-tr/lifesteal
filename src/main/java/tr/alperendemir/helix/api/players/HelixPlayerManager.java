package tr.alperendemir.helix.api.players;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.util.Collection;
import java.util.UUID;

public interface HelixPlayerManager {

    @Nullable
    Player getOnline(@NotNull String name);

    @Nullable
    Player getOnline(@NotNull UUID uuid);

    @NotNull
    OfflinePlayer getOffline(@NotNull String name);

    @NotNull
    OfflinePlayer getOffline(@NotNull UUID uuid);

    /**
     * Fetches information about a given address.
     * <strong>This method is synchronous and will block.</strong>
     *
     * @return The information about the provided address, or null if it could not be fetched.
     * */
    @Nullable
    PlayerInformation getInformation(@NotNull InetAddress address);

    /**
     * Fetches information about a given player.
     * <strong>This method is synchronous and will block.</strong>
     *
     * @return The information about the provided player, or null if it could not be fetched.
     * */
    @Nullable
    default PlayerInformation getInformation(@NotNull Player player) {
        var addr = player.getAddress();
        if (addr == null) return null;

        return getInformation(addr.getAddress());
    }

    Collection<Player> allOnline();

    Collection<OfflinePlayer> allOffline();

}
