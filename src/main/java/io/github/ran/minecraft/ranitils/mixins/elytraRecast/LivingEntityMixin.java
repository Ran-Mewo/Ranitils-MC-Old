package io.github.ran.minecraft.ranitils.mixins.elytraRecast;

import io.github.ran.minecraft.ranitils.config.ModConfig;
import io.github.ran.minecraft.ranitils.util.elytraRecast.ElytraRecastHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Most of the code is from elytra-recast (https://github.com/InLieuOfLuna/elytra-recast) (https://modrinth.com/mod/elytra-recast)
// Which is by Luna & is licensed under the MIT license
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique
    private boolean prevElytra = false;

    @Inject(method = "isFallFlying", at = @At(value = "TAIL"), cancellable = true)
    private void recastIfNeeded(CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.getInstance().elytraRecast) {
            boolean elytra = cir.getReturnValue();
            if (prevElytra && !elytra) {
                cir.setReturnValue(ElytraRecastHelper.recastElytra(Minecraft.getInstance().player));
            }
            prevElytra = elytra;
        }
    }
}
