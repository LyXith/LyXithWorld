package org.lyxith.lyxithworld;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.lyxith.lyxithconfig.api.LyXithConfigAPI;
import org.lyxith.lyxithconfig.api.LyXithConfigNodeImpl;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.lyxith.lyxithworld.command.MainCommand.*;

public class LyxithWorld implements ModInitializer {
    private static final String defaultHelpInfo = "test help information";
    public static LyXithConfigNodeImpl configNode = new LyXithConfigNodeImpl();
    public static final String modId = "LyXithWorld";
    public static final String nameSpace = "lyxithworld";
    public static final String configName = "World";
    public static LyXithConfigAPI configAPI;
    public static MinecraftServer server;
    public static final Logger LOGGER = LogUtils.getLogger();

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
            loadAllWorlds();
        });

        CommandRegistrationCallback.EVENT.register((dispatcher,registryAccess,environment) -> {
            commandRegister(dispatcher,mainCommand);
        });
    }
    private void commandRegister(CommandDispatcher<ServerCommandSource> dispatcher, LiteralCommandNode<ServerCommandSource> command) {
        command.addChild(createWorld);
        command.addChild(load);
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
        configNode = configAPI.getConfigRootNode(modId,configName).getRoot();
        configNode.initNode("helpInfo", false, defaultHelpInfo);
        configNode.addNode("worldConfigs",false);
        configAPI.saveConfig(modId,configName,configNode);
    }
    public static LyXithConfigAPI getConfigAPI() {
        return configAPI;
    }
    public static boolean isWorldExist(Identifier key) {
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, key);

        ServerWorld world = server.getWorld(worldKey);
        return world != null;
    }
    private static void loadAllWorlds() {
        List worlds = configNode.getNode("worlds").get().getList().get();
        for (Object world : worlds) {
            WorldManager.loadWorld(world.toString());
        }
    }
}
