package tr.alperendemir.helix.api.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.tree.CommandNode;
import tr.alperendemir.helix.api.commands.arguments.ArgumentList;
import tr.alperendemir.helix.api.logging.HelixLogger;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class HelixCommandParameters {

    private static final MethodHandle ARGUMENT_HANDLE;

    static {
        try {
            var lookup = MethodHandles.privateLookupIn(CommandContext.class, MethodHandles.lookup());
            ARGUMENT_HANDLE = lookup.findGetter(CommandContext.class, "arguments", Map.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private final HelixCommandParameters parent;
    private final String name;

    private final Map<String, CommandNode<CommandSender>> children = new HashMap<>();
    private final Map<String, ArgumentType<?>> arguments = new LinkedHashMap<>();
    private HelixCommand redirect;
    private HelixCommand executor;
    private String permission;

    private HelixCommandParameters(HelixCommandParameters parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public static HelixCommandParameters create(String name) {
        return new HelixCommandParameters(null, Objects.requireNonNull(name));
    }

    public String getName() {
        return this.name;
    }

    public HelixCommand getExecutor() {
        return this.executor;
    }

    public HelixCommandParameters permission(String permission) {
        this.permission = permission;
        return this;
    }

    public HelixCommandParameters childCommand(HelixCommand executor) {
        var params = executor.getParameters();
        this.children.put(params.getName(), params);
        return this;
    }

    public HelixCommandParameters redirect(HelixCommand other) {
        this.redirect = other;
        return this;
    }

    public HelixCommandParameters parent() {
        return this.parent;
    }

    public HelixCommandParameters argument(String name, ArgumentType<?> type) {
        this.arguments.put(name, type);
        return this;
    }

    public HelixCommandParameters executor(HelixCommand executor) {
        this.executor = executor;
        return this;
    }

    public String getPermission() {
        return this.permission;
    }

    public CommandNode<CommandSender> build(HelixCommand command) {
        var currentNode = LiteralArgumentBuilder.<CommandSender>literal(this.name);
        if (this.permission != null && !this.permission.isBlank()) {
            currentNode.requires(sender -> sender.hasPermission(this.permission));
        }

        for (var child : this.children.values()) {
            currentNode.then(child);
        }

        CommandNode<CommandSender> firstArgument = null;
        CommandNode<CommandSender> currentArgument = null;

        var iterator = this.arguments.entrySet().iterator();
        if (iterator.hasNext()) {
            var current = arg(iterator.next());

            if (!iterator.hasNext()) {
                this.setExecute(current, command);
            }

            currentArgument = current.build();
            firstArgument = currentArgument;
        }

        while (iterator.hasNext()) {
            var next = arg(iterator.next());

            if (!iterator.hasNext()) {
                this.setExecute(next, command);
            }

            var built = next.build();

            currentArgument.addChild(built);
            currentArgument = built;
        }

        if (firstArgument != null) {
            currentNode.then(firstArgument);
        } else {
            this.setExecute(currentNode, command);
        }
        return currentNode.build();
    }

    private void setExecute(ArgumentBuilder<CommandSender, ? extends ArgumentBuilder<CommandSender, ?>> node, HelixCommand command) {
        if (this.redirect != null) {
            if (this.redirect == command) {
                node.redirect(LiteralArgumentBuilder.<CommandSender>literal(this.name).build(), ctx -> {
                    this.handleExecution(ctx);
                    return ctx.getSource();
                });
                return;
            }

            node.redirect(this.redirect.getParameters(), ctx -> {
                this.handleExecution(ctx);
                return ctx.getSource();
            });
            return;
        }

        node.executes(ctx -> {
            this.handleExecution(ctx);
            return 1;
        });
    }

    private <S extends CommandSender> RequiredArgumentBuilder<S, ?> arg(Map.Entry<String, ArgumentType<?>> entry) {
        return arg(entry.getKey(), entry.getValue());
    }

    private <S extends CommandSender, T> RequiredArgumentBuilder<S, T> arg(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    @SuppressWarnings("unchecked")
    private void handleExecution(CommandContext<CommandSender> context) {
        if (this.executor == null) {
            return;
        }

        try {
            var map = (Map<String, ParsedArgument<CommandSender, ?>>) ARGUMENT_HANDLE.invoke(context);
            var list = new ArgumentList(map);

            var sender = context.getSource();
            var result = this.executor.handleGenericSender(sender, list);
            if (result == CommandExecutionResult.HANDLED || result == CommandExecutionResult.FAILURE) {
                return;
            }

            if (sender instanceof Player player) {
                this.executor.handlePlayerSender(player, list);
                return;
            }

            if (sender instanceof ConsoleCommandSender consoleCommandSender) {
                this.executor.handleConsoleSender(consoleCommandSender, list);
                return;
            }

            if (sender instanceof BlockCommandSender blockCommandSender) {
                this.executor.handleBlockSender(blockCommandSender, list);
            }
        } catch (Throwable e) {
            HelixLogger.reportError(e);
        }
    }

}
