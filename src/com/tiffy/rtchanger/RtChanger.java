package com.tiffy.rtchanger;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * RtChanger - Runtime Recipe Changer
 * Dynamically override Hytale crafting recipes via JSON at runtime.
 */
public class RtChanger extends JavaPlugin {

    private static final String VERSION = "1.0.0";
    private static final String LOG_PREFIX = "[RtChanger v" + VERSION + "] ";
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public RtChanger(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void start() {
        System.out.println(LOG_PREFIX + "Starting Runtime Recipe Changer...");

        // Initial apply
        loadAndApplyConfig();

        // Retry at T+5s and T+15s to ensure we catch dynamically loaded recipes
        scheduler.schedule(this::loadAndApplyConfig, 5, TimeUnit.SECONDS);
        scheduler.schedule(this::loadAndApplyConfig, 15, TimeUnit.SECONDS);
    }

    private void loadAndApplyConfig() {
        File configFile = new File("mods/tiffy/rtchanger.json");
        if (!configFile.exists()) {
            // Migration check
            File[] legacyFiles = {
                    new File("mods/tiffy/rt_overrides.json"),
                    new File("mods/tiffy/recipe_overrides.json"),
                    new File("mods/tiffy/poc_config.json")
            };
            for (File old : legacyFiles) {
                if (old.exists()) {
                    old.renameTo(configFile);
                    break;
                }
            }
            if (!configFile.exists()) {
                createDefaultConfig(configFile);
                return;
            }
        }

        try (FileReader reader = new FileReader(configFile)) {
            CustomizerConfig config = new Gson().fromJson(reader, CustomizerConfig.class);
            if (config == null || config.overrides == null)
                return;

            System.out.println(LOG_PREFIX + "Checking " + config.overrides.size() + " recipe overrides...");
            for (RecipeOverride override : config.overrides) {
                processOverride(override);
            }
        } catch (Exception e) {
            System.err.println(LOG_PREFIX + "Failed to load config: " + e.getMessage());
        }
    }

    private void processOverride(RecipeOverride override) {
        DefaultAssetMap<String, CraftingRecipe> assetMap = CraftingRecipe.getAssetMap();
        List<CraftingRecipe> targetRecipes = new ArrayList<>();

        String target = override.targetItemId;

        CraftingRecipe exact = assetMap.getAsset(target);
        if (exact != null)
            targetRecipes.add(exact);

        for (String key : assetMap.getAssetMap().keySet()) {
            if (key.startsWith(target) && key.contains("_Recipe_Generated_")) {
                CraftingRecipe r = assetMap.getAsset(key);
                if (r != null && !targetRecipes.contains(r)) {
                    targetRecipes.add(r);
                }
            }
        }

        if (targetRecipes.isEmpty())
            return;

        for (CraftingRecipe recipe : targetRecipes) {
            applyRecipeChange(recipe, override);
        }
    }

    private void applyRecipeChange(CraftingRecipe recipe, RecipeOverride override) {
        try {
            Field inputField = CraftingRecipe.class.getDeclaredField("input");
            inputField.setAccessible(true);

            List<MaterialQuantity> newInputs = new ArrayList<>();
            for (Ingredient ing : override.ingredients) {
                newInputs.add(new MaterialQuantity(ing.id, null, null, ing.amount, null));
            }

            inputField.set(recipe, newInputs.toArray(new MaterialQuantity[0]));

            AssetStore<String, CraftingRecipe, DefaultAssetMap<String, CraftingRecipe>> store = CraftingRecipe
                    .getAssetStore();
            String packId = CraftingRecipe.getAssetMap().getAssetPack(recipe.getId());
            if (packId == null)
                packId = "Hytale:Hytale";

            store.loadAssets(packId, Collections.singletonList(recipe), AssetUpdateQuery.DEFAULT, true);

            System.out.println(LOG_PREFIX + "Successfully synchronized recipe: " + recipe.getId() +
                    " (" + override.ingredients.size() + " ingredients)");

        } catch (Exception e) {
            System.err.println(LOG_PREFIX + "Error while overriding " + recipe.getId() + ": " + e.getMessage());
        }
    }

    private void createDefaultConfig(File file) {
        file.getParentFile().mkdirs();
        CustomizerConfig config = new CustomizerConfig();
        config.overrides = new ArrayList<>();

        RecipeOverride example = new RecipeOverride();
        example.targetItemId = "Jewelry_Fly_Ring";
        example.ingredients = new ArrayList<>();
        example.ingredients.add(new Ingredient("Ingredient_Bar_Iron", 100));

        config.overrides.add(example);

        try (Writer writer = new FileWriter(file)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(config, writer);
        } catch (Exception ignored) {
        }
    }

    public static class CustomizerConfig {
        public List<RecipeOverride> overrides;
    }

    public static class RecipeOverride {
        public String targetItemId;
        public List<Ingredient> ingredients;
    }

    public static class Ingredient {
        public String id;
        public int amount;

        public Ingredient() {
        }

        public Ingredient(String id, int amount) {
            this.id = id;
            this.amount = amount;
        }
    }
}
