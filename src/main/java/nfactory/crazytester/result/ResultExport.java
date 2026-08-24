package nfactory.crazytester.result;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import nfactory.crazytester.CrazyTester;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ResultExport {

    public static String format(
            TreasureResult result,
            long attempts
    ) {
        StringBuilder builder = new StringBuilder();

        builder.append("CrazyTester Result\n");
        builder.append("==================\n\n");

        builder.append("Attempts: ")
                .append(attempts)
                .append("\n\n");

        builder.append("Items\n");
        builder.append("-----\n");

        result.getItemCounts().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(
                        Comparator.comparing(ResourceLocation::toString)
                ))
                .forEach(entry -> builder
                        .append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue())
                        .append("\n"));

        builder.append("\nEnchantments\n");
        builder.append("------------\n");

        result.getEnchantmentCounts().keySet().stream()
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .forEach(enchantment -> {
                    long count = result.getEnchantmentCounts()
                            .get(enchantment);

                    builder.append(enchantment)
                            .append(": ")
                            .append(count)
                            .append("\n");

                    Map<Integer, Long> levels =
                            result.getEnchantmentLevelCounts()
                                    .get(enchantment);

                    if (levels != null) {
                        levels.entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .forEach(level -> builder
                                        .append("  level ")
                                        .append(level.getKey())
                                        .append(": ")
                                        .append(level.getValue())
                                        .append("\n"));
                    }

                    builder.append("\n");
                });

        Set<ResourceLocation> notFound =
                getNotFoundEnchantments(result);

        if (!notFound.isEmpty()) {
            builder.append("Not Found Enchantments\n");
            builder.append("----------------------\n");

            notFound.stream()
                    .sorted(Comparator.comparing(ResourceLocation::toString))
                    .forEach(id -> builder
                            .append(id)
                            .append("\n"));
        }

        return builder.toString();
    }

    public static void write(String output) {
        Path path = Path.of("crazytester-results.txt");

        try {
            Files.writeString(
                    path,
                    output,
                    StandardCharsets.UTF_8
            );

            CrazyTester.LOGGER.info(
                    "Result written to {}",
                    path.toAbsolutePath()
            );
        } catch (IOException e) {
            CrazyTester.LOGGER.error(
                    "Failed to write result",
                    e
            );
        }
    }

    private static Set<ResourceLocation> getNotFoundEnchantments(
            TreasureResult result
    ) {
        Set<ResourceLocation> allEnchantments =
                new HashSet<>(ForgeRegistries.ENCHANTMENTS.getKeys());

        allEnchantments.removeAll(
                result.getEnchantmentCounts().keySet()
        );

        return allEnchantments;
    }
}
