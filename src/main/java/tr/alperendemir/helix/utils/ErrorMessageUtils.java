package tr.alperendemir.helix.utils;

import tr.alperendemir.helix.api.messages.colors.MinecraftColor;
import tr.alperendemir.helix.api.messages.MessageBuilder;
import org.bukkit.command.CommandSender;

public final class ErrorMessageUtils {

    private ErrorMessageUtils() {

    }

    public static void sendErrorMessage(CommandSender sender, String translatableKey, boolean gray, String message, Object... args) {
        var msg = new MessageBuilder()
                .translatable(translatableKey, args)
                .color(MinecraftColor.RED)

                .literal("\n")

                .literal(message)
                .color(gray ? MinecraftColor.GRAY : MinecraftColor.RED)
                .underlined(!gray)

                .translatable("command.context.here")
                .color(MinecraftColor.RED)
                .italic(true)
                .buildArray();

        sender.spigot().sendMessage(msg);
    }

}
