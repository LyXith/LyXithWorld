package org.lyxith.lyxithworld.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.lyxith.lyxithconfig.api.LyXithConfigAPI;

import static org.lyxith.lyxithworld.LyxithWorld.*;

public class MainCommand {
     static LyXithConfigAPI configAPI = getConfigAPI();
     public static LiteralCommandNode<ServerCommandSource> mainCommand = CommandManager.literal("lyxithworld")
            .executes(context -> {
                configAPI.loadConfig(modId,configName);
                context.getSource().sendFeedback(()->Text.literal(String.valueOf(configNode.getNode("helpInfo").get().getString())), false);
                return 1;
            }).build();
     public static LiteralCommandNode<ServerCommandSource> createWorld = CommandManager.literal("create")
             .then(CommandManager.argument("DimensionType", StringArgumentType.string()))
             .then(CommandManager.argument("Generator", StringArgumentType.string()))
             .then(CommandManager.argument("ShouldTickTime", BoolArgumentType.bool()))
             .executes(context -> {
                 String DimensionType = StringArgumentType.getString(context,"DimensionType");
                 String Generator = StringArgumentType.getString(context,"Generator");
                 boolean ShouldTickTime = BoolArgumentType.getBool(context,"ShouldTickTime");
                 return 1;
             }).build();
}
