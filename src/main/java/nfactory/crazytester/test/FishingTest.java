package nfactory.crazytester.test;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import nfactory.crazytester.mixin.FishingHookAccessor;
import nfactory.crazytester.result.ResultExport;
import nfactory.crazytester.result.TreasureResult;

import java.util.List;

import static nfactory.crazytester.test.TreasureTest.BATCH_SIZE;

public class FishingTest {

    private static boolean running = false;
    private static long targetCount;
    private static long completedCount;

    private static ServerLevel level;

    private static boolean openWater;
    private static ResourceKey<Biome> biome;

    private static LootTable lootTable;
    private static TreasureResult result;

    private static FishingHook fishingHook;
    private static ItemStack fishingRod;
    private static BlockPos fishingPos;

    private static CommandSourceStack source;

    public static int start(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {

        int count = IntegerArgumentType.getInteger(context, "count");

        boolean openWaterValue = BoolArgumentType.getBool(
                context,
                "open_water"
        );

        ResourceLocation biomeValue =
                ResourceLocationArgument.getId(
                        context,
                        "biome"
                );

        if (running) {
            context.getSource().sendFailure(
                    Component.literal("CrazyTest is already running.")
            );
            return 0;
        }

        source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        level = source.getLevel();

        openWater = openWaterValue;

        biome = ResourceKey.create(
                Registries.BIOME,
                biomeValue
        );

        fishingPos = player.blockPosition();

        fishingRod = new ItemStack(Items.FISHING_ROD);

        fishingHook = new FishingHook(
                player,
                level,
                0,
                0
        );

        ((FishingHookAccessor) fishingHook).setOpenWater(openWater);

        lootTable = source.getServer()
                .getLootData()
                .getLootTable(BuiltInLootTables.FISHING);

        targetCount = count;
        completedCount = 0;

        running = true;
        result = new TreasureResult();

        source.sendSuccess(
                () -> Component.literal(
                        "CrazyTest started: fishing / "
                                + count
                                + " / open_water="
                                + openWater
                                + " / biome="
                                + biome.location()
                ),
                true
        );

        return 1;
    }

    public static void tick() {

        if (!running) {
            return;
        }

        long remaining = targetCount - completedCount;
        int batch = (int) Math.min(BATCH_SIZE, remaining);

        LootParams params = new LootParams.Builder(level)
                .withParameter(
                        LootContextParams.ORIGIN,
                        Vec3.atCenterOf(fishingPos)
                )
                .withParameter(
                        LootContextParams.TOOL,
                        fishingRod
                )
                .withOptionalParameter(
                        LootContextParams.THIS_ENTITY,
                        fishingHook
                )
                .withLuck(0.0F)
                .create(LootContextParamSets.FISHING);

        RandomSource batchRandom = RandomSource.create();
        long firstSeed = batchRandom.nextLong();

        for (int i = 0; i < batch; i++) {

            long lootSeed = (i == 0)
                    ? firstSeed
                    : batchRandom.nextLong();

            List<ItemStack> items =
                    lootTable.getRandomItems(params, lootSeed);

            for (ItemStack item : items) {
                result.add(item);
            }
        }

        completedCount += batch;

        if (completedCount >= targetCount) {
            finish();
        }
    }

    private static void finish() {
        running = false;

        String output = ResultExport.format(
                result,
                completedCount
        );

        ResultExport.write(output);

        source.sendSuccess(
                () -> Component.literal(
                        "CrazyTest completed: " + completedCount
                ),
                true
        );

        source = null;
        lootTable = null;
        level = null;
        fishingHook = null;
        fishingRod = null;
        fishingPos = null;
    }

    public static ResourceKey<Biome> getTestBiome() {
        return running ? biome : null;
    }
}
