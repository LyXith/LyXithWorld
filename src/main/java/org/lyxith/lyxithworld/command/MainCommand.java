package org.lyxith.lyxithworld.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lyxith.lyxithconfig.api.LyXithConfigAPI;
import org.lyxith.lyxithworld.WorldManager;

import java.util.ArrayList;
import java.util.List;

import static org.lyxith.lyxithworld.LyxithWorld.*;

public class MainCommand {
     static LyXithConfigAPI configAPI = getConfigAPI();
     public static LiteralCommandNode<ServerCommandSource> mainCommand = CommandManager.literal("lyxithworld")
            .executes(context -> {
                configAPI.loadConfig(modId,configName);
                context.getSource().sendFeedback(()->Text.literal(configNode.getNode("helpInfo").get().getString().get()), false);
                return 1;
            }).build();
    public static LiteralCommandNode<ServerCommandSource> createWorld = CommandManager.literal("create")
            .then(CommandManager.argument("DimensionType", StringArgumentType.string())
                    .then(CommandManager.argument("Generator", StringArgumentType.string())
                            .then(CommandManager.argument("ShouldTickTime", BoolArgumentType.bool())
                                    .executes(context -> {
                                        String dimensionType = StringArgumentType.getString(context, "DimensionType");
                                        String generator = StringArgumentType.getString(context, "Generator");
                                        boolean shouldTickTime = BoolArgumentType.getBool(context, "ShouldTickTime");
                                        String Owner =context.getSource().getName().toLowerCase();
                                        if (isWorldExist(Identifier.of(nameSpace,Owner))) {
                                            context.getSource().sendFeedback(() -> Text.literal("You already have a world!"),false);
                                            return 0;
                                        }
                                        context.getSource().sendFeedback(() -> Text.literal("World created"+" dimensionType:"+dimensionType+" generator:"+generator+" shouldTickTime:"+shouldTickTime+" Owner:"+Owner+"."), false);
                                        WorldManager.createWorld(dimensionType,generator,shouldTickTime,Owner);
                                        List<String> worldConfig = new ArrayList<>();
                                        List worlds = configNode.getNode("worlds").get().getList().get();
                                        worldConfig.add(dimensionType);
                                        worldConfig.add(generator);
                                        worldConfig.add(String.valueOf(shouldTickTime));
                                        configNode.initNode("worldConfigs."+Owner,false,worldConfig);
                                        worlds.add(Owner);
                                        configNode.initNode("worlds",false,worlds);
                                        configAPI.saveConfig(modId,configName,configNode);
                                        return 1;
                                    }))))
            .build();
    public static LiteralCommandNode<ServerCommandSource> load = CommandManager.literal("load")
            .executes(context -> {
                try {
                    WorldManager.loadWorld(context.getSource().getName());
                } catch (Exception e) {
                    LOGGER.warn(e.getMessage());
                    context.getSource().sendFeedback(() -> Text.literal("Error:"+e.getMessage()),false);
                    return 0;
                }
                context.getSource().sendFeedback(() -> Text.literal(context.getSource().getName()+ "'s world loaded."),false);
                return 1;
            })
            .then(CommandManager.argument("Owner",StringArgumentType.string())
                    .executes(context -> {
                        try {
                            WorldManager.loadWorld(StringArgumentType.getString(context,"Owner"));
                        } catch (Exception e) {
                            LOGGER.warn(e.getMessage());
                            context.getSource().sendFeedback(() -> Text.literal("Error:"+e.getMessage()),false);
                            return 0;
                        }
                        context.getSource().sendFeedback(() -> Text.literal(StringArgumentType.getString(context,"Owner")+ "'s world loaded."),false);
                        return 1;
                    })).build();
}
