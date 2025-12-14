package com.zbyszeq;

import org.spongepowered.include.com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

public class DummyItemConfig extends JsonElement {

    public List<String> items = new ArrayList<>();

    public static DummyItemConfig defaultConfig() {
        DummyItemConfig cfg = new DummyItemConfig();
        cfg.items.add("example:dummy_item");
        return cfg;
    }
}
