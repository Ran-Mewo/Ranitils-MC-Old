package io.github.ran.minecraft.ranitils.mixins.roadrunner;

import io.github.ran.minecraft.ranitils.config.ModConfig;
import io.github.ran.minecraft.ranitils.features.roadRunner.RoadRunner;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class FlatDigMixin {
    @Inject(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)V", shift = At.Shift.BEFORE), cancellable = true)
    private void onDestroyBlock(BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.getInstance().roadRunner) {
            if (RoadRunner.cannotMine(blockPos)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void startDestroyBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.getInstance().roadRunner) {
            if (RoadRunner.cannotMine(blockPos)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void continueDestroyBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.getInstance().roadRunner) {
            if (RoadRunner.cannotMine(blockPos)) {
                cir.setReturnValue(false);
            }
        }
    }
}
