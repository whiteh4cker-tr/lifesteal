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
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerParser<T extends OfflinePlayer> implements ArgumentType<T> {

    public static final PlayerParser<Player> ONLINE = new PlayerParser<>(true);
    public static final PlayerParser<OfflinePlayer> ALL = new PlayerParser<>(false);

    private final boolean onlyOnline;

    public PlayerParser(boolean onlyOnline) {
        this.onlyOnline = onlyOnline;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T parse(StringReader reader) throws CommandSyntaxException {
        if (this.onlyOnline) {
            return (T) readOnline(reader);
        }

        return (T) readOffline(reader.readUnquotedString());
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (!this.onlyOnline) {
            this.suggestPlayers(Helix.players().allOffline(), builder);
        }

        this.suggestPlayers(Helix.players().allOnline(), builder);

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return ArgumentType.super.getExamples();
    }

    private void suggestPlayers(Collection<? extends OfflinePlayer> players, SuggestionsBuilder builder) {
        var remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (var player : players) {
            var name = player.getName();
            if (name == null) continue;

            if (!remaining.isBlank() && !name.toLowerCase(Locale.ROOT).startsWith(remaining)) continue;

            builder.suggest(name);
        }
    }

    private Player readOnline(StringReader reader) throws CommandSyntaxException {
        var input = reader.readUnquotedString();

        try {
            var uuid = UUID.fromString(input);
            var player = Helix.players().getOnline(uuid);
            if (player == null) {
                var msg = new LiteralMessage("Unknown player: " + input);
                throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg, reader.getString(), reader.getCursor() - input.length());
            }

            return player;
        } catch (IllegalArgumentException ignored) {

        }

        var player = Helix.players().getOnline(input);
        if (player == null) {
            var msg = new LiteralMessage("Unknown player: " + input);
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg, reader.getString(), reader.getCursor() - input.length());
        }

        return player;
    }

    private OfflinePlayer readOffline(String input) {
        try {
            var uuid = UUID.fromString(input);
            return Helix.players().getOffline(uuid);
        } catch (IllegalArgumentException e) {
            return Helix.players().getOffline(input);
        }
    }
}
