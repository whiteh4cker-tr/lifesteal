package tr.alperendemir.helix.api.messages;

import tr.alperendemir.helix.api.messages.colors.MinecraftColor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Content;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MessageBuilder {

    private final List<BaseComponent> components = new ArrayList<>();

    public MessageBuilder translatable(String key, Object... formatting) {
        append(new TranslatableComponent(key, formatting));
        return this;
    }

    public MessageBuilder literal(String text, Object... formatting) {
        if (formatting.length > 0) {
            text = String.format(text, formatting);
        }

        append(new TextComponent(text));
        return this;
    }

    public MessageBuilder legacy(String text, Object... formatting) {
        if (formatting.length > 0) {
            text = String.format(text, formatting);
        }

        append(new TextComponent(TextComponent.fromLegacyText(text)));
        return this;
    }

    public MessageBuilder bold(boolean bold) {
        last().setBold(bold);
        return this;
    }

    public MessageBuilder underlined(boolean underlined) {
        last().setUnderlined(underlined);
        return this;
    }

    public MessageBuilder italic(boolean italic) {
        last().setItalic(italic);
        return this;
    }

    public MessageBuilder click(ClickEvent.Action action, String value) {
        last().setClickEvent(new ClickEvent(action, value));
        return this;
    }

    public MessageBuilder hover(HoverEvent.Action action, Content... contents) {
        last().setHoverEvent(new HoverEvent(action, contents));
        return this;
    }

    public MessageBuilder color(MinecraftColor color) {
        last().setColor(color.isHex() ? ChatColor.of(new Color(color.getHexColor())) : ChatColor.getByChar(color.getChar()));
        return this;
    }

    public BaseComponent[] buildArray() {
        return this.components.toArray(BaseComponent[]::new);
    }

    public BaseComponent build() {
        return new TextComponent(this.buildArray());
    }

    private BaseComponent last() {
        if (this.components.isEmpty()) {
            throw new IllegalStateException("Cannot edit a message builder with no messages!");
        }

        return this.components.get(this.components.size() - 1);
    }

    private void append(BaseComponent component) {
        this.components.add(component);
    }

}
