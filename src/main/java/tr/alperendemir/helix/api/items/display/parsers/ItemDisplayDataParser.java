package tr.alperendemir.helix.api.items.display.parsers;

import tr.alperendemir.helix.api.config.Configuration;
import tr.alperendemir.helix.api.config.builder.ConfigurationBuilder;
import tr.alperendemir.helix.api.config.parsing.CompoundTypeParser;
import tr.alperendemir.helix.api.items.display.ItemDisplayData;
import tr.alperendemir.helix.api.items.display.ItemTextDisplayData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemDisplayDataParser implements CompoundTypeParser<ItemDisplayData> {
    @Override
    public @NotNull Class<ItemDisplayData> complexType() {
        return ItemDisplayData.class;
    }

    @Override
    public @NotNull ItemDisplayData deserialize(@NotNull Configuration value) {
        return new ItemDisplayData(
                value.getValue("material"),
                value.getValue("customModelData"),
                new ItemTextDisplayData(
                        value.getValue("displayName"),
                        List.of(value.getValueArray("lore"))
                )
        );
    }

    @Override
    public void serialize(@NotNull Configuration section, @NotNull ItemDisplayData value) {
        section.value("material").value(value.material());
        section.value("customModelData").value(value.customModelData());
        section.value("displayName").value(value.textDisplayData().displayName());
        section.valueArray("lore").values(value.textDisplayData().lore().toArray(new String[0]));
    }

    @Override
    public void setup(@NotNull ConfigurationBuilder template, @NotNull ItemDisplayData value) {
        template.value("material", value.material()).next();
        template.value("customModelData", value.customModelData()).next();
        template.value("displayName", value.textDisplayData().displayName()).next();
        template.valueArray("lore", value.textDisplayData().lore().toArray(new String[0])).next();
    }
}
