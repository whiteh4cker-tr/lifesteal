package tr.alperendemir.helix.api.commands;

import com.mojang.brigadier.CommandDispatcher;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface HelixCommandRegistry {

    void register(Plugin plugin, HelixCommand command);

    void unregister(Plugin plugin, String name);

    void unregisterAll(Plugin plugin);

    Collection<String> getNames(Plugin plugin);

    Collection<HelixCommandData> getCommands(Plugin plugin);

    HelixCommandData getByName(@Nullable Plugin plugin, String name);

    void syncAll();

    CommandDispatcher<CommandSender> getDispatcher();

}
