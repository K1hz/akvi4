package minecraft.system.capes;

import minecraft.system.capes.config.Config;

public class CapesBase {
    public static CapesBase INSTANCE;
    public static Config config;

    public void init() {
        INSTANCE = this;
        config = new Config();
    }
}
