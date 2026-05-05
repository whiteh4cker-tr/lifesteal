package tr.alperendemir.helix.config.components;

import tr.alperendemir.helix.api.config.ConfigComponent;
import tr.alperendemir.helix.api.config.ConfigEntry;
import tr.alperendemir.helix.api.config.components.TreeConfigComponent;
import tr.alperendemir.helix.api.config.errors.ConfigError;
import tr.alperendemir.helix.api.config.errors.ExceptionConfigError;
import tr.alperendemir.helix.api.config.errors.MissingValueConfigError;
import tr.alperendemir.helix.api.config.errors.TreeConfigError;
import tr.alperendemir.helix.api.logging.HelixLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleTreeConfigComponent implements TreeConfigComponent {

    private final ConfigEntry entry;
    private final TreeConfigComponent parent;

    private final String id;

    private final Map<String, ConfigComponent> children = new LinkedHashMap<>();
    private final Map<String, ConfigComponent> childrenView = Collections.unmodifiableMap(this.children);

    public SimpleTreeConfigComponent(ConfigEntry entry, TreeConfigComponent parent, String id) {
        this.entry = entry == null ? this : null;
        this.parent = parent;
        this.id = id;
    }

    @Override
    public @Nullable TreeConfigComponent parent() {
        return this.parent;
    }

    @Override
    public @NotNull Map<String, ConfigComponent> components() {
        return this.childrenView;
    }

    @Override
    public TreeConfigComponent addComponent(@NotNull final ConfigComponent child) {
        this.children.put(child.id(), child);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ConfigComponent> T component(@NotNull String id) {
        return (T) this.children.get(id);
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
    public @NotNull String path() {
        var sb = new StringBuilder();

        var top = this.parent;
        while (top != null) {
            sb.insert(0, top.id());
            sb.insert(top.id().length(), '.');
            top = top.parent();
        }

        return sb.toString();
    }

    @Override
    public void write(@NotNull final StringBuilder builder, int indent) {
        var childIndent = this.parent() != null ? indent + 1 : indent;

        for (var child : this.children.values()) {
            child.write(builder, childIndent);
        }
    }

    @Override
    public void writeError(@NotNull StringBuilder builder, @NotNull ConfigError error, int indent) {
        if (error instanceof MissingValueConfigError) {
            error = new TreeConfigError(new HashMap<>());
            for (var child : this.children.values()) {
                if (!child.canWriteError()) continue;

                ((TreeConfigError) error).childErrors().put(child.id(), new MissingValueConfigError());
            }
        }

        var tree = (TreeConfigError) error;

        var childIndent = this.parent() != null ? indent + 1 : indent;

        for (var child : this.childrenView.values()) {
            if (!child.canWriteError()) continue;

            var current = tree.childErrors().get(child.id());

            if (current == null) {
                child.write(builder, childIndent);
                continue;
            }

            child.writeError(builder, current, childIndent);
        }
    }

    @Override
    public ConfigError read(@NotNull final Map<?, ?> data) {
        var dataValue = data.get("value");
        var dataMap = dataValue instanceof Map<?,?> map ? map : data;

        var errors = new HashMap<String, ConfigError>();

        for (var child : this.childrenView.values()) {
            if (!child.canRead()) continue;

            var value = dataMap.get(child.id());
            if (value == null) {
                errors.put(child.id(), new MissingValueConfigError());
                continue;
            }

            try {
                var childError = child.read(Map.of("value", value));
                if (childError == null) continue;

                errors.put(child.id(), childError);
            } catch (Exception e) {
                HelixLogger.reportError(e);
                errors.put(child.id(), new ExceptionConfigError(e));
            }
        }

        return errors.isEmpty() ? null : new TreeConfigError(errors);
    }
}
