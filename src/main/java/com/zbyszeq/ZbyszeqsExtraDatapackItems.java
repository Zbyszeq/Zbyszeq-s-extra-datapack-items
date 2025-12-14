package com.zbyszeq;

import com.google.gson.Gson;
import com.zbyszeq.DummyItemConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class ZbyszeqsExtraDatapackItems implements ModInitializer {

    public static final String MOD_ID = "xtra_datapack_items";
    private static final Gson GSON = new Gson();
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        Path configPath = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("xtra_datapack_items.json");

        DummyItemConfig config = loadConfig(configPath);

        if (config.items == null) {
            config.items = List.of();
        }

        int registered = 0;

        for (String idString : config.items) {
            try {
                ResourceLocation id = ResourceLocation.parse(idString);

                if (BuiltInRegistries.ITEM.containsKey(id)) {
                    LOGGER.warn("Item '{}' already exists, skipping", id);
                    continue;
                }

                Item item = new Item(new Item.Properties());
                Registry.register(BuiltInRegistries.ITEM, id, item);
                registered++;

            } catch (Exception e) {
                LOGGER.error("Failed to register dummy item '{}'", idString, e);
            }
        }


        LOGGER.info("Registered {} datapack dummy items", registered);
    }

    private static DummyItemConfig loadConfig(Path path) {
        try {
            if (Files.notExists(path)) {
                DummyItemConfig defaultConfig = DummyItemConfig.defaultConfig();
                Files.createDirectories(path.getParent());
                Files.writeString(
                        path,
                        GSON.toJson(defaultConfig),
                        StandardOpenOption.CREATE
                );
                return defaultConfig;
            }

            try (Reader reader = Files.newBufferedReader(path)) {
                return GSON.fromJson(reader, DummyItemConfig.class);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to load config, no items will be registered", e);
            return new DummyItemConfig();
        }
    }
}
