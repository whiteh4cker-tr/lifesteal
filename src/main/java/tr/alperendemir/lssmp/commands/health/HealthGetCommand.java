package tr.alperendemir.lssmp.commands.health;

import tr.alperendemir.helix.api.commands.CommandExecutionResult;
import tr.alperendemir.helix.api.commands.HelixCommand;
import tr.alperendemir.helix.api.commands.HelixCommandParameters;
import tr.alperendemir.helix.api.commands.arguments.ArgumentList;
import tr.alperendemir.helix.api.commands.parsers.PlayerParser;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HealthGetCommand extends HelixCommand {

    @Override
    protected HelixCommandParameters makeParameters() {
        return HelixCommandParameters.create("get")
                .argument("victim", PlayerParser.ONLINE);
    }

    @Override
    public CommandExecutionResult handleGenericSender(@NotNull CommandSender sender, @NotNull ArgumentList args) {
        var player = args.<Player>getArgument("victim");

        var health = player.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        sender.sendMessage("§f" + player.getName() + " §ahas §3" + (health / 2) + " §ahearts!");

        return CommandExecutionResult.HANDLED;
    }
}
