package tr.alperendemir.lssmp.configuration.parsers.recipes;

import tr.alperendemir.helix.api.config.Configuration;
import tr.alperendemir.helix.api.config.builder.ConfigurationBuilder;
import tr.alperendemir.helix.api.config.parsing.CompoundTypeParser;
import tr.alperendemir.helix.api.items.display.ItemDisplayData;
import tr.alperendemir.lssmp.configuration.data.recipes.RecipeConfiguration;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class RecipeConfigurationParser implements CompoundTypeParser<RecipeConfiguration> {

    @Override
    public @NotNull Class<RecipeConfiguration> complexType() {
        return RecipeConfiguration.class;
    }

    @Override
    public @NotNull RecipeConfiguration deserialize(@NotNull Configuration configuration) {
        return new RecipeConfiguration(
                configuration.getValue("shaped"),
                configuration.getCompoundArray("choices")
        );
    }

    @Override
    public void serialize(@NotNull Configuration configuration, @NotNull RecipeConfiguration recipeConfiguration) {
        configuration.value("shaped").value(recipeConfiguration.shaped());
        configuration.compoundArray("choices").values(recipeConfiguration.choices());
    }

    @Override
    public void setup(@NotNull ConfigurationBuilder configurationBuilder, @NotNull RecipeConfiguration recipeConfiguration) {
        configurationBuilder.value("shaped", true).next();
        configurationBuilder.compoundArray("choices", recipeConfiguration.choices(), new RecipeChoiceParser(metaItem ->
                new ItemDisplayData(Material.CHARCOAL))).next();
    }
}
