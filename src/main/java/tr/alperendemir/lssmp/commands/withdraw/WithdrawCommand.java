package tr.alperendemir.lssmp.commands.withdraw;

import com.google.common.util.concurrent.AtomicDouble;
import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.commands.CommandExecutionResult;
import tr.alperendemir.helix.api.commands.HelixCommand;
import tr.alperendemir.helix.api.commands.HelixCommandParameters;
import tr.alperendemir.helix.api.commands.arguments.ArgumentList;
import tr.alperendemir.helix.api.config.Configuration;
import tr.alperendemir.helix.api.namespaced.UniqueIdentifier;
import tr.alperendemir.helix.api.plugins.HelixPlugin;
import tr.alperendemir.lssmp.elimination.EliminationManager;
import tr.alperendemir.lssmp.pipeline.heart.HeartPipeline;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class WithdrawCommand extends HelixCommand {

    private final Configuration configuration;
    private final HeartPipeline pipeline;
    private final EliminationManager eliminationManager;

    public WithdrawCommand(Configuration configuration, HeartPipeline pipeline, EliminationManager eliminationManager) {
        this.configuration = configuration;
        this.pipeline = pipeline;
        this.eliminationManager = eliminationManager;
    }

    @Override
    protected HelixCommandParameters makeParameters() {
        return HelixCommandParameters.create(this.configuration.getValue("name"))
                .permission(this.configuration.getValue("permission"));
    }

    @Override
    public CommandExecutionResult handlePlayerSender(@NotNull Player player, @NotNull ArgumentList args) {
        final var maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH));
        var hearts = new AtomicDouble(maxHealth.getBaseValue());
        var cancel = new AtomicBoolean();
        this.pipeline.fire(player, null, hearts, null, cancel);

        if (cancel.get()) {
            if (hearts.get() < 0 && this.configuration.<Boolean>getValue("allowSelfElimination")) {
                this.eliminationManager.tryEliminate(player, player);
                return CommandExecutionResult.FAILURE;
            }

            player.sendMessage("§cYou can't eliminate yourself!");
            return CommandExecutionResult.FAILURE;
        }

        maxHealth.setBaseValue(hearts.get());

        final var id = this.configuration.<String>getValue("heartItem");
        final var plugin = HelixPlugin.getProvidingPlugin(this.getClass());

        final var uniqueId = UniqueIdentifier.plugin(plugin, id);

        var stack = Helix.items().createItem(uniqueId, null);
        var res = player.getInventory().addItem(stack);
        for (var result : res.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), result);
        }

        player.sendMessage("§aSuccessfully withdrawn a heart!");
        return CommandExecutionResult.HANDLED;
    }
}
