package org.lyxith.lyxithworld;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.lyxith.lyxithconfig.api.LyXithConfigAPI;
import org.lyxith.lyxithconfig.api.LyXithConfigNodeImpl;
import org.slf4j.Logger;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.HashMap;
import java.util.List;

import static org.lyxith.lyxithworld.command.MainCommand.*;

public class LyxithWorld implements ModInitializer {
    private static final String defaultHelpInfo = "test help information";
    public static final LyXithConfigNodeImpl configNode = new LyXithConfigNodeImpl();
    public static final String modId = "LyXithWorld";
    public static final String configName = "World";
    public static LyXithConfigAPI configAPI;
    public static MinecraftServer server;
    public static final Logger LOGGER = LogUtils.getLogger();
    private final HashMap<Identifier, RuntimeWorldHandle> worlds = new HashMap<>();

    @Override
    public void onInitialize() {
        List<LyXithConfigAPI> apiInstances = FabricLoader.getInstance()
                .getEntrypoints("lyxithconfig-api", LyXithConfigAPI.class);

        if (apiInstances.isEmpty()) {
            System.err.println("LyXithConfig API 入口点未找到，可能是版本不兼容");
        } else if (apiInstances.size() == 1) {
            configAPI = apiInstances.getFirst();
        }
        initConfig();
        ServerLifecycleEvents.SERVER_STARTED.register((s) -> {
            server = s;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher,registryAccess,environment) -> {
            commandRegister(dispatcher,mainCommand);
        });
    }
    private void commandRegister(CommandDispatcher<ServerCommandSource> dispatcher, LiteralCommandNode<ServerCommandSource> command) {
        command.addChild(createWorld);
        dispatcher.getRoot().addChild(command);
    }
    private void initConfig() {
        if (!configAPI.modConfigDirExist(modId)) {
            configAPI.createModConfigDir(modId);
        }
        if (!configAPI.modConfigExist(modId,configName)) {
            configAPI.createModConfig(modId,configName);
        }
        configAPI.loadConfig(modId,configName);
        configNode.initNode("helpInfo", false, defaultHelpInfo);
        configAPI.saveConfig(modId,configName,configNode);
    }
    public static LyXithConfigAPI getConfigAPI() {
        return  configAPI;
    }
}
