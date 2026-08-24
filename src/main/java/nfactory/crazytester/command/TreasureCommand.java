package nfactory.crazytester.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataType;
import nfactory.crazytester.test.TreasureTest;

import java.util.concurrent.CompletableFuture;

public class TreasureCommand {

    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        dispatcher.register(
                Commands.literal("crazytester")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.literal("checkTreasure")
                                        .then(
                                                Commands.argument(
                                                                "loot_table",
                                                                ResourceLocationArgument.id()
                                                        )
                                                        .suggests(TreasureCommand::suggestLootTables)
                                                        .then(
                                                                Commands.argument(
                                                                                "count",
                                                                                IntegerArgumentType.integer(1)
                                                                        )
                                                                        .executes(TreasureTest::start)
                                                        )
                                        )
                        )
        );
    }

    private static CompletableFuture<Suggestions> suggestLootTables(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        context.getSource()
                .getServer()
                .getLootData()
                .getKeys(LootDataType.TABLE)
                .stream()
                .filter(id -> id.getPath().startsWith("chests/"))
                .map(ResourceLocation::toString)
                .forEach(builder::suggest);

        return builder.buildFuture();
    }
}
