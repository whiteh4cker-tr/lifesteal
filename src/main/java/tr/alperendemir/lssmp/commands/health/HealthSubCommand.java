package tr.alperendemir.lssmp.commands.health;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import tr.alperendemir.helix.api.commands.CommandExecutionResult;
import tr.alperendemir.helix.api.commands.HelixCommand;
import tr.alperendemir.helix.api.commands.HelixCommandParameters;
import tr.alperendemir.helix.api.commands.arguments.ArgumentList;
import tr.alperendemir.helix.api.commands.parsers.PlayerParser;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HealthSubCommand extends HelixCommand {

    @Override
    protected HelixCommandParameters makeParameters() {
        return HelixCommandParameters.create("add")
                .argument("victim", PlayerParser.ONLINE)
                .argument("amount", DoubleArgumentType.doubleArg(0));
    }

    @Override
    public CommandExecutionResult handleGenericSender(@NotNull CommandSender sender, @NotNull ArgumentList args) {
        var player = args.<Player>getArgument("victim");
        var amount = args.<Double>getArgument("amount");

        var attribute = player.getAttribute(Attribute.MAX_HEALTH);
        assert attribute != null;
        attribute.setBaseValue(Math.max(attribute.getBaseValue() - (amount * 2), 1));

        sender.sendMessage("§f" + player.getName() + " §anow has §3" + (attribute.getBaseValue() / 2) + " §ahearts!");

        return CommandExecutionResult.HANDLED;
    }
}
