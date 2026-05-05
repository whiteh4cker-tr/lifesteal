package tr.alperendemir.helix.commands;

import com.mojang.brigadier.CommandDispatcher;
import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.commands.HelixCommand;
import tr.alperendemir.helix.api.logging.HelixLogger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public record HelixCommandTabCompleter(HelixCommand helixCommand) implements TabCompleter {

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return this.tabComplete(Helix.commands().getDispatcher(), sender, alias + " " + String.join(" ", args));
    }

    public List<String> tabComplete(CommandDispatcher<CommandSender> dispatcher, @NotNull CommandSender sender, @NotNull String commandLine) {
        var parse = dispatcher.parse(commandLine, sender);

        try {
            var usage = dispatcher.getCompletionSuggestions(parse).get();
            var list = new ArrayList<String>();
            for (var res : usage.getList()) {
                var text = res.getText();
                list.add(text);
                if (list.size() >= 75) break;
            }

            return list;
        } catch (InterruptedException | ExecutionException e) {
            HelixLogger.error(e);
            return List.of();
        }
    }
}
