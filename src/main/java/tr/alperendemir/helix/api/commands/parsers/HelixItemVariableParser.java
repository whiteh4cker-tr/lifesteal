package tr.alperendemir.helix.api.commands.parsers;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.commands.arguments.ArgumentReader;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class HelixItemVariableParser implements ArgumentType<String> {

    public static HelixItemVariableParser INSTANCE = new HelixItemVariableParser();

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return ArgumentReader.readString(reader, (u, c) -> StringReader.isAllowedInUnquotedString(c) || c == '_' || c == '-');
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if(!(context.getSource() instanceof Player player)) return builder.buildFuture();

        var item = player.getInventory().getItemInMainHand();

        var instance = Helix.items().getItemFromStack(item);
        if (instance == null) return builder.buildFuture();

        for (var key : instance.getVariables().getKeys()) {
            if (key.equals("___lore___")) continue;
            builder.suggest(key);
        }

        return builder.buildFuture();
    }
}
