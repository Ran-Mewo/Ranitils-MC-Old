package io.github.ran.minecraft.ranitils.mixins.autoSwitchElytra;

import com.mojang.authlib.GameProfile;
import io.github.ran.minecraft.ranitils.config.ModConfig;
import io.github.ran.minecraft.ranitils.util.autoSwitchElytra.AutoSwitchElytra;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Most of the code here is from OMMC (https://github.com/plusls/oh-my-minecraft-client/blob/multi/src/main/java/com/plusls/ommc/mixin/feature/autoSwitchElytra/MixinClientPlayerEntity.java)
// Which is by plusls and is licensed under the GNU Lesser General Public License v3.0
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
    @Shadow
    @Final
    protected Minecraft minecraft;

    boolean prevFallFlying = false;

    public LocalPlayerMixin(ClientLevel world, GameProfile profile) {
        super(world, profile, null);
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;", ordinal = 0))
    private void autoSwitchElytra(CallbackInfo ci) {
        if (!ModConfig.getInstance().autoSwitchElytra) {
            return;
        }
        ItemStack chestItemStack = this.getItemBySlot(EquipmentSlot.CHEST);
        if (chestItemStack.is(Items.ELYTRA) || !AutoSwitchElytra.myCheckFallFlying(this)) {
            return;
        }
        AutoSwitchElytra.autoSwitch(AutoSwitchElytra.CHEST_SLOT_IDX, this.minecraft, (LocalPlayer) (Object) this, itemStack -> itemStack.is(Items.ELYTRA));
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "aiStep", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/player/LocalPlayer;isFallFlying()Z", ordinal = 0))
    private void autoSwitchChest(CallbackInfo ci) {
        if (!ModConfig.getInstance().autoSwitchElytra) {
            return;
        }
        ItemStack chestItemStack = this.getItemBySlot(EquipmentSlot.CHEST);
        if (!chestItemStack.is(Items.ELYTRA) || !prevFallFlying || this.isFallFlying()) {
            prevFallFlying = this.isFallFlying();
            return;
        }
        prevFallFlying = this.isFallFlying();
        AutoSwitchElytra.autoSwitch(AutoSwitchElytra.CHEST_SLOT_IDX, this.minecraft, (LocalPlayer) (Object) this, itemStack -> Registry.ITEM.getKey(itemStack.getItem()).toString().contains("_chestplate"));
    }
}
