package tr.alperendemir.helix.api.commands.parsers;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.logging.HelixLogger;
import org.bukkit.World;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WorldParser implements ArgumentType<World> {

    public enum WorldFlag {
        ALLOWED_BY_PLUGIN,
        DENIED_BY_PLUGIN,
        ALL_WORLDS
    }

    private final WorldFlag worldFlag;

    public WorldParser(WorldFlag worldFlag) {
        this.worldFlag = worldFlag;
    }

    @Override
    public World parse(StringReader reader) throws CommandSyntaxException {
        var str = reader.readUnquotedString();

        try {
            return Helix.worlds().get(UUID.fromString(str));
        } catch (IllegalArgumentException e) {
            var world = Helix.worlds().get(str);
            if (world != null) {
                return world;
            }

            var msg = new LiteralMessage("Unknown world: " + str);
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg, reader.getString(), reader.getCursor() - str.length());
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        var nodes = context.getNodes();
        var last = nodes.isEmpty() ? null : nodes.get(nodes.size() - 1);
        var previous = nodes.isEmpty() ? null : last.getNode();

        if (this.worldFlag == WorldFlag.ALL_WORLDS || last == null || !(previous instanceof ArgumentCommandNode<?,?> arg && arg.getType() instanceof HelixPluginParser pluginParser)) {
            var worlds = Helix.worlds().listWorlds();
            for (var world : worlds) {
                builder.suggest(world.getName());
            }

            return builder.buildFuture();
        }

        var pluginString = last.getRange().get(context.getInput());
        try {
            var parsed = pluginParser.parse(new StringReader(pluginString));

            var worlds = Helix.worlds().listWorlds();
            for (var world : worlds) {
                var allowed = parsed.isWorldAllowed(world.getUID());
                if (this.worldFlag == WorldFlag.DENIED_BY_PLUGIN) {
                    allowed = !allowed;
                }

                if (allowed) {
                    builder.suggest(world.getName());
                }
            }
        } catch (CommandSyntaxException e) {
            HelixLogger.reportError(e);
        }

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return List.of("world");
    }
}
