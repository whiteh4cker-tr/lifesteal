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

public class BasicHeartGain implements HeartPipelineHandler {

    private final Configuration heartConfig;

    public BasicHeartGain(Configuration heartConfig) {
        this.heartConfig = heartConfig;
    }

    @Override
    public PipelineResult handle(Player player, @Nullable Entity attacker, AtomicDouble playerHearts, @Nullable AtomicDouble attackerHearts, AtomicBoolean cancel) {
        var heartLossMode = this.heartConfig.<HeartLossMode>getValue("heartLossMode");

        return switch (heartLossMode) {
            case PLAYERS_ONLY, ALWAYS -> {
                if (attacker == null) yield PipelineResult.CONTINUE;
                assert attackerHearts != null;

                double amount = this.heartConfig.getValue("playerHeartLoss");
                attackerHearts.addAndGet(amount * 2);

                yield PipelineResult.CONTINUE;
            }

            case ENVIRONMENT_ONLY, NEVER -> PipelineResult.CONTINUE;
        };
    }
}
