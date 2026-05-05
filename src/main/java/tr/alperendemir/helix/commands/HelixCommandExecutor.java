package tr.alperendemir.helix.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.messages.colors.MinecraftColor;
import tr.alperendemir.helix.api.commands.HelixCommand;
import tr.alperendemir.helix.api.messages.MessageBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;

public record HelixCommandExecutor(HelixCommand helixCommand) implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        var commandLine = label;
        if (args.length > 0) {
            commandLine = commandLine + " " + String.join(" ", args);
        }

        this.handleCommandExecution(Helix.commands().getDispatcher(), sender, commandLine);
        return true;
    }

    public void handleCommandExecution(CommandDispatcher<CommandSender> dispatcher, CommandSender sender, String commandLine) {
        var parsed = dispatcher.parse(commandLine, sender);
        var exceptions = parsed.getExceptions();

        if (!exceptions.isEmpty()) {
            for (var entry : exceptions.entrySet()) {
                handleError(sender, entry.getValue());
            }

            return;
        }

        try {
            dispatcher.execute(parsed);
        } catch (CommandSyntaxException e) {
            handleError(sender, e);
        }
    }

    private void handleError(@NotNull CommandSender sender, @NotNull CommandSyntaxException exception) {
        var input = exception.getInput();
        if (input == null) {
            var sw = new StringWriter();
            var writer = new PrintWriter(sw);
            exception.fillInStackTrace().printStackTrace(writer);

            var message = new TextComponent("An internal error occurred while executing this command.");

            var lines = sw
                    .getBuffer()
                    .toString()
                    .lines()
                    .map(s ->new Text(new MessageBuilder().literal(s).color(MinecraftColor.WHITE).buildArray()))
                    .toArray(Text[]::new);

            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, lines));

            sender.spigot().sendMessage(message);
            return;
        }

        var message = exception.getRawMessage().getString();
        var sb = new StringBuilder();
        var cursor = Math.min(input.length(), exception.getCursor());

        if (cursor > CommandSyntaxException.CONTEXT_AMOUNT) {
            sb.append("...");
        }

        sb.append("§7").append(input, Math.max(0, cursor - CommandSyntaxException.CONTEXT_AMOUNT), cursor);
        sb.append("§c§n").append(input.substring(cursor));
        sb.append("§c§o").append("<--[HERE]");

        sender.spigot().sendMessage(new MessageBuilder().translatable(message).color(MinecraftColor.RED).buildArray());
        sender.sendMessage(sb.toString());
    }
}
