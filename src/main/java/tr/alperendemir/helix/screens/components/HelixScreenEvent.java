package tr.alperendemir.helix.screens.components;

import tr.alperendemir.helix.api.screens.ScreenAction;
import tr.alperendemir.helix.api.screens.SlotPosition;
import tr.alperendemir.helix.api.screens.components.HelixComponentEvent;
import org.bukkit.entity.Player;

public class HelixScreenEvent implements HelixComponentEvent {

    private final ScreenAction action;
    private final Player player;
    private final SlotPosition position;
    private boolean cancelled = false;

    public HelixScreenEvent(ScreenAction action, Player player, SlotPosition position) {
        this.action = action;
        this.player = player;
        this.position = position;
    }

    @Override
    public ScreenAction action() {
        return this.action;
    }

    @Override
    public Player player() {
        return this.player;
    }

    @Override
    public SlotPosition clickPosition() {
        return this.position;
    }

    @Override
    public boolean cancelled() {
        return this.cancelled;
    }

    @Override
    public void cancel() {
        this.cancelled = true;
    }
}
