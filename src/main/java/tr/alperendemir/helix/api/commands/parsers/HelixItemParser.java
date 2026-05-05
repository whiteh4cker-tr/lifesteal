package tr.alperendemir.helix.api.commands.parsers;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.namespaced.UniqueIdentifier;

import java.util.concurrent.CompletableFuture;

import static tr.alperendemir.helix.api.commands.arguments.ArgumentReader.readString;

public class HelixItemParser implements ArgumentType<UniqueIdentifier> {
    @Override
    public UniqueIdentifier parse(StringReader reader) throws CommandSyntaxException {
        var string = readString(reader, (r, ch) -> StringReader.isAllowedInUnquotedString(ch) || ch == ':');

        UniqueIdentifier key = null;

        if (!string.contains(":")) {
            for (var itemKey : Helix.items().getAllKeys()) {
                if (itemKey.key().equals(string)) {
                    key = itemKey;
                }
            }
        } else {
            try {
                key = UniqueIdentifier.parse(string);
            } catch (IllegalArgumentException e) {
                var msg = new LiteralMessage(e.getMessage());
                throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg, reader.getString(), reader.getCursor() - string.length());
            }
        }

        var item = Helix.items().getItem(key);
        if (item == null) {
            var msg = new LiteralMessage("Unknown item '" + string + "'");
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg, reader.getString(), reader.getCursor() - string.length());
        }

        return key;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for(var key : Helix.items().getAllKeys()) {
            builder.suggest(key.toString());
        }

        return builder.buildFuture();
    }
}
