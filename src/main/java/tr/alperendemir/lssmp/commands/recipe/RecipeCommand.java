package tr.alperendemir.lssmp.commands.recipe;

import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.commands.CommandExecutionResult;
import tr.alperendemir.helix.api.commands.HelixCommand;
import tr.alperendemir.helix.api.commands.HelixCommandParameters;
import tr.alperendemir.helix.api.commands.arguments.ArgumentList;
import tr.alperendemir.helix.api.config.Configuration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static tr.alperendemir.lssmp.Constants.RECIPE_SCREEN_ID;

public class RecipeCommand extends HelixCommand {

    private final Configuration configuration;

    public RecipeCommand(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected HelixCommandParameters makeParameters() {
        return HelixCommandParameters.create(this.configuration.getValue("name"))
                .permission(this.configuration.getValue("permission"));
    }

    @Override
    public CommandExecutionResult handlePlayerSender(@NotNull Player player, @NotNull ArgumentList args) {
        var res = Helix.screens().open(player, RECIPE_SCREEN_ID, null);
        if (!res.success()) {
            player.sendMessage("§cUnable to open recipe screen, sorry!");
            return CommandExecutionResult.FAILURE;
        }

        return CommandExecutionResult.HANDLED;
    }
}
