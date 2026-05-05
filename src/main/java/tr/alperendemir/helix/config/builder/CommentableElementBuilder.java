package tr.alperendemir.helix.config.builder;

import tr.alperendemir.helix.api.config.ConfigEntry;
import tr.alperendemir.helix.api.config.builder.ElementBuilder;
import tr.alperendemir.helix.config.components.SimpleCommentConfigComponent;
import tr.alperendemir.helix.config.components.SpaceConfigComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class CommentableElementBuilder implements ElementBuilder {

    private final SimpleCommentConfigComponent component;
    private ConfigEntry entry;

    public CommentableElementBuilder(ConfigEntry entry, String name) {
        this.component = new SimpleCommentConfigComponent(entry, name + "____comments");
        this.entry = entry;
    }

    @Override
    public @NotNull ElementBuilder comment(@Nullable String comment) {
        this.setup();
        this.component.comment(comment);
        return this;
    }

    @Override
    public @NotNull <Z extends Enum<Z>> ElementBuilder commentEnum(Class<Z> enumClass) {
        this.setup();
        this.component.commentEnum(enumClass);
        return this;
    }

    @Override
    public @NotNull <Z extends Enum<Z>> ElementBuilder commentEnum(Class<Z> enumClass, Z max) {
        this.setup();
        this.component.commentEnum(enumClass, max);
        return this;
    }

    @Override
    public @NotNull List<String> comments() {
        this.setup();
        return this.component.comments();
    }

    @Override
    public @NotNull ElementBuilder commentSpace() {
        this.setup();
        this.component.commentSpace();
        return this;
    }

    private void setup() {
        if (this.entry == null) return;

        if (!this.entry.components().isEmpty()) {
            this.entry.addComponent(new SpaceConfigComponent(this.entry));
        }

        this.entry.addComponent(this.component);
        this.entry = null;
    }
}
