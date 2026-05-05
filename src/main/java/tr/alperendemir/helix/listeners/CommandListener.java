package tr.alperendemir.helix.listeners;

import com.mojang.brigadier.CommandDispatcher;
import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.namespaced.UniqueIdentifier;
import tr.alperendemir.helix.commands.HelixCommandExecutor;
import tr.alperendemir.helix.commands.HelixCommandTabCompleter;
import tr.alperendemir.helix.utils.ErrorMessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.TabCompleteEvent;

import java.io.File;
import java.util.Locale;
import java.util.UUID;

public class CommandListener implements Listener {

    private final CommandDispatcher<CommandSender> dispatcher;

    public CommandListener(CommandDispatcher<CommandSender> dispatcher) {
        this.dispatcher = dispatcher;
    }

    @EventHandler(ignoreCancelled = true)
    public void onTabComplete(TabCompleteEvent event) {
        var buffer = event.getBuffer().substring(1); // Remove the /

        var space = buffer.indexOf(' ');
        var commandName = space == -1 ? buffer : buffer.substring(0, space);

        var identifierIndex = commandName.indexOf(':');
        var identifier = identifierIndex == -1 ? "" : commandName.substring(0, identifierIndex);
        var command = identifierIndex == -1 ? commandName : commandName.substring(identifierIndex + 1);

        var plugin = identifier.isEmpty() ? null : Bukkit.getPluginManager().getPlugin(identifier);

        var commandRegistry = Helix.commands();
        var helixCommand = commandRegistry.getByName(plugin, command);

        if (helixCommand == null) return;

        var completer = helixCommand.completer();
        if (completer instanceof HelixCommandTabCompleter tabCompleter) {
            var completions = tabCompleter.tabComplete(
                    this.dispatcher,
                    event.getSender(),
                    buffer
            );

            event.setCompletions(completions);
        }
    }

    @EventHandler
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        var player = event.getPlayer();
        var world = player.getWorld().getUID();
        var commandRegistry = Helix.commands();
        var plugins = Helix.pluginLoader().getPlugins();

        var commandNames = event.getCommands();

        for (var plugin : plugins.values()) {
            if (plugin.isWorldAllowed(world)) {
                var commands = commandRegistry.getCommands(plugin.getBukkitPlugin());
                for (var command : commands) {
                    var params = command.command().getParameters();
                    if (params.canUse(player)) continue;

                    commandNames.remove(params.getName());
                    commandNames.remove(UniqueIdentifier.plugin(plugin, params.getName()).toString());
                }
                continue;
            }

            var names = commandRegistry.getNames(plugin.getBukkitPlugin());
            commandNames.removeAll(names);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        var player = event.getPlayer();
        var world = player.getWorld().getUID();
        var commandRegistry = Helix.commands();
        var plugins = Helix.pluginLoader().getPlugins();

        var message = event.getMessage().substring(1);
        var space = message.indexOf(' ');

        var command = space == -1 ? message : message.substring(0, space);

        if (isPluginCommand(command)) {
            this.overridePluginCommand(player, world);
            event.setCancelled(true);
            return;
        }

        for (var plugin : plugins.values()) {
            if (plugin.isWorldAllowed(world)) {
                var cmd = commandRegistry.getByName(plugin.getBukkitPlugin(), command);
                if (cmd != null && cmd.executor() instanceof HelixCommandExecutor executor) {
                    executor.handleCommandExecution(this.dispatcher, player, message);
                    break;
                }

                continue;
            }

            var names = commandRegistry.getNames(plugin.getBukkitPlugin());
            if (names.contains(command)) {
                // Send an error message

                ErrorMessageUtils.sendErrorMessage(player, "command.unknown.command", false, message);
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event) {
        var message = event.getCommand();
        var space = message.indexOf(' ');

        var command = space == -1 ? message : message.substring(0, space);


        UUID worldId = null;
        var sender = event.getSender();
        if (sender instanceof BlockCommandSender blockCommandSender) {
            worldId = blockCommandSender.getBlock().getWorld().getUID();
        }

        if (sender instanceof Entity entity) {
            worldId = entity.getWorld().getUID();
        }

        if (!isPluginCommand(command)) {
            var commandRegistry = Helix.commands();
            var plugins = Helix.pluginLoader().getPlugins();

            for (var plugin : plugins.values()) {
                if (plugin.isWorldAllowed(worldId)) {
                    var bukkitPlugin = command.contains(":") ? null : plugin.getBukkitPlugin();
                    var cmd = commandRegistry.getByName(bukkitPlugin, command);
                    if (cmd != null && cmd.executor() instanceof HelixCommandExecutor executor) {
                        executor.handleCommandExecution(this.dispatcher, sender, message);
                        break;
                    }
                }
            }
            return;
        }

        this.overridePluginCommand(event.getSender(), worldId);
        event.setCancelled(true);
    }

    private boolean isPluginCommand(String command) {
        if (command.startsWith("bukkit:")) {
            command = command.substring("bukkit:".length());
        }

        return command.equals("pl") || command.equals("plugins");
    }

    private void overridePluginCommand(CommandSender sender, UUID world) {
        var bukkitPlugins = Bukkit.getPluginManager().getPlugins();
        var loadedHelixPlugins = Helix.pluginLoader().getPlugins();
        var helixPluginFiles = Helix.pluginLoader().getPluginFolder().listFiles(File::isFile);
        if (helixPluginFiles == null) {
            helixPluginFiles = new File[0];
        }

        var bukkitPluginCount = bukkitPlugins.length - loadedHelixPlugins.size();

        var sb = new StringBuilder("§3Helix Plugins ({{TOTAL_PLUGIN_COUNT}}): ");
        var helixPluginCount = 0;
        for (var helixPlugin : helixPluginFiles) {
            if (!helixPlugin.getName().endsWith(".jar")) continue;

            var id = Helix.pluginLoader().getPluginId(helixPlugin);
            var loaded = loadedHelixPlugins.get(id);

            if (loaded != null) {
                if(!loaded.isWorldAllowed(world)) {
                    continue;
                }

                var bukkit = loadedHelixPlugins.get(id).getBukkitPlugin();
                if (bukkit.isEnabled()) {
                    sb.append("§a");
                } else {
                    sb.append("§e");
                }
            } else {
                sb.append("§c");
            }

            sb.append(id);
            sb.append(", ");
            helixPluginCount++;
        }

        if (helixPluginFiles.length > 0) {
            sb.setLength(sb.length() - 2);
            sender.sendMessage(sb.toString().replace("{{TOTAL_PLUGIN_COUNT}}", String.valueOf(helixPluginCount)));
        } else {
            sender.sendMessage("§cNo helix plugins found!");
        }

        sb.setLength(0);

        sb.append("§6Bukkit Plugins (%s): ".formatted(bukkitPluginCount));
        for (var bukkitPlugin : bukkitPlugins) {
            var id = bukkitPlugin.getName().toLowerCase(Locale.ROOT);
            if (loadedHelixPlugins.containsKey(id)) {
                continue;
            }

            if (bukkitPlugin.isEnabled()) {
                sb.append("§a");
            } else {
                sb.append("§c");
            }
            sb.append(bukkitPlugin.getName());
            sb.append(", ");
        }

        if (bukkitPluginCount > 0) {
            sb.setLength(sb.length() - 2);
            sender.sendMessage(sb.toString());

            sb.setLength(0);
        }
    }
}
