package org.lyxith.lyxithworld;

import net.minecraft.block.Blocks;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorLayer;
import org.lyxith.lyxithconfig.api.LyXithConfigNode;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

import java.util.List;
import java.util.Optional;

import static net.minecraft.world.GameRules.*;
import static org.lyxith.lyxithworld.LyxithWorld.*;

public class WorldManager {
    private static RuntimeWorldHandle worldHandle;

    public static void createWorld(String dimensionType, String generatorType, boolean shouldTickTime, String worldName) {
        try {
            LOGGER.info("=== 开始创建世界 ===");

            // 检查服务器实例
            if (server == null) {
                throw new IllegalStateException("服务器实例为 null");
            }

            // 创建世界标识符
            Identifier worldId = Identifier.of("lyxithworld", worldName.toLowerCase());
            LOGGER.info("世界标识符: {}", worldId);

            // 创建世界配置
            RuntimeWorldConfig config = createWorldConfig(dimensionType, generatorType, shouldTickTime);

            // 获取 Fantasy 实例并创建世界
            Fantasy fantasy = Fantasy.get(server);
            LOGGER.info("创建持久化世界: {}", worldId);

            worldHandle = fantasy.getOrOpenPersistentWorld(worldId, config);

            LOGGER.info("世界创建成功: {}", worldId);

        } catch (Exception e) {
            LOGGER.error("创建世界失败", e);
            throw new RuntimeException("创建世界失败: " + e.getMessage(), e);
        }
    }

    private static RuntimeWorldConfig createWorldConfig(String dimensionType, String generatorType, boolean shouldTickTime) {
        RegistryKey<DimensionType> dimensionKey = parseDimensionType(dimensionType);
        ChunkGenerator chunkGenerator = createChunkGenerator(server, generatorType);

        return new RuntimeWorldConfig()
                .setDimensionType(dimensionKey)
                .setGenerator(chunkGenerator)
                .setGameRule(KEEP_INVENTORY, true)
                .setGameRule(DO_MOB_SPAWNING, false)
                .setGameRule(DO_DAYLIGHT_CYCLE, shouldTickTime)
                .setGameRule(DO_WEATHER_CYCLE, shouldTickTime)
                .setShouldTickTime(shouldTickTime);
    }

    public RuntimeWorldHandle getWorldHandle() {
        return worldHandle;
    }
    private static RegistryKey<DimensionType> parseDimensionType(String dimensionType) {
        return switch (dimensionType.toLowerCase()) {
            case "overworld" -> DimensionTypes.OVERWORLD;
            case "nether" -> DimensionTypes.THE_NETHER;
            case "end" -> DimensionTypes.THE_END;
            default -> {
                LOGGER.warn("未知的维度类型: {}, 使用默认的 OVERWORLD", dimensionType);
                yield DimensionTypes.OVERWORLD;
            }
        };
    }
    private static ChunkGenerator createChunkGenerator(MinecraftServer server, String generatorType) {
        var biomeRegistry = server.getRegistryManager().getOrThrow(RegistryKeys.BIOME);
        var plainsBiome = biomeRegistry.getOrThrow(BiomeKeys.PLAINS);

        return switch (generatorType.toLowerCase()) {
            case "void" -> new VoidChunkGenerator(biomeRegistry.getEntry(0).get());

            case "flat" -> {
                FlatChunkGeneratorConfig flat = new FlatChunkGeneratorConfig(Optional.empty(), plainsBiome, List.of());

                FlatChunkGeneratorLayer[] layers = {
                        new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK),
                        new FlatChunkGeneratorLayer(5, Blocks.DIRT),
                        new FlatChunkGeneratorLayer(2, Blocks.BEDROCK)
                };

                for (int i = layers.length - 1; i >= 0; --i) {
                    flat.getLayers().add(layers[i]);
                }

                flat.updateLayerBlocks();

                yield new FlatChunkGenerator(flat);
            }
            case "default", "normal" ->
                    server.getOverworld().getChunkManager().getChunkGenerator();

            default -> {
                LOGGER.warn("未知的生成器类型: {}, 使用默认生成器", generatorType);
                yield server.getOverworld().getChunkManager().getChunkGenerator();
            }
        };
    }
    public static void loadWorld(Identifier worldId) {
        LyXithConfigNode worldNode = configNode.getNode("worldConfigs."+worldId.toString()).get();
        String dimensionType = worldNode.getList().get().get(0).toString();
        String generatorType = worldNode.getList().get().get(1).toString();
        boolean shouldTickTime = Boolean.parseBoolean(worldNode.getList().get().get(2).toString());
        RuntimeWorldConfig config = createWorldConfig(dimensionType, generatorType, shouldTickTime);
        Fantasy fantasy = Fantasy.get(server);
        fantasy.getOrOpenPersistentWorld(worldId, config);
    }
    public static void unloadWorld(Identifier worldId) {
        LyXithConfigNode worldNode = configNode.getNode("worldConfigs."+worldId.toString()).get();
        String dimensionType = worldNode.getList().get().get(0).toString();
        String generatorType = worldNode.getList().get().get(1).toString();
        boolean shouldTickTime = Boolean.parseBoolean(worldNode.getList().get().get(2).toString());
        RuntimeWorldConfig config = createWorldConfig(dimensionType, generatorType, shouldTickTime);
        Fantasy fantasy = Fantasy.get(server);
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(worldId, config);
        worldHandle.unload();
    }
    public static void delWorld(Identifier worldId) {
        LyXithConfigNode worldNode = configNode.getNode("worldConfigs."+worldId.toString()).get();
        String dimensionType = worldNode.getList().get().get(0).toString();
        String generatorType = worldNode.getList().get().get(1).toString();
        boolean shouldTickTime = Boolean.parseBoolean(worldNode.getList().get().get(2).toString());
        RuntimeWorldConfig config = createWorldConfig(dimensionType, generatorType, shouldTickTime);
        Fantasy fantasy = Fantasy.get(server);
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(worldId, config);
        worldHandle.delete();
    }
    public static ServerWorld getWorld(Identifier worldId) {
        LyXithConfigNode worldNode = configNode.getNode("worldConfigs."+worldId.toString()).get();
        String dimensionType = worldNode.getList().get().get(0).toString();
        String generatorType = worldNode.getList().get().get(1).toString();
        boolean shouldTickTime = Boolean.parseBoolean(worldNode.getList().get().get(2).toString());
        RuntimeWorldConfig config = createWorldConfig(dimensionType, generatorType, shouldTickTime);
        Fantasy fantasy = Fantasy.get(server);
        return fantasy.getOrOpenPersistentWorld(worldId, config).asWorld();
    }
}
