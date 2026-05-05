package tr.alperendemir.helix.config.components;

import tr.alperendemir.helix.api.config.ConfigEntry;
import tr.alperendemir.helix.api.config.components.CommentableConfigComponent;
import tr.alperendemir.helix.api.config.errors.ConfigError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SimpleCommentConfigComponent implements CommentableConfigComponent {

    private final List<String> comments = new LinkedList<>();
    private final List<String> commentView = Collections.unmodifiableList(this.comments);
    private final ConfigEntry entry;
    private final String id;

    public SimpleCommentConfigComponent(ConfigEntry entry, String id) {
        this.entry = entry;
        this.id = id;
    }

    @Override
    public @NotNull List<String> comments() {
        return this.commentView;
    }

    @Override
    public @NotNull CommentableConfigComponent comment(@Nullable String comment) {
        this.comments.add(comment);
        return this;
    }

    @Override
    public @NotNull CommentableConfigComponent commentSpace() {
        return this.comment(null);
    }

    @Override
    public @NotNull <T extends Enum<T>> CommentableConfigComponent commentEnum(Class<T> enumClass) {
        if (!enumClass.isEnum()) throw new IllegalArgumentException("#commentEnum parameter must be an enum class!");

        this.comment("Possible values:");
        for (var value : enumClass.getEnumConstants()) {
            this.comment(" - " + value.toString());
        }

        return this;
    }

    @Override
    public @NotNull <T extends Enum<T>> CommentableConfigComponent commentEnum(Class<T> enumClass, T max) {
        if (!enumClass.isEnum()) throw new IllegalArgumentException("#commentEnum parameter must be an enum class!");

        this.comment("Possible values:");
        for (var value : enumClass.getEnumConstants()) {
            this.comment(" - " + value.toString());
            if (value == max) break;
        }

        return this;
    }

    @Override
    public @NotNull ConfigEntry entry() {
        return this.entry;
    }

    @Override
    public @NotNull String id() {
        return this.id;
    }

    @Override
    public void write(@NotNull final StringBuilder builder, int indent) {
        var str = "    ".repeat(indent);

        for (var comment : this.comments) {
            builder.append(str).append('#');

            if (comment == null) {
                builder.append('\n');
                continue;
            }

            builder.append(' ').append(comment).append('\n');
        }
    }

    @Override
    public void writeError(@NotNull StringBuilder builder, @NotNull ConfigError errors, int indent) {
        // Empty
    }

    @Override
    public ConfigError read(@NotNull final Map<?, ?> data) {
        return null; // We don't read comments
    }

    @Override
    public boolean canWriteError() {
        return false;
    }

    @Override
    public boolean canRead() {
        return false;
    }
}
