package tr.alperendemir.lssmp.feature.heart;

import com.google.common.util.concurrent.AtomicDouble;
import tr.alperendemir.helix.api.config.Configuration;
import tr.alperendemir.lssmp.pipeline.PipelineResult;
import tr.alperendemir.lssmp.pipeline.heart.HeartPipelineHandler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

public class BasicElimination implements HeartPipelineHandler {

    private final Configuration generalConfig;

    public BasicElimination(Configuration generalConfig) {
        this.generalConfig = generalConfig;
    }

    @Override
    public PipelineResult handle(Player player, @Nullable Entity attacker, AtomicDouble playerHearts, @Nullable AtomicDouble attackerHearts, AtomicBoolean cancel) {
        double minHearts = this.generalConfig.<Double>getValue("minimumHearts") * 2;
        var eliminate = this.generalConfig.<Boolean>getValue("eliminatePlayers");

        if (eliminate && playerHearts.get() <= minHearts) {
            playerHearts.set(-1);
            cancel.set(true);
        }

        return PipelineResult.CANCEL;
    }
}
