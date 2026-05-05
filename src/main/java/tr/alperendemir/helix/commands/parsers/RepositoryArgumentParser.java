package tr.alperendemir.helix.commands.parsers;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import tr.alperendemir.helix.BukkitHelixProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public class RepositoryArgumentParser implements ArgumentType<String> {

    public static RepositoryArgumentParser INSTANCE = new RepositoryArgumentParser();

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        var plugin = JavaPlugin.getPlugin(BukkitHelixProvider.class);
        var repoManager = plugin.getRepositoryManager();
        var repos = repoManager.getRepositories();

        var str = reader.readString();

        if(!repos.containsKey(str)) {
            var msg = new LiteralMessage("Unknown repository: " + str);
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg, reader.getString(), reader.getCursor() - str.length());
        }

        return str;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        var plugin = JavaPlugin.getPlugin(BukkitHelixProvider.class);
        var repoManager = plugin.getRepositoryManager();
        var repos = repoManager.getRepositories();

        for (var repo : repos.keySet()) {
            builder.suggest(repo);
        }

        return builder.buildFuture();
    }
}
