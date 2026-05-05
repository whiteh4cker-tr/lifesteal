package tr.alperendemir.helix.api.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

public record HelixCommandData(HelixCommand command, CommandExecutor executor, TabCompleter completer) {

    @Override
    public String toString() {
        return "HelixCommandData[" +
                "command=" + command.getParameters().getName() +
                ", executor=" + executor +
                ", completer=" + completer +
                ']';
    }

}
