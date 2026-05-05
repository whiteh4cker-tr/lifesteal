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

public class BasicHeartDeny implements HeartPipelineHandler {

    private final Configuration heartConfig;
    private final Configuration generalConfig;

    public BasicHeartDeny(Configuration heartConfig, Configuration generalConfig) {
        this.heartConfig = heartConfig;
        this.generalConfig = generalConfig;
    }

    @Override
    public PipelineResult handle(Player player, @Nullable Entity attacker, AtomicDouble playerHearts, @Nullable AtomicDouble attackerHearts, AtomicBoolean cancel) {
        var eliminate = this.generalConfig.<Boolean>getValue("eliminatePlayers");
        if (eliminate) return PipelineResult.CONTINUE;

        var heartLossMode = this.heartConfig.<HeartLossMode>getValue("heartLossMode");
        double minHearts = this.generalConfig.<Double>getValue("minimumHearts") * 2;

        return switch (heartLossMode) {
            case ALWAYS -> {
                double amount = attacker == null
                        ? this.heartConfig.getValue("environmentHeartLoss")
                        : this.heartConfig.getValue("playerHeartLoss");

                if (playerHearts.get() - (amount * 2) <= minHearts) {
                    yield PipelineResult.CANCEL;
                }

                yield PipelineResult.CONTINUE;
            }

            case PLAYERS_ONLY -> {
                if (attacker == null) yield PipelineResult.CONTINUE;

                double amount = this.heartConfig.getValue("playerHeartLoss");
                attacker.sendMessage(" " + amount);
                attacker.sendMessage(" " + playerHearts.get());
                attacker.sendMessage(" " + minHearts);
                if (playerHearts.get() - (amount * 2) <= minHearts) {
                    yield PipelineResult.CANCEL;
                }

                yield PipelineResult.CONTINUE;
            }

            case ENVIRONMENT_ONLY -> {
                if (attacker != null) yield PipelineResult.CONTINUE;

                double amount = this.heartConfig.getValue("environmentHeartLoss");
                if (playerHearts.get() - (amount * 2) <= minHearts) {
                    yield PipelineResult.CANCEL;
                }

                yield PipelineResult.CONTINUE;
            }

            case NEVER -> PipelineResult.CANCEL;
        };
    }
}
