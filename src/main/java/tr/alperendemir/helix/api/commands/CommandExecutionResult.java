package tr.alperendemir.helix.api.commands;

import tr.alperendemir.helix.api.commands.arguments.ArgumentList;
import org.bukkit.command.CommandSender;

public enum CommandExecutionResult {

    /**
     * Indicates that this part of the command ran correctly and that no errors occurred.
     * */
    SUCCESS,

    /**
     * Indicates that a problem has happened in this stage of the command, it will cancel any, more specific, executions.
     * See {@link #HANDLED} on how this result blocks further executions.
     * */
    FAILURE,

    /**
     * Indicates a success, and blocks all further more-specific executions.
     * For example returning this from {@link HelixCommand#handleGenericSender(CommandSender, ArgumentList)}
     * will cause handleConsoleSender, handleBlockSender and handlePlayerSender to not run.
     * */
    HANDLED,

    /**
     * Indicates that the current stage was ignored, does nothing.
     * */
    IGNORED,

}
