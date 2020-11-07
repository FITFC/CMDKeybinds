package net.kyrptonaught.cmdkeybind;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.kyrptonaught.cmdkeybind.MacroTypes.*;
import net.kyrptonaught.cmdkeybind.config.ConfigOptions;
import net.kyrptonaught.kyrptconfig.config.ConfigManager;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class CmdKeybindMod implements ClientModInitializer {
    public static final String MOD_ID = "cmdkeybind";
    public static ConfigManager.SingleConfigManager config = new ConfigManager.SingleConfigManager(MOD_ID, new ConfigOptions());

    public static List<BaseMacro> macros = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        config.load();
        if (getConfig().macros.size() == 0) addEmptyMacro();
        buildMacros();
        ClientTickEvents.START_WORLD_TICK.register(clientWorld ->
        {
            if (MinecraftClient.getInstance().currentScreen == null) {
                long hndl = MinecraftClient.getInstance().getWindow().getHandle();
                long curTime = System.currentTimeMillis();
                for (BaseMacro macro : macros)
                    macro.tick(hndl, MinecraftClient.getInstance().player, curTime);
            }
        });
    }

    public static ConfigOptions getConfig() {
        return (ConfigOptions) config.getConfig();
    }

    public static void buildMacros() {
        macros.clear();
        ConfigOptions options = getConfig();
        if (options.enabled)
            for (ConfigOptions.ConfigMacro macro : options.macros) {
                if (macro.macroType == null) macro.macroType = BaseMacro.MacroType.SingleUse;
                switch (macro.macroType) {
                    case Delayed:
                        macros.add(new DelayedMacro(macro.keyName, macro.keyModName, macro.command, macro.delay));
                        break;
                    case Repeating:
                        macros.add(new RepeatingMacro(macro.keyName, macro.keyModName, macro.command, macro.delay));
                        break;
                    case SingleUse:
                        macros.add(new SingleMacro(macro.keyName, macro.keyModName, macro.command));
                        break;
                    case DisplayOnly:
                        macros.add(new DisplayMacro(macro.keyName, macro.keyModName, macro.command));
                        break;
                    case ToggledRepeating:
                        macros.add(new ToggledRepeating(macro.keyName, macro.keyModName, macro.command, macro.delay));
                        break;
                }
            }
    }

    public static void addEmptyMacro() {
        getConfig().macros.add(new ConfigOptions.ConfigMacro());
        buildMacros();
        config.save();
    }
}
