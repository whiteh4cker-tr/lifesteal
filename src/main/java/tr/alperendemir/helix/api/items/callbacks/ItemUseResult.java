package tr.alperendemir.helix.api.items.callbacks;

public enum ItemUseResult {

    CANCEL,
    SUCCEED,
    PASS,

    CANCEL_SWING_HAND,
    SUCCEED_SWING_HAND;

    public final boolean isSuccess() {
        return this == SUCCEED || this == SUCCEED_SWING_HAND || this == PASS;
    }

    public final boolean shouldSwingHand() {
        return this == CANCEL_SWING_HAND || this == SUCCEED_SWING_HAND;
    }

}
