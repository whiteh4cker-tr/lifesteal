package tr.alperendemir.helix.api.screens;

public record SlotPosition(int slot, int x, int y) {

    public static SlotPosition fromSlot(int slot, ScreenDimensions dimensions) {
        return new SlotPosition(slot, slot % dimensions.width(), slot / dimensions.width());
    }

    public static SlotPosition fromXY(int x, int y, ScreenDimensions dimensions) {
        return new SlotPosition(y * dimensions.width() + x, x, y);
    }

    public static SlotPosition zero() {
        return new SlotPosition(0, 0, 0);
    }

    public SlotPosition top(ScreenDimensions dimensions) {
        return fromXY(this.x(), 0, dimensions);
    }

    public SlotPosition left(ScreenDimensions dimensions) {
        return fromXY(0, this.y(), dimensions);
    }

    public SlotPosition bottom(ScreenDimensions dimensions) {
        return fromXY(this.x(), dimensions.height(), dimensions);
    }

    public SlotPosition right(ScreenDimensions dimensions) {
        return fromXY(dimensions.width(), this.y(), dimensions);
    }

    public SlotPosition topLeft(ScreenDimensions dimensions) {
        return fromXY(0, 0, dimensions);
    }

    public SlotPosition bottomLeft(ScreenDimensions dimensions) {
        return fromXY(0, dimensions.height(), dimensions);
    }

    public SlotPosition topRight(ScreenDimensions dimensions) {
        return fromXY(dimensions.width(), 0, dimensions);
    }

    public SlotPosition bottomRight(ScreenDimensions dimensions) {
        return fromXY(dimensions.width(), dimensions.height(), dimensions);
    }

    public SlotPosition centerX(ScreenDimensions dimensions) {
        return fromXY(dimensions.width() / 2, this.y(), dimensions);
    }

    public SlotPosition centerY(ScreenDimensions dimensions) {
        return fromXY(0, dimensions.height() / 2, dimensions);
    }

    public SlotPosition center(ScreenDimensions dimensions) {
        return fromXY(dimensions.width() / 2, dimensions.height() / 2, dimensions);
    }
}
