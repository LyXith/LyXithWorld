package org.lyxith.lyxithworld;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

import java.util.List;
import java.util.Optional;

import static net.minecraft.world.GameRules.*;
import static org.lyxith.lyxithworld.LyxithWorld.LOGGER;
import static org.lyxith.lyxithworld.LyxithWorld.server;

public class CreateWorld {
    public CreateWorld(String DimensionType, String Generator, boolean ShouldTickTime, String id) {
        RegistryKey<DimensionType> dimensionKey =  parseDimensionType(DimensionType);
        ChunkGenerator chunkGenerator = createChunkGenerator(server, Generator);
        RuntimeWorldConfig config = new RuntimeWorldConfig()
                .setDimensionType(dimensionKey)
                .setGenerator(chunkGenerator)
                .setGameRule(KEEP_INVENTORY,true)
                .setGameRule(DO_MOB_SPAWNING, false)
                .setShouldTickTime(ShouldTickTime);
        Fantasy.get(server).getOrOpenPersistentWorld(Identifier.of(id),config);
    }
    private RegistryKey<DimensionType> parseDimensionType(String dimensionType) {
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
    private ChunkGenerator createChunkGenerator(MinecraftServer server, String generatorType) {
        var biomeRegistry = server.getRegistryManager().getOrThrow(RegistryKeys.BIOME);
        var plainsBiome = biomeRegistry.getOrThrow(BiomeKeys.PLAINS);

        return switch (generatorType.toLowerCase()) {
            case "void" -> new VoidChunkGenerator(biomeRegistry.getEntry(0).get());

            case "flat" -> {
                FlatChunkGeneratorConfig flat = new FlatChunkGeneratorConfig(Optional.empty(), plainsBiome, List.of());
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
}
