package tr.alperendemir.helix.commands;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.brigadier.CommandDispatcher;
import tr.alperendemir.helix.api.commands.HelixCommand;
import tr.alperendemir.helix.api.commands.HelixCommandData;
import tr.alperendemir.helix.api.commands.HelixCommandRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class JavaHelixCommandRegistry implements HelixCommandRegistry {

    private static final MethodHandle SYNC_COMMANDS;
    private final Multimap<Plugin, String> commandNames = HashMultimap.create();
    private final Map<String, HelixCommandData> commandData = new HashMap<>();
    private final CommandDispatcher<CommandSender> dispatcher = new CommandDispatcher<>();

    private CommandMap cachedCommandMap;
    private Map<String, Command> cachedKnownCommands;


    static {
        try {
            SYNC_COMMANDS = MethodHandles.lookup().findVirtual(
                    Bukkit.getServer().getClass(),
                    "syncCommands",
                    MethodType.methodType(void.class)
            );
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(Plugin plugin, HelixCommand command) {
        var data = new HelixCommandData(command, new HelixCommandExecutor(command), new HelixCommandTabCompleter(command));

        var parameters = command.getParameters();

        this.dispatcher.getRoot().addChild(parameters);

        var name = parameters.getName();
        var prefix = plugin.getName().toLowerCase(Locale.ROOT) + ":";
        var prefixedName = prefix + name;

        var pluginCommand = this.createPluginCommand(name, plugin);
        pluginCommand.setExecutor(data.executor());
        pluginCommand.setTabCompleter(data.completer());
        pluginCommand.setPermission(null);

        Objects.requireNonNull(this.getCommandMap()).register(plugin.getName(), pluginCommand);

        this.commandNames.putAll(
                plugin,
                List.of(
                        name,
                        prefixedName
                )
        );

        this.commandData.put(name, data);
        this.commandData.put(prefixedName, data);

        this.commandNames.putAll(plugin, pluginCommand.getAliases());
        for (var alias : pluginCommand.getAliases()) {
            this.commandNames.put(plugin, prefix + alias);

            this.commandData.put(alias, data);
            this.commandData.put(prefix + alias, data);
        }
    }

    @Override
    public void unregister(Plugin plugin, String name) {
        var knownCommands = this.getKnownCommands();
        var commandMap = this.getCommandMap();
        assert commandMap != null;


        var baseCommand = this.getPluginCommand(plugin, name);
        if (baseCommand == null) return;

        baseCommand.unregister(commandMap);
        knownCommands.remove(name);
        this.commandNames.remove(plugin, name);
        this.commandData.remove(name);

        var prefix = plugin.getName() + ":";
        var labelCommandName = prefix + baseCommand.getName();
        var labelCommand = this.getPluginCommand(plugin, labelCommandName);
        if (labelCommand != null) {
            labelCommand.unregister(commandMap);
            knownCommands.remove(labelCommandName);
            this.commandNames.remove(plugin, labelCommandName);
            this.commandData.remove(labelCommandName);
        }

        for (var alias : baseCommand.getAliases()) {
            knownCommands.remove(alias);

            var aliasWithPrefix = prefix + alias;
            knownCommands.remove(aliasWithPrefix);

            this.commandNames.remove(plugin, alias);
            this.commandNames.remove(plugin, aliasWithPrefix);

            this.commandData.remove(alias);
            this.commandData.remove(aliasWithPrefix);
        }
    }

    @Override
    public void unregisterAll(Plugin plugin) {
        var toRemove = this.getNames(plugin);
        for (var name : toRemove) {
            this.unregister(plugin, name);
        }
    }

    @Override
    public Collection<String> getNames(Plugin plugin) {
        return new ArrayList<>(this.commandNames.get(plugin));
    }

    @Override
    public Collection<HelixCommandData> getCommands(Plugin plugin) {
        var set = new HashSet<HelixCommandData>();

        for (var name : this.commandNames.get(plugin)) {
            var data = this.commandData.get(name);
            if (data == null) continue;
            set.add(data);
        }

        return set;
    }

    @Override
    public HelixCommandData getByName(@Nullable Plugin plugin, String name) {
        var id = (plugin == null ? "" : plugin.getName().toLowerCase(Locale.ROOT) + ":") + name;

        return this.commandData.get(id);
    }

    @Override
    public void syncAll() {
        try {
            SYNC_COMMANDS.invoke(Bukkit.getServer());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public CommandDispatcher<CommandSender> getDispatcher() {
        return this.dispatcher;
    }

    private PluginCommand getPluginCommand(Plugin plugin, String name) {
        var commandMap = this.getCommandMap();
        assert commandMap != null;

        var command = commandMap.getCommand(name);
        if (!(command instanceof PluginCommand pluginCommand)) return null;
        if (pluginCommand.getPlugin() != plugin) return null;

        return pluginCommand;
    }

    private PluginCommand createPluginCommand(String name, Plugin plugin) {
        try {
            var commandConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            commandConstructor.setAccessible(true);

            return commandConstructor.newInstance(name, plugin);
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private CommandMap getCommandMap() {
        if (this.cachedCommandMap != null) return this.cachedCommandMap;

        try {
            this.setupCommandMap();
            return this.cachedCommandMap;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return null;
        }
    }

    private void setupCommandMap() throws IllegalAccessException, NoSuchFieldException {
        if (this.cachedCommandMap != null) return;

        var field = SimplePluginManager.class.getDeclaredField("commandMap");
        field.setAccessible(true);

        this.cachedCommandMap = (CommandMap) field.get(Bukkit.getPluginManager());
    }

    private Map<String, Command> getKnownCommands() {
        if (this.cachedKnownCommands != null) return this.cachedKnownCommands;

        try {
            this.setupKnownCommands();
            return this.cachedKnownCommands;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private void setupKnownCommands() throws NoSuchFieldException, IllegalAccessException {
        if (this.cachedKnownCommands != null) return;

        var knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
        knownCommandsField.setAccessible(true);

        this.cachedKnownCommands = (Map<String, Command>) knownCommandsField.get(this.getCommandMap());
    }
}
