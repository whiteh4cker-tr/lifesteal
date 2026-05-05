package tr.alperendemir.helix.api.commands.parsers;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class FileParser implements ArgumentType<File> {

    private final File folder;
    private final Predicate<File> namePredicate;

    public FileParser(File folder, Predicate<File> namePredicate) {
        this.folder = folder;
        this.namePredicate = namePredicate;
    }

    @Override
    public File parse(StringReader reader) throws CommandSyntaxException {
        var str = reader.readUnquotedString();
        var file = new File(this.folder, str);
        if (!file.exists()) {
            var msg = new LiteralMessage("Unknown file '" + str + "'");
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg, reader.getString(), reader.getCursor() - str.length());
        }

        if(!this.namePredicate.test(file)) {
            var msg = new LiteralMessage("Invalid file name '" + str + "'");
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg, reader.getString(), reader.getCursor() - str.length());
        }

        return file;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        var files = this.folder.listFiles();
        if (files == null) {
            return builder.buildFuture();
        }

        for (var file : files) {
            if (this.namePredicate.test(file)) {
                builder.suggest(file.getName());
            }
        }

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return List.of("file");
    }
}
