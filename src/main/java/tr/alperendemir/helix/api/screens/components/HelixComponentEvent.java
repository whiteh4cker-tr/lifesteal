package tr.alperendemir.helix.api.screens.components;

import tr.alperendemir.helix.api.screens.ScreenAction;
import tr.alperendemir.helix.api.screens.SlotPosition;
import org.bukkit.entity.Player;

public interface HelixComponentEvent {

    ScreenAction action();

    Player player();

    SlotPosition clickPosition();

    boolean cancelled();

    void cancel();

}
