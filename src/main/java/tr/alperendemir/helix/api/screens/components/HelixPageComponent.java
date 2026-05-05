package tr.alperendemir.helix.api.screens.components;

import tr.alperendemir.helix.api.screens.ScreenDimensions;
import tr.alperendemir.helix.api.screens.SlotPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HelixPageComponent {
    
    ScreenDimensions dimensions();

    void dimensions(final ScreenDimensions dimensions);

    SlotPosition position();

    void position(final SlotPosition position);

    /**
     * A component being dirty indicates that it should be re-rendered.
     *
     * @return The dirty state of this component
     * */
    boolean dirty();

    /**
     * Makes this component dirty.
     *
     * @see HelixPageComponent#dirty()
     * */
    void markDirty();

    /**
     * Returns a slot position using this component's dimensions. This is a convenience method.
     * Can be used to convert an index to x/y.
     *
     * @param x The x (column)
     * @param y The y (row)
     * @return A slot position with the provided x and y coordinates, relative to this component.
     * */
    default SlotPosition position(int x, int y) {
        return SlotPosition.fromXY(x, y, this.dimensions());
    }

    /**
     * Returns a slot position using this component's dimensions. This is a convenience method.
     * Can be used to convert x/y to an index.
     *
     * @param slot A position relative to this component.
     * @return A slot position with the provided slot as coordinates, will populate the x and y fields.
     * */
    default SlotPosition position(int slot) {
        return SlotPosition.fromSlot(slot, this.dimensions());
    }

    /**
     * Gets a property from this component.
     *
     * @param name The name of the property you wish to get
     * @param defaultValue The default value for the property, or null.
     * @return The value of the property if present, otherwise returns the default value.
     * @see HelixComponentContext
     */
    @Nullable
    <T> T getProperty(String name, @Nullable final T defaultValue);

    /**
     * Sets a property and creates (or, if already exists, returns) a callback to handle the property.
     *
     * @param name The name of the property you wish to set
     * @param value The value for this property
     * @return A callable to update the property.
     * @see HelixComponentContext
     */
    <T> HelixPropertyCallback<T> setProperty(String name, @NotNull final T value);

}
