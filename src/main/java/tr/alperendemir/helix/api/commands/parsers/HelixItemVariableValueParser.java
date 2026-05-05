package tr.alperendemir.helix.api.commands.parsers;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.commands.arguments.ArgumentReader;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class HelixItemVariableValueParser implements ArgumentType<String> {

    public static HelixItemVariableValueParser INSTANCE = new HelixItemVariableValueParser();

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return ArgumentReader.readString(reader, (u, c) -> true);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if(!(context.getSource() instanceof Player player)) return builder.buildFuture();

        var item = player.getInventory().getItemInMainHand();

        var instance = Helix.items().getItemFromStack(item);
        if (instance == null) return builder.buildFuture();

        var nodes = context.getNodes();
        var last = nodes.isEmpty() ? null : nodes.get(nodes.size() - 1);
        var previous = nodes.isEmpty() ? null : last.getNode();

        if (!(previous instanceof ArgumentCommandNode<?,?> arg && arg.getType() instanceof HelixItemVariableParser)) {
            return builder.buildFuture();
        }

        var variable = last.getRange().get(context.getInput());
        if (variable.equals("___lore___")) return builder.buildFuture();

        var value = instance.getVariables().getValue(variable);

        builder.suggest(value.toString());

        return builder.buildFuture();
    }
}
