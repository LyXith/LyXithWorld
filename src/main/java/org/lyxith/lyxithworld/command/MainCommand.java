package org.lyxith.lyxithworld.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.TeleportTarget;
import org.lyxith.lyxithconfig.api.LyXithConfigAPI;
import org.lyxith.lyxithworld.WorldManager;

import java.util.ArrayList;
import java.util.List;

import static org.lyxith.lyxithworld.LyxithWorld.*;

public class MainCommand {
    static LyXithConfigAPI configAPI = getConfigAPI();
    private static LiteralCommandNode<ServerCommandSource> mainCmdCreator(String cmdName) {
         return CommandManager.literal(cmdName)
                 .executes(context -> {
                     configAPI.loadConfig(modId,configName);
                     context.getSource().sendFeedback(()->Text.literal(configNode.getNode("helpInfo").get().getString().get()), false);
                     return 1;
                 }).build();
    }
    private static LiteralCommandNode<ServerCommandSource> homeCmdCreator(String cmdName) {
        return CommandManager.literal(cmdName)
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    ServerWorld world = WorldManager.getWorld(Identifier.of(nameSpace,source.getName().toLowerCase()));
                    List worldConfig = configNode.getNode("worldConfigs."+nameSpace+":"+source.getName().toLowerCase()).get().getList().get();
                    Vec3d pos = new Vec3d(0,0,0);
                    if (worldConfig.size() >= 6 && worldConfig.get(3) instanceof Double && worldConfig.get(4) instanceof Double && worldConfig.get(5) instanceof Double) {
                        pos = new Vec3d(Double.parseDouble(worldConfig.get(3).toString()),
                                Double.parseDouble(worldConfig.get(4).toString()),
                                Double.parseDouble(worldConfig.get(5).toString()));
                    } else {
                        source.sendFeedback(()->Text.literal("§cYour world wasn't set home."),false);
                    }
                    if (source.getEntity() != null) {
                        source.sendFeedback(()->Text.literal("§6Teleporting to home"),false);
                        source.getEntity().teleportTo(new TeleportTarget(world, pos, Vec3d.ZERO, 0, 0, TeleportTarget.NO_OP));
                    }
                    return 1;
                }).build();
    }
    private static LiteralCommandNode<ServerCommandSource> visitCmdCreator(String cmdName) {
        return CommandManager.literal(cmdName)
                .then(CommandManager.argument("worldId", StringArgumentType.string())
                        .executes(context -> {
                            String worldId = StringArgumentType.getString(context, "worldId");
                            ServerCommandSource source = context.getSource();
                            ServerWorld world = WorldManager.getWorld(Identifier.of(nameSpace,worldId.toLowerCase()));
                            List worldConfig = configNode.getNode("worldConfigs."+nameSpace+":"+worldId.toLowerCase()).get().getList().get();
                            Vec3d pos = new Vec3d(0,0,0);
                            if (worldConfig.size() >= 6 && worldConfig.get(3) instanceof Double && worldConfig.get(4) instanceof Double && worldConfig.get(5) instanceof Double) {
                                pos = new Vec3d(Double.parseDouble(worldConfig.get(3).toString()),
                                        Double.parseDouble(worldConfig.get(4).toString()),
                                        Double.parseDouble(worldConfig.get(5).toString()));
                            } else {
                                source.sendFeedback(()->Text.literal("§cThis world wasn't set home."),false);
                            }
                            if (source.getEntity() != null) {
                                source.sendFeedback(()->Text.literal("§6Teleporting to home"),false);
                                source.getEntity().teleportTo(new TeleportTarget(world, pos, Vec3d.ZERO, 0, 0, TeleportTarget.NO_OP));
                            }
                            return 1;})).build();
    }
    public static LiteralCommandNode<ServerCommandSource> mainCommand = mainCmdCreator("lyxithworld");
    public static LiteralCommandNode<ServerCommandSource> mainAlias1 = mainCmdCreator("lw");
    public static LiteralCommandNode<ServerCommandSource> mainAlias2 = mainCmdCreator("p");
    public static LiteralCommandNode<ServerCommandSource> worldHome = homeCmdCreator("home");
    public static LiteralCommandNode<ServerCommandSource> homeAlias = homeCmdCreator("h");
    public static LiteralCommandNode<ServerCommandSource> worldVisit = visitCmdCreator("visit");
    public static LiteralCommandNode<ServerCommandSource> visitAlias = visitCmdCreator("v");
    public static LiteralCommandNode<ServerCommandSource> createWorld = CommandManager.literal("create")
            .then(CommandManager.argument("DimensionType", StringArgumentType.string())
                    .then(CommandManager.argument("Generator", StringArgumentType.string())
                            .then(CommandManager.argument("ShouldTickTime", BoolArgumentType.bool())
                                    .executes(MainCommand::createWorld)
                                    .then(CommandManager.argument("worldName", StringArgumentType.string())
                                            .executes(MainCommand::createWorld)))))
            .build();
    public static LiteralCommandNode<ServerCommandSource> loadWorld = CommandManager.literal("load")
            .executes(context -> {
                String worldId = context.getSource().getName().toLowerCase();
                try {
                    WorldManager.loadWorld(Identifier.of(nameSpace,worldId));
                } catch (Exception e) {
                    LOGGER.warn(e.getMessage());
                    context.getSource().sendFeedback(() -> Text.literal("Error:"+e.getMessage()),false);
                    return 0;
                }
                context.getSource().sendFeedback(() -> Text.literal(worldId+ " world loaded."),false);
                List<String> worlds = configNode.getNode("worlds").get().getList().get();
                worlds.add(Identifier.of(nameSpace,worldId).toString());
                configNode.initNode("worlds",false,worlds);
                configAPI.saveConfig(modId,configName,configNode);
                return 1;
            })
            .then(CommandManager.argument("worldId", IdentifierArgumentType.identifier())
                    .executes(context -> {
                        try {
                            WorldManager.loadWorld(IdentifierArgumentType.getIdentifier(context,"worldId"));
                        } catch (Exception e) {
                            LOGGER.warn(e.getMessage());
                            context.getSource().sendFeedback(() -> Text.literal("Error:"+e.getMessage()),false);
                            return 0;
                        }
                        context.getSource().sendFeedback(() -> Text.literal(IdentifierArgumentType.getIdentifier(context,"worldId")+ " world loaded."),false);
                        List<String> worlds = configNode.getNode("worlds").get().getList().get();
                        worlds.add(IdentifierArgumentType.getIdentifier(context,"worldId").toString());
                        worlds.set(3,"0");
                        worlds.set(4,"8");
                        worlds.set(5,"0");
                        configNode.initNode("worlds",false,worlds);
                        configAPI.saveConfig(modId,configName,configNode);
                        return 1;
                    })).build();
    public static LiteralCommandNode<ServerCommandSource> unloadWorld = CommandManager.literal("unload")
            .executes(context -> {
                String worldId = context.getSource().getName().toLowerCase();
                try {
                    WorldManager.unloadWorld(Identifier.of(nameSpace,worldId));
                } catch (Exception e) {
                    LOGGER.warn(e.getMessage());
                    context.getSource().sendFeedback(() -> Text.literal("Error:"+e.getMessage()),false);
                    return 0;
                }
                context.getSource().sendFeedback(() -> Text.literal(worldId+ " world unloaded."),false);
                List<String> worlds = configNode.getNode("worlds").get().getList().get();
                worlds.remove(Identifier.of(nameSpace,worldId).toString());
                configNode.initNode("worlds",true,worlds);
                configAPI.saveConfig(modId,configName,configNode);
                return 1;
            })
            .then(CommandManager.argument("worldId", IdentifierArgumentType.identifier())
                    .executes(context -> {
                        try {
                            WorldManager.unloadWorld(IdentifierArgumentType.getIdentifier(context,"worldId"));
                        } catch (Exception e) {
                            LOGGER.warn(e.getMessage());
                            context.getSource().sendFeedback(() -> Text.literal("Error:"+e.getMessage()),false);
                            return 0;
                        }
                        context.getSource().sendFeedback(() -> Text.literal(IdentifierArgumentType.getIdentifier(context,"worldId")+ " world unloaded."),false);
                        List<String> worlds = configNode.getNode("worlds").get().getList().get();
                        worlds.remove(IdentifierArgumentType.getIdentifier(context,"worldId").toString());
                        configNode.initNode("worlds",true,worlds);
                        configAPI.saveConfig(modId,configName,configNode);
                        return 1;
                    })).build();
    public static LiteralCommandNode<ServerCommandSource> delWorld = CommandManager.literal("delete")
            .executes(context -> {
                String worldId = context.getSource().getName().toLowerCase();
                try {
                    WorldManager.delWorld(Identifier.of(nameSpace,worldId));
                } catch (Exception e) {
                    LOGGER.warn(e.getMessage());
                    context.getSource().sendFeedback(() -> Text.literal("Error:"+e.getMessage()),false);
                    return 0;
                }
                context.getSource().sendFeedback(() -> Text.literal(worldId+ " world deleted."),false);
                List<String> worlds = configNode.getNode("worlds").get().getList().get();
                worlds.remove(Identifier.of(nameSpace,worldId).toString());
                configNode.delNode("worldConfigs."+ Identifier.of(nameSpace,worldId));
                configNode.initNode("worlds",true,worlds);
                configAPI.saveConfig(modId,configName,configNode);
                return 1;
            })
            .then(CommandManager.argument("worldId", IdentifierArgumentType.identifier())
                    .executes(context -> {
                        try {
                            WorldManager.delWorld(IdentifierArgumentType.getIdentifier(context,"worldId"));
                        } catch (Exception e) {
                            LOGGER.warn(e.getMessage());
                            context.getSource().sendFeedback(() -> Text.literal("Error:"+e.getMessage()),false);
                            return 0;
                        }
                        context.getSource().sendFeedback(() -> Text.literal(IdentifierArgumentType.getIdentifier(context,"worldId")+ " world deleted."),false);
                        List<String> worlds = configNode.getNode("worlds").get().getList().get();
                        worlds.remove(IdentifierArgumentType.getIdentifier(context,"worldId").toString());
                        configNode.delNode("worldConfigs."+IdentifierArgumentType.getIdentifier(context,"worldId"));
                        configNode.initNode("worlds",true,worlds);
                        configAPI.saveConfig(modId,configName,configNode);
                        return 1;
                    })).build();
    private static int createWorld(CommandContext<ServerCommandSource> context) {
        String dimensionType = StringArgumentType.getString(context, "DimensionType");
        String generator = StringArgumentType.getString(context, "Generator");
        boolean shouldTickTime = BoolArgumentType.getBool(context, "ShouldTickTime");
        String worldName;
        try {
            worldName = StringArgumentType.getString(context, "worldName");
        } catch (IllegalArgumentException e) {
            // 如果 worldName 参数不存在，使用玩家名称
            worldName = context.getSource().getName().toLowerCase();
        }
        Identifier worldId = Identifier.of(nameSpace,worldName);
        if (isWorldExist(worldId)) {
            context.getSource().sendFeedback(() -> Text.literal("You already have a world!"),false);
            return 0;
        }
        String finalWorldName = worldName;
        context.getSource().sendFeedback(() -> Text.literal("World created"+" dimensionType:"+dimensionType+" generator:"+generator+" shouldTickTime:"+shouldTickTime+" worldName:"+ finalWorldName +"."), false);
        WorldManager.createWorld(dimensionType,generator,shouldTickTime,worldName);
        List<String> worldConfig = new ArrayList<>();
        List<String> worlds = configNode.getNode("worlds").get().getList().get();
        worldConfig.add(dimensionType);
        worldConfig.add(generator);
        worldConfig.add(String.valueOf(shouldTickTime));
        configNode.initNode("worldConfigs."+worldId,false,worldConfig);
        worlds.add(worldId.toString());
        configNode.initNode("worlds",false,worlds);
        configAPI.saveConfig(modId,configName,configNode);
        return 1;
    }
    public static LiteralCommandNode<ServerCommandSource> worldGamerule = CommandManager.literal("gamerule")
            .executes(context -> {
                context.getSource().sendFeedback(()-> Text.literal("/lw gamerule list \n /lw gamerule query \n /lw gamerule set"),false);
                return 1;
            }).build();
    public static LiteralCommandNode<ServerCommandSource> wgList = CommandManager.literal("list")
            .executes(context -> {
                ServerWorld world = context.getSource().getWorld();
                GameRules gameRules = world.getGameRules();
                gameRules.accept(new GameRules.Visitor() {
                    @Override
                    public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                        GameRules.Rule<T> rule = gameRules.get(key);
                        String value = rule.serialize();

                        context.getSource().sendFeedback(() -> Text.literal("§6" + key.getName() + "§7: §a" + value),false);
                    }
                });
                return 1;
            }).build();
    public static LiteralCommandNode<ServerCommandSource> wgQuery = CommandManager.literal("query")
            .then(CommandManager.argument("gamerule",StringArgumentType.string())
                    .suggests(GameRuleSuggestionProvider::getSuggestions)
                    .executes(context -> {
                        ServerWorld world = context.getSource().getWorld();
                        GameRules gameRules = world.getGameRules();
                        String inputRuleName = StringArgumentType.getString(context, "gamerule");

                        // 查找匹配的游戏规则Key
                        final GameRules.Key<?>[] foundKey = new GameRules.Key<?>[1];
                        gameRules.accept(new GameRules.Visitor() {
                            @Override
                            public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                                if (key.getName().equals(inputRuleName)) {
                                    foundKey[0] = key;
                                }
                            }
                        });

                        if (foundKey[0] != null) {
                            // 获取规则值并发送反馈
                            GameRules.Rule<?> rule = gameRules.get(foundKey[0]); // 使用get方法获取规则对象
                            String value = rule.serialize(); // 将规则值序列化为字符串
                            context.getSource().sendFeedback(() -> Text.literal("§6"+foundKey[0].getName() + "§7: §a" + value), false);
                        } else {
                            context.getSource().sendFeedback(() -> Text.literal("§cCan't find gamerule§7: §a" + inputRuleName), false);
                        }
                        return 1;
                    })).build();
    public static LiteralCommandNode<ServerCommandSource> wgSet = CommandManager.literal("set")
            .then(CommandManager.argument("gamerule", StringArgumentType.string())
                    .suggests(GameRuleSuggestionProvider::getSuggestions)
                    .then(CommandManager.argument("value", StringArgumentType.string())
                            .executes(context -> {
                                ServerWorld world = context.getSource().getWorld();
                                GameRules gameRules = world.getGameRules();
                                String inputRuleName = StringArgumentType.getString(context, "gamerule");
                                String inputValue = StringArgumentType.getString(context, "value");

                                // 查找匹配的游戏规则Key和Type
                                final GameRules.Key<?>[] foundKey = new GameRules.Key<?>[1];
                                final GameRules.Type<?>[] foundType = new GameRules.Type<?>[1];

                                gameRules.accept(new GameRules.Visitor() {
                                    @Override
                                    public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                                        if (key.getName().equals(inputRuleName)) {
                                            foundKey[0] = key;
                                            foundType[0] = type;
                                        }
                                    }
                                });

                                if (foundKey[0] != null && foundType[0] != null) {
                                    try {
                                        // 设置规则值
                                        GameRules.Rule<?> rule = gameRules.get(foundKey[0]);
                                        boolean success = setRuleValue(rule, inputValue, context.getSource().getServer());

                                        if (success) {
                                            String newValue = rule.serialize();
                                            context.getSource().sendFeedback(() ->
                                                    Text.literal("§6" + foundKey[0].getName() + "§7 is set to §a" + newValue), false);
                                        } else {
                                            context.getSource().sendFeedback(() ->
                                                    Text.literal("§cInvalid value: §7" + inputValue), false);
                                        }
                                    } catch (Exception e) {
                                        context.getSource().sendFeedback(() ->
                                                Text.literal("§cSet failed: §7" + e.getMessage()), false);
                                    }
                                } else {
                                    context.getSource().sendFeedback(() ->
                                            Text.literal("§cCan't find gamerule: §7" + inputRuleName), false);
                                }
                                return 1;
                            }))).build();
    private static boolean setRuleValue(GameRules.Rule<?> rule, String value, MinecraftServer server) {
        try {
            if (rule instanceof GameRules.BooleanRule) {
                if ("true".equalsIgnoreCase(value)) {
                    ((GameRules.BooleanRule) rule).set(true, server);
                    return true;
                } else if ("false".equalsIgnoreCase(value)) {
                    ((GameRules.BooleanRule) rule).set(false, server);
                    return true;
                } else {
                    return false;
                }
            } else if (rule instanceof GameRules.IntRule) {
                int intValue = Integer.parseInt(value);
                ((GameRules.IntRule) rule).set(intValue, server);
                return true;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return false;
    }
    public static LiteralCommandNode<ServerCommandSource> worldSetHome = CommandManager.literal("sethome")
            .executes(context -> {
                ServerCommandSource source = context.getSource();
                ServerWorld world = WorldManager.getWorld(Identifier.of(nameSpace,source.getName().toLowerCase()));
                if (!(source.getWorld() == world)) {
                    source.sendFeedback(()->Text.literal("Please sethome in your world"),false);
                    return 0;
                }
                List worldConfig = configNode.getNode("worldConfigs."+nameSpace+":"+source.getName().toLowerCase()).get().getList().get();
                Vec3d pos = source.getPosition();
                while (worldConfig.size() <= 6) {
                    worldConfig.add(0); // 填充默认值
                }
                worldConfig.set(3,pos.x);
                worldConfig.set(4,pos.y);
                worldConfig.set(5,pos.z);
                configNode.getNode("worldConfigs."+nameSpace+":"+source.getName().toLowerCase()).get().set(worldConfig);
                configAPI.saveConfig(modId,configName,configNode);
                source.sendFeedback(()->Text.literal("Your home is set to "+pos.x+" "+pos.y+" "+pos.z),false);
                return 1;
            }).build();
}
