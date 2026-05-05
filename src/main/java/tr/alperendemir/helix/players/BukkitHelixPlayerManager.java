package tr.alperendemir.helix.players;

import com.google.gson.JsonObject;
import tr.alperendemir.helix.api.players.HelixPlayerManager;
import tr.alperendemir.helix.api.players.PlayerInformation;
import tr.alperendemir.helix.utils.JsonUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class BukkitHelixPlayerManager implements HelixPlayerManager {

    private final Map<String, PlayerInformation> infoCache = new HashMap<>();

    @Override
    public @Nullable Player getOnline(@NotNull String name) {
        return Bukkit.getPlayer(name);
    }

    @Override
    public @Nullable Player getOnline(@NotNull UUID uuid) {
        return Bukkit.getPlayer(uuid);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull OfflinePlayer getOffline(@NotNull String name) {
        return Bukkit.getOfflinePlayer(name);
    }

    @Override
    public @NotNull OfflinePlayer getOffline(@NotNull UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid);
    }

    @Override
    public PlayerInformation getInformation(@NotNull InetAddress address) {
        try {
            var ip = address.getHostAddress();

            var cached = this.infoCache.get(ip);
            if (cached != null) {
                return cached; // Doing this to save API calls
            }

            var url = new URL("https://ipinfo.io/" + ip + "/json");
            var connection = url.openConnection();

            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            connection.setDoInput(true);


            var str = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            var object = JsonUtils.GSON.fromJson(str, JsonObject.class);

            var country = object.get("country");
            var countryStr = country == null ? "CH" : country.getAsString(); // Switzerland is neutral
            var timeZone = object.get("timezone");
            var timeZoneStr = timeZone == null ? "Europe/Berlin" : timeZone.getAsString();

            var info = new BukkitHelixPlayerInformation(
                    countryStr.toUpperCase(Locale.ROOT),
                    TimeZone.getTimeZone(timeZoneStr)
            );

            this.infoCache.put(ip, info);

            return info;
        } catch (IOException exception) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Player> allOnline() {
        return (Collection<Player>) Bukkit.getOnlinePlayers();
    }

    @Override
    public Collection<OfflinePlayer> allOffline() {
        return Arrays.stream(Bukkit.getOfflinePlayers()).toList();
    }
}
