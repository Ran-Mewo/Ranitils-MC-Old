package io.github.ran.minecraft.ranitils.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.*;


@Config(name = "ranitils")
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip // In the code this might be called anyArmor
    public boolean wearableItems = true;

    @ConfigEntry.Gui.Tooltip
    public boolean verticalTP = false;

    @ConfigEntry.Gui.Tooltip
    public boolean autoSwitchElytra = true;

    @ConfigEntry.Gui.Tooltip
    public boolean roadRunner = false;

    public static void init() {
        AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);
    }

    public static ModConfig getInstance() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public static ConfigHolder<?> getConfigHolder() {
        return AutoConfig.getConfigHolder(ModConfig.class);
    }
}