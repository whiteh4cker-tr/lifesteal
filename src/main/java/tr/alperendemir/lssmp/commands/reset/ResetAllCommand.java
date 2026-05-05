package tr.alperendemir.lssmp.commands.reset;

import tr.alperendemir.helix.api.commands.CommandExecutionResult;
import tr.alperendemir.helix.api.commands.HelixCommandParameters;
import tr.alperendemir.helix.api.commands.arguments.ArgumentList;
import tr.alperendemir.helix.api.config.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ResetAllCommand extends ResetCommandBase {

    private final Configuration generalConfiguration;

    public ResetAllCommand(Configuration generalConfiguration) {
        this.generalConfiguration = generalConfiguration;
    }

    @Override
    protected HelixCommandParameters makeParameters() {
        return HelixCommandParameters.create("all");
    }

    @Override
    public CommandExecutionResult handleGenericSender(@NotNull CommandSender sender, @NotNull ArgumentList args) {
        for (var online : Bukkit.getOnlinePlayers()) {
            super.reset(online, this.generalConfiguration);
        }

        sender.sendMessage("§aReset §3" + Bukkit.getOnlinePlayers().size() + "§a players!");

        return CommandExecutionResult.HANDLED;
    }
}
