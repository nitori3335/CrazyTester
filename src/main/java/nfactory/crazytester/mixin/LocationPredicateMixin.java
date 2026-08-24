package nfactory.crazytester.mixin;

import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import nfactory.crazytester.test.FishingTest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocationPredicate.class)
public abstract class LocationPredicateMixin {

    @Redirect(
            method = "matches",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/resources/ResourceKey;)Z"
            )
    )
    private boolean crazyTester$overrideBiome(
            Holder<Biome> holder,
            ResourceKey<Biome> biome
    ) {
        ResourceKey<Biome> testBiome =
                FishingTest.getTestBiome();

        if (testBiome != null) {
            return testBiome.equals(biome);
        }

        return holder.is(biome);
    }
}