package com.zbyszeq.config;

import java.util.ArrayList;
import java.util.List;

public class DummyItemConfig {
    public List<ItemEntry> items = new ArrayList<>();

    public static DummyItemConfig defaultConfig() {
        DummyItemConfig cfg = new DummyItemConfig();

        ItemEntry example = new ItemEntry();
        example.id = "example:debug_sword";
        example.type = "sword";

        cfg.items.add(example);
        return cfg;
    }
}
