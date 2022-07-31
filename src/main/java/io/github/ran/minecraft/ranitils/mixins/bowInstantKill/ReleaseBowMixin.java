package io.github.ran.minecraft.ranitils.mixins.bowInstantKill;
import io.github.ran.minecraft.ranitils.config.ModConfig;
import io.github.ran.minecraft.ranitils.stuff.bowInstantKill.BowInstantKill;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class ReleaseBowMixin {
    @Inject(method = "releaseUsingItem", at = @At("HEAD"))
    public void releaseUsingItem(Player player, CallbackInfo ci) {
        if (ModConfig.getInstance().bowInstantKill && player instanceof LocalPlayer personWithBetterGamingChair && player.isHolding(Items.BOW)) {
            BowInstantKill.addVelocity(personWithBetterGamingChair);
        }
    }
}