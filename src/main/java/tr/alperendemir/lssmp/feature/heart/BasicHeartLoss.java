package tr.alperendemir.lssmp.feature.heart;

import com.google.common.util.concurrent.AtomicDouble;
import tr.alperendemir.helix.api.config.Configuration;
import tr.alperendemir.lssmp.configuration.data.types.HeartLossMode;
import tr.alperendemir.lssmp.pipeline.PipelineResult;
import tr.alperendemir.lssmp.pipeline.heart.HeartPipelineHandler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

public class BasicHeartLoss implements HeartPipelineHandler {

    private final Configuration heartConfig;

    public BasicHeartLoss(Configuration heartConfig) {
        this.heartConfig = heartConfig;
    }

    @Override
    public PipelineResult handle(Player player, @Nullable Entity attacker, AtomicDouble playerHearts, @Nullable AtomicDouble attackerHearts, AtomicBoolean cancel) {
        var heartLossMode = this.heartConfig.<HeartLossMode>getValue("heartLossMode");
        return switch (heartLossMode) {
            case ALWAYS -> {
                double amount = attacker == null
                        ? this.heartConfig.getValue("environmentHeartLoss")
                        : this.heartConfig.getValue("playerHeartLoss");

                playerHearts.addAndGet(-(amount * 2));

                yield PipelineResult.CONTINUE;
            }

            case PLAYERS_ONLY -> {
                if (attacker == null) yield PipelineResult.CANCEL;

                double amount = this.heartConfig.getValue("playerHeartLoss");
                playerHearts.addAndGet(-(amount * 2));

                yield PipelineResult.CONTINUE;
            }

            case ENVIRONMENT_ONLY -> {
                if (attacker != null) yield PipelineResult.CANCEL;

                double amount = this.heartConfig.getValue("environmentHeartLoss");
                playerHearts.addAndGet(-(amount * 2));

                yield PipelineResult.CONTINUE;
            }

            case NEVER -> PipelineResult.CANCEL;
        };
    }
}
