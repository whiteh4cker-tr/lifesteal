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
import tr.alperendemir.helix.api.plugins.HelixPlugin;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HelixPluginParser implements ArgumentType<HelixPlugin> {

    public enum LoadedFlag {
        LOADED {
            @Override
            public void checkParse(HelixPlugin plugin, boolean enabled, String id) throws CommandSyntaxException {
                if (plugin == null) {
                    this.throwError("Helix Plugin with id '" + id + "' id is not loaded!");
                }
            }

            @Override
            protected void suggest(SuggestionsBuilder builder) {
                var plugins = Helix.pluginLoader().getPlugins();
                for (var entry : plugins.entrySet()) {
                    if (entry.getValue().isBuiltin()) continue;

                    builder.suggest(entry.getKey());
                }
            }
        },
        ENABLED {
            @Override
            public void checkParse(HelixPlugin plugin, boolean enabled, String id) throws CommandSyntaxException {
                LoadedFlag.LOADED.checkParse(plugin, enabled, id);

                if (!enabled) {
                    this.throwError("Helix Plugin with id '" + id + "' is is not enabled!");
                }
            }

            @Override
            protected void suggest(SuggestionsBuilder builder) {
                var plugins = Helix.pluginLoader().getPlugins();
                for (var entry : plugins.entrySet()) {
                    var plugin = entry.getValue();
                    if (plugin.isBuiltin()) continue;

                    if (plugin.getBukkitPlugin().isEnabled()) {
                        builder.suggest(entry.getKey());
                    }
                }
            }
        },
        DISABLED {
            @Override
            public void checkParse(HelixPlugin plugin, boolean enabled, String id) throws CommandSyntaxException {
                LoadedFlag.LOADED.checkParse(plugin, enabled, id);

                if (enabled) {
                    this.throwError("Helix Plugin with id '" + id + "' is is not disabled!");
                }
            }

            @Override
            protected void suggest(SuggestionsBuilder builder) {
                var plugins = Helix.pluginLoader().getPlugins();
                for (var entry : plugins.entrySet()) {
                    var plugin = entry.getValue();
                    if (plugin.isBuiltin()) continue;

                    if (!plugin.getBukkitPlugin().isEnabled()) {
                        builder.suggest(entry.getKey());
                    }
                }
            }
        };

        public void checkParse(HelixPlugin plugin, boolean enabled, String id) throws CommandSyntaxException {

        }

        protected void suggest(SuggestionsBuilder builder) {

        }

        protected void throwError(String message) throws CommandSyntaxException {
            var msg = new LiteralMessage(message);
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
        }
    }

    private final LoadedFlag loadedFlag;

    public HelixPluginParser(LoadedFlag loadedFlag) {
        this.loadedFlag = loadedFlag;
    }

    public LoadedFlag getLoadedFlag() {
        return this.loadedFlag;
    }

    @Override
    public HelixPlugin parse(StringReader reader) throws CommandSyntaxException {
        var plugins = Helix.pluginLoader().getPlugins();
        var pluginId = reader.readUnquotedString();
        var helixPlugin = plugins.get(pluginId);
        if(helixPlugin != null && helixPlugin.isBuiltin()) {
            var msg = new LiteralMessage("Helix Plugin with id '" + pluginId + "' cannot be modified as it is a builtin plugin!");
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg, reader.getString(), reader.getCursor() - pluginId.length());
        }

        var enabled = helixPlugin != null && helixPlugin.getBukkitPlugin().isEnabled();

        try {
            this.loadedFlag.checkParse(helixPlugin, enabled, pluginId);
        } catch (CommandSyntaxException exception) {
            throw new CommandSyntaxException(exception.getType(), exception.getRawMessage(), reader.getString(), reader.getCursor() - pluginId.length());
        }
        return helixPlugin;
    }

    @Override
    public Collection<String> getExamples() {
        return List.of("helix-plugin");
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        this.loadedFlag.suggest(builder);
        return builder.buildFuture();
    }
}
