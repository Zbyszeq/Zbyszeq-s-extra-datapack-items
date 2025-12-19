package com.zbyszeq;

import com.google.gson.Gson;
import com.zbyszeq.config.DummyItemConfig;
import com.zbyszeq.config.ItemEntry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;

import net.minecraft.world.item.component.Unbreakable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;


public class ZbyszeqsExtraDatapackItems implements ModInitializer {

    public static final String MOD_ID = "xtra_datapack_items";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Gson GSON = new Gson();


    private record ToolStats(Tier tier, int damage, float speed) {}

    @Override
    public void onInitialize() {
        Path path = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("xtra_datapack_items.json");

        DummyItemConfig cfg = loadConfig(path);
        int count = 0;



        for (ItemEntry entry : cfg.items) {
            Map<String, Object> props = entry.properties == null
                    ? Map.of()
                    : entry.properties;

            try {
                ResourceLocation id = ResourceLocation.parse(entry.id);

                if (BuiltInRegistries.ITEM.containsKey(id)) {
                    LOGGER.warn("Item {} already exists, skipping", id);
                    continue;
                }

                String type = entry.type == null ? "item" : entry.type.toLowerCase();

                Item item = switch (type) {

                    case "item" -> new Item(new Item.Properties());
                    case "sword" -> {
                        ToolStats stat = getItemProperties(props);
                        Item.Properties itemProps = applyUnbreakable(new Item.Properties().attributes(
                                SwordItem.createAttributes(stat.tier, stat.damage, stat.speed)), props
                        );
                        yield new SwordItem(stat.tier, itemProps);
                    }
                    case "pickaxe" ->{
                        ToolStats stat = getItemProperties(props);
                        Item.Properties itemProps = applyUnbreakable(new Item.Properties().attributes(
                                PickaxeItem.createAttributes(stat.tier, stat.damage, stat.speed)), props
                        );
                        yield new PickaxeItem(stat.tier, itemProps);
                    }
                    case "axe" ->{
                        ToolStats stat = getItemProperties(props);
                        Item.Properties itemProps = applyUnbreakable(new Item.Properties().attributes(
                                AxeItem.createAttributes(stat.tier, stat.damage, stat.speed)), props
                        );                        yield new AxeItem(stat.tier, itemProps);
                    }
                    case "shovel" ->{
                        ToolStats stat = getItemProperties(props);
                        Item.Properties itemProps = applyUnbreakable(new Item.Properties().attributes(
                                ShovelItem.createAttributes(stat.tier, stat.damage, stat.speed)), props
                        );
                        yield new ShovelItem(stat.tier, itemProps);
                    }
                    case "hoe" ->{
                        ToolStats stat = getItemProperties(props);
                        Item.Properties itemProps = applyUnbreakable(new Item.Properties().attributes(
                                HoeItem.createAttributes(stat.tier, stat.damage, stat.speed)), props
                        );
                        yield new HoeItem(stat.tier, itemProps);
                    }
                    case "food" -> {
                        int nutrition = ((Number) props.getOrDefault("nutrition", 0)).intValue();
                        float saturation = ((Number) props.getOrDefault("saturation", 0)).floatValue();
                        yield new Item(new Item.Properties().food(new FoodProperties.Builder()
                                        .nutrition(nutrition)
                                        .saturationModifier(saturation)
                                        .build()));
                    }
                    case "bow" -> {
                        Item.Properties itemProps = applyUnbreakable(new Item.Properties(), props
                        );
                        yield new BowItem(itemProps);
                    }
                    case "crossbow" -> {
                        Item.Properties itemProps = applyUnbreakable(new Item.Properties(), props
                        );
                        yield new CrossbowItem(itemProps);
                    }

                    default -> {
                        LOGGER.warn("Unknown item type {}, using basic item", type);
                        yield new Item(new Item.Properties());
                    }
                };

                Registry.register(BuiltInRegistries.ITEM, id, item);
                count++;

            } catch (Exception e) {
                LOGGER.error("Failed to register item {}", entry.id, e);
            }
        }

        LOGGER.info("Registered {} custom datapack items", count);
    }

    private static ToolStats getItemProperties(Map<String, Object> props) {
        if (props == null) {
            return new ToolStats(Tiers.IRON, 0, 0f);
        }

        String tierName = props.getOrDefault("tier", "iron").toString();
        Tier tier = getTierFromString(tierName);

        int damage = ((Number) props.getOrDefault("damage", 0)).intValue();
        float speed = ((Number) props.getOrDefault("speed", 0)).floatValue();

        return new ToolStats(tier, damage, speed);
    }

//    private static Item.Properties applyDurability(
//            Item.Properties props,
//            Map<String, Object> cfg
//    ) {
//        if (cfg == null || !cfg.containsKey("durability")) {
//            LOGGER.info("Item has no durability field, skipping.");
//            return props;
//        }
//
//        int durability = ((Double) cfg.get("durability")).intValue();
//        // durability <= 0 → unbreakable
//        if (durability <= 0) {
//            LOGGER.info("Item is now unbreakable");
//            return props.component(DataComponents.UNBREAKABLE,  new Unbreakable(true));
//        }
//        LOGGER.info("Item has now max {} uses.", durability);
//        return props.durability(durability);
//    }

    private static Item.Properties applyUnbreakable(
            Item.Properties props,
            Map<String, Object> cfg
    ){
        if (cfg == null || !cfg.containsKey("unbreakable")) {
            LOGGER.debug("Item has no unbreakable field, skipping.");
            return props;
        }

        boolean itemIsUnbreakable = (boolean) cfg.get("unbreakable");

        if(itemIsUnbreakable){
            return props.component(DataComponents.UNBREAKABLE, new Unbreakable(true));
        }
        return props;
    }


    private static Tier getTierFromString(String string){
        switch (string){
            case "wood" -> {return Tiers.WOOD;}
            case "stone" -> {return Tiers.STONE;}
            case "iron" -> {return Tiers.IRON;}
            case "gold" -> {return Tiers.GOLD;}
            case "diamond" -> {return Tiers.DIAMOND;}
            case "netherite" -> {return Tiers.NETHERITE;}
        }
        return Tiers.IRON;
    }

    /* ---------------- CONFIG ---------------- */

    private static DummyItemConfig loadConfig(Path path) {
        try {
            if (Files.notExists(path)) {
                DummyItemConfig def = DummyItemConfig.defaultConfig();
                Files.createDirectories(path.getParent());
                Files.writeString(path, GSON.toJson(def));
                return def;
            }

            try (Reader r = Files.newBufferedReader(path)) {
                return GSON.fromJson(r, DummyItemConfig.class);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load config", e);
            return new DummyItemConfig();
        }
    }
}
