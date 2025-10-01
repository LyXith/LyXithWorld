package org.lyxith.lyxithworld.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;

import java.util.concurrent.CompletableFuture;

public class GameRuleSuggestionProvider {
    // 将此方法改为静态方法
    public static CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        ServerWorld world = context.getSource().getWorld();
        GameRules gameRules = world.getGameRules();
        gameRules.accept(new GameRules.Visitor() {
            @Override
            public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                builder.suggest(key.getName());
            }
        });
        return builder.buildFuture();
    }
}