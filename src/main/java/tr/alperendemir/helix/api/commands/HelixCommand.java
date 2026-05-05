package tr.alperendemir.helix.api.commands;

import com.mojang.brigadier.tree.CommandNode;
import tr.alperendemir.helix.api.messages.colors.MinecraftColor;
import tr.alperendemir.helix.api.commands.arguments.ArgumentList;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class HelixCommand {

    private CommandNode<CommandSender> parameters;

    public final CommandNode<CommandSender> getParameters() {
        if (this.parameters == null) {
            var params = this.makeParameters();
            if(params.getExecutor() == null) {
                params.executor(this);
            }
            this.parameters = params.build(this);
        }

        return this.parameters;
    }

    protected abstract HelixCommandParameters makeParameters();

    public CommandExecutionResult handleGenericSender(@NotNull CommandSender sender, @NotNull ArgumentList args) {
        return CommandExecutionResult.IGNORED;
    }

    public CommandExecutionResult handleConsoleSender(@NotNull ConsoleCommandSender sender, @NotNull ArgumentList args) {
        sender.sendMessage(MinecraftColor.RED + "This command is not available to the console!");
        return CommandExecutionResult.SUCCESS;
    }

    public CommandExecutionResult handleBlockSender(@NotNull BlockCommandSender sender, @NotNull ArgumentList args) {
        sender.sendMessage(MinecraftColor.RED + "This command is not available to command blocks!");
        return CommandExecutionResult.SUCCESS;
    }

    public CommandExecutionResult handlePlayerSender(@NotNull Player player, @NotNull ArgumentList args) {
        player.sendMessage(MinecraftColor.RED + "This command is not available to players!");
        return CommandExecutionResult.SUCCESS;
    }

}
