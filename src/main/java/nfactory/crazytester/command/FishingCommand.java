package nfactory.crazytester.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import nfactory.crazytester.test.FishingTest;

import java.util.concurrent.CompletableFuture;

public class FishingCommand {

    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        dispatcher.register(
                Commands.literal("crazytester")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.literal("checkFishing")
                                        .then(
                                                Commands.argument(
                                                                "count",
                                                                IntegerArgumentType.integer(1)
                                                        )
                                                        .then(
                                                                Commands.argument(
                                                                                "open_water",
                                                                                BoolArgumentType.bool()
                                                                        )
                                                                        .then(
                                                                                Commands.argument(
                                                                                                "biome",
                                                                                                ResourceLocationArgument.id()
                                                                                        )
                                                                                        .suggests(FishingCommand::suggestBiomes)
                                                                                        .executes(FishingTest::start)
                                                                                        .then(createEnchantmentArgument())
                                                                        )
                                                        )
                                        )
                        )
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> createEnchantmentArgument() {
        return Commands.literal("enchant")
                .then(
                        Commands.argument(
                                        "enchantment",
                                        ResourceLocationArgument.id()
                                )
                                .suggests(FishingCommand::suggestEnchantments)
                                .then(
                                        Commands.argument(
                                                        "level",
                                                        IntegerArgumentType.integer(1)
                                                )
                                                .suggests(FishingCommand::suggestEnchantmentLevels)
                                                .executes(FishingTest::start)
                                )
                );
    }

    private static CompletableFuture<Suggestions> suggestBiomes(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        context.getSource()
                .getServer()
                .registryAccess()
                .registryOrThrow(Registries.BIOME)
                .keySet()
                .stream()
                .map(ResourceLocation::toString)
                .forEach(builder::suggest);

        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestEnchantments(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        ItemStack fishingRod = new ItemStack(Items.FISHING_ROD);

        context.getSource()
                .getServer()
                .registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .entrySet()
                .stream()
                .filter(entry -> {
                    Enchantment enchantment = entry.getValue();

                    return enchantment.canEnchant(fishingRod);
                })
                .map(entry -> entry.getKey().location().toString())
                .forEach(builder::suggest);

        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestEnchantmentLevels(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        ResourceLocation id = ResourceLocationArgument.getId(
                context,
                "enchantment"
        );

        Registry<Enchantment> registry = context.getSource()
                .getServer()
                .registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT);

        Enchantment enchantment = registry.get(
                ResourceKey.create(Registries.ENCHANTMENT, id)
        );

        if (enchantment == null) {
            return builder.buildFuture();
        }

        for (int i = 1; i <= enchantment.getMaxLevel(); i++) {
            builder.suggest(i);
        }

        return builder.buildFuture();
    }
}