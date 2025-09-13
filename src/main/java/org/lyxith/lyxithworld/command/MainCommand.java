package org.lyxith.lyxithworld.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lyxith.lyxithconfig.api.LyXithConfigAPI;
import org.lyxith.lyxithworld.CreateWorld;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

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
                                        String Owner =context.getSource().getName();
                                        if (isWorldExist(Identifier.of(modId,Owner))) {
                                            context.getSource().sendFeedback(() -> Text.literal("You already have a world!"),false);
                                        }
                                        context.getSource().sendFeedback(() -> Text.literal("World created"+" dimensionType:"+dimensionType+" generator:"+generator+" shouldTickTime:"+shouldTickTime+" Owner:"+Owner+"."), false);
                                        CreateWorld world = new CreateWorld(dimensionType,generator,shouldTickTime,Owner);
                                        RuntimeWorldHandle worldHandle = world.getWorldHandle();
                                        worlds.put(Identifier.of(modId,Owner), worldHandle);
                                        return 1;
                                    }))))
            .build();
}
