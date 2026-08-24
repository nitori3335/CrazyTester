package nfactory.crazytester.test;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import nfactory.crazytester.result.ResultExport;
import nfactory.crazytester.result.TreasureResult;

import java.util.List;

public class TreasureTest {

    private static boolean running = false;
    private static long targetCount;
    private static long completedCount;
    private static CommandSourceStack source;
    private static LootTable lootTable;
    private static ServerLevel level;
    static final int BATCH_SIZE = 65536;
    private static TreasureResult result;

    public static int start(CommandContext<CommandSourceStack> context) {

        int count = IntegerArgumentType.getInteger(context, "count");

        ResourceLocation lootTableId =
                ResourceLocationArgument.getId(
                        context,
                        "loot_table"
                );

        if (running) {
            context.getSource().sendFailure(
                    Component.literal("CrazyTest is already running.")
            );
            return 0;
        }

        level = context.getSource().getLevel();

        lootTable = context.getSource()
                .getServer()
                .getLootData()
                .getLootTable(lootTableId);

        targetCount = count;
        completedCount = 0;
        source = context.getSource();
        running = true;
        result = new TreasureResult();

        source.sendSuccess(
                () -> Component.literal(
                        "CrazyTest started: "
                                + lootTableId
                                + " / "
                                + count
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
                .withOptionalParameter(
                        LootContextParams.THIS_ENTITY,
                        source.getEntity()
                )
                .withParameter(
                        LootContextParams.ORIGIN,
                        source.getPosition()
                )
                .create(LootContextParamSets.CHEST);

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

        String output =
                ResultExport.format(result, completedCount);

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
    }
}