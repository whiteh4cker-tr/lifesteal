package tr.alperendemir.lssmp.commands.reset;

import tr.alperendemir.helix.api.commands.HelixCommand;
import tr.alperendemir.helix.api.config.Configuration;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public abstract class ResetCommandBase extends HelixCommand {

    protected void reset(Player player, Configuration generalConfig) {
        var def = generalConfig.<Double>getValue("defaultHearts");
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(def * 2);
    }

}
