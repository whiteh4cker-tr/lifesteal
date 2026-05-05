package tr.alperendemir.helix.api.commands.arguments;

import com.mojang.brigadier.context.ParsedArgument;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class ArgumentList {

    private final Map<String, ParsedArgument<CommandSender, ?>> arguments;

    public ArgumentList(Map<String, ParsedArgument<CommandSender, ?>> arguments) {
        this.arguments = arguments;
    }


    @SuppressWarnings("unchecked")
    public <T> T getArgument(String name) {
        return (T) this.arguments.get(name).getResult();
    }

    @SuppressWarnings("unchecked")
    public <T> T getArgument(String name, T defaultValue) {
        var arg = this.arguments.get(name);
        if (arg == null) {
            return defaultValue;
        }

        return (T) arg.getResult();
    }

    public boolean hasArgument(String name) {
        return this.arguments.containsKey(name);
    }

}
