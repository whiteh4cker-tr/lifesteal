package tr.alperendemir.lssmp.commands.eliminate;

import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.commands.CommandExecutionResult;
import tr.alperendemir.helix.api.commands.HelixCommand;
import tr.alperendemir.helix.api.commands.HelixCommandParameters;
import tr.alperendemir.helix.api.commands.arguments.ArgumentList;
import tr.alperendemir.lssmp.elimination.EliminationManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EliminateAllCommand extends HelixCommand {

    private final EliminationManager eliminationManager;

    public EliminateAllCommand(EliminationManager eliminationManager) {
        this.eliminationManager = eliminationManager;
    }

    @Override
    protected HelixCommandParameters makeParameters() {
        return HelixCommandParameters.create("all");
    }

    @Override
    public CommandExecutionResult handleGenericSender(@NotNull CommandSender sender, @NotNull ArgumentList args) {
        var online = Helix.players().allOnline();
        if (online.isEmpty()) {
            sender.sendMessage("§aThere are no players!");
            return CommandExecutionResult.HANDLED;
        }

        final var senderPlayer = sender instanceof Player player ? player : null;

        int eliminatedCount = 0;

        for (var plr : online) {
            final var result = this.eliminationManager.tryEliminate(plr, senderPlayer);
            switch (result) {
                case ELIMINATION, ATTEMPTED_ELIMINATION -> eliminatedCount++;
            }
        }

        sender.sendMessage("§aEliminated §3" + eliminatedCount + " §aplayer" + (eliminatedCount != 1 ? "s" : ""));

        return CommandExecutionResult.HANDLED;
    }
}
