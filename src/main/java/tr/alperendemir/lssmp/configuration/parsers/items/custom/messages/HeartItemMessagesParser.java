package tr.alperendemir.lssmp.configuration.parsers.items.custom.messages;

import tr.alperendemir.helix.api.config.Configuration;
import tr.alperendemir.helix.api.config.builder.ConfigurationBuilder;
import tr.alperendemir.helix.api.config.parsing.CompoundTypeParser;
import tr.alperendemir.lssmp.configuration.data.items.custom.messages.HeartItemMessages;
import org.jetbrains.annotations.NotNull;

public class HeartItemMessagesParser implements CompoundTypeParser<HeartItemMessages> {
    @Override
    public @NotNull Class<HeartItemMessages> complexType() {
        return HeartItemMessages.class;
    }

    @Override
    public @NotNull HeartItemMessages deserialize(@NotNull Configuration configuration) {
        return new HeartItemMessages(
                configuration.getValue("successfulUse"),
                configuration.getValue("tooManyHearts"),
                configuration.getValue("partialConsumeAvailable")
        );
    }

    @Override
    public void serialize(@NotNull Configuration configuration, @NotNull HeartItemMessages heartItemMessages) {
        configuration.value("successfulUse").value(heartItemMessages.successfulUse());
        configuration.value("tooManyHearts").value(heartItemMessages.tooManyHearts());
        configuration.value("partialConsumeAvailable").value(heartItemMessages.partialConsumeAvailable());
    }

    @Override
    public void setup(@NotNull ConfigurationBuilder configurationBuilder, @NotNull HeartItemMessages heartItemMessages) {
        configurationBuilder.value("successfulUse", heartItemMessages.successfulUse()).next();
        configurationBuilder.value("tooManyHearts", heartItemMessages.tooManyHearts()).next();
        configurationBuilder.value("partialConsumeAvailable", heartItemMessages.partialConsumeAvailable()).next();
    }
}
