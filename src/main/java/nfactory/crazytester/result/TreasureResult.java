package nfactory.crazytester.result;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class TreasureResult {

    private final Map<ResourceLocation, Long> itemCounts = new HashMap<>();
    private final Map<ResourceLocation, Long> enchantmentCounts = new HashMap<>();
    private final Map<ResourceLocation, Map<Integer, Long>> enchantmentLevelCounts = new HashMap<>();
    private final Map<ResourceLocation, Map<ResourceLocation, Long>> enchantmentItemCounts = new HashMap<>();

    public void add(ItemStack item) {

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item.getItem());

        itemCounts.merge(
                itemId,
                (long) item.getCount(),
                Long::sum
        );

        for (Map.Entry<Enchantment, Integer> entry
                : EnchantmentHelper.getEnchantments(item).entrySet()) {

            ResourceLocation enchantmentId =
                    ForgeRegistries.ENCHANTMENTS.getKey(entry.getKey());

            int level = entry.getValue();

            enchantmentCounts.merge(
                    enchantmentId,
                    1L,
                    Long::sum
            );

            enchantmentLevelCounts
                    .computeIfAbsent(enchantmentId, key -> new HashMap<>())
                    .merge(level, 1L, Long::sum);

            enchantmentItemCounts
                    .computeIfAbsent(enchantmentId, key -> new HashMap<>())
                    .merge(itemId, 1L, Long::sum);
        }
    }

    public Map<ResourceLocation, Long> getItemCounts() {
        return itemCounts;
    }

    public Map<ResourceLocation, Long> getEnchantmentCounts() {
        return enchantmentCounts;
    }

    public Map<ResourceLocation, Map<Integer, Long>> getEnchantmentLevelCounts() {
        return enchantmentLevelCounts;
    }
}
