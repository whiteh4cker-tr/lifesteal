package tr.alperendemir.lssmp.commands.reset;

import tr.alperendemir.helix.api.commands.CommandExecutionResult;
import tr.alperendemir.helix.api.commands.HelixCommandParameters;
import tr.alperendemir.helix.api.commands.arguments.ArgumentList;
import tr.alperendemir.helix.api.commands.parsers.PlayerParser;
import tr.alperendemir.helix.api.config.Configuration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ResetCommand extends ResetCommandBase {

    private final Configuration generalConfiguration;
    private final Configuration commandConfig;

    public ResetCommand(Configuration generalConfiguration, Configuration commandConfig) {
        this.generalConfiguration = generalConfiguration;
        this.commandConfig = commandConfig;
    }

    @Override
    protected HelixCommandParameters makeParameters() {
        return HelixCommandParameters.create(this.commandConfig.getValue("name"))
                .childCommand(new ResetAllCommand(this.generalConfiguration))
                .permission(this.commandConfig.getValue("permission"))
                .argument("victim", PlayerParser.ONLINE);
    }

    @Override
    public CommandExecutionResult handleGenericSender(@NotNull CommandSender sender, @NotNull ArgumentList args) {
        var resetting = args.<Player>getArgument("victim");

        super.reset(resetting, this.generalConfiguration);

        sender.sendMessage("§aReset §3" + resetting.getName());

        return CommandExecutionResult.HANDLED;
    }
}
