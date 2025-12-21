package org.lyxith.lyxithworld.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.lyxith.lyxithworld.LyxithWorld.*;

public class WorldSuggestionProvider {
    public static CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> worlds = configNode.getNode("worlds").get().getList().orElse(new ArrayList<>());
        for (String worldId : worlds) {
            builder.suggest(Identifier.of(worldId).getPath());
        }
        return builder.buildFuture();
    }
}
