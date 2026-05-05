package tr.alperendemir.helix.screens.components;

import tr.alperendemir.helix.api.logging.HelixLogger;
import tr.alperendemir.helix.api.screens.ScreenDimensions;
import tr.alperendemir.helix.api.screens.SlotPosition;
import tr.alperendemir.helix.api.screens.components.HelixComponentHandler;
import tr.alperendemir.helix.api.screens.components.HelixPageComponent;
import tr.alperendemir.helix.api.screens.components.HelixPropertyCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public class HelixScreenComponent implements HelixPageComponent {

    private final HelixComponentHandler handler;
    private final Map<String, ComponentProperty<?>> properties = new HashMap<>();
    private ScreenDimensions dimensions;
    private SlotPosition position;
    private final Runnable updateRunnable;
    private boolean dirty;

    public HelixScreenComponent(HelixComponentHandler handler, Runnable updateRunnable) {
        this.handler = handler;
        this.updateRunnable = updateRunnable;

        this.dimensions(null);
        this.position(null);
    }

    public HelixComponentHandler getHandler() {
        return this.handler;
    }

    @Override
    public ScreenDimensions dimensions() {
        return this.dimensions;
    }

    @Override
    public void dimensions(ScreenDimensions dimensions) {
        if (dimensions == null) {
            this.dimensions = new ScreenDimensions(null, 0, 0, 0);
            return;
        }

        this.dimensions = dimensions;
    }

    @Override
    public SlotPosition position() {
        return this.position;
    }

    @Override
    public void position(SlotPosition position) {
        if (position == null) {
            this.position = new SlotPosition(0, 0, 0);
            return;
        }

        this.position = position;
    }

    @Override
    public boolean dirty() {
        return this.dirty;
    }

    @Override
    public void markDirty() {
        this.dirty = true;
    }

    public void markClean() {
        this.dirty = false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getProperty(String name, @Nullable T defaultValue) {
        var property = this.properties.get(name);

        if (property == null) return defaultValue;

        try {
            return (T) property.value();
        } catch (ClassCastException e) {
            HelixLogger.reportError(e);
            return defaultValue;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> HelixPropertyCallback<T> setProperty(String name, @NotNull T value) {
        var property = this.properties.get(name);

        if (property == null) {
            var callback = new ComponentCallback<T>();
            callback.onUpdate(t -> {
                this.setProperty(name, value);
                return t;
            });
            callback.onUpdate(t -> {
                this.updateRunnable.run();
                return t;
            });

            var newProperty = new ComponentProperty<>(callback, value);

            this.properties.put(name, newProperty);

            return callback;
        }

        try {
            var casted =  ((ComponentProperty<T>) property);
            casted.value(value);
            ((ComponentCallback<T>) casted.callback()).fireUpdate(value, false);

            this.markDirty();

            return casted.callback();
        } catch (ClassCastException exception) {
            HelixLogger.reportError(exception);
            return null;
        }
    }

    private static final class ComponentCallback<T> implements HelixPropertyCallback<T> {

        private final List<UnaryOperator<T>> operators = new ArrayList<>();

        public void fireUpdate(T value, boolean fireFirst) {
            for (var operator : this.operators) {
                if (!fireFirst) {
                    fireFirst = true;
                    continue;
                }

                value = operator.apply(value);
            }
        }

        @Override
        public void fireUpdate(T value) {
            this.fireUpdate(value, true);
        }

        @Override
        public void onUpdate(UnaryOperator<T> value) {
            this.operators.add(value);
        }
    }

    private static final class ComponentProperty<T> {
        private final HelixPropertyCallback<T> callback;
        private T value;

        private ComponentProperty(HelixPropertyCallback<T> callback, T value) {
            this.callback = callback;
            this.value = value;
        }

        public HelixPropertyCallback<T> callback() {
            return callback;
        }

        public T value() {
            return this.value;
        }

        public void value(T value) {
            this.value = value;
        }
    }
}
