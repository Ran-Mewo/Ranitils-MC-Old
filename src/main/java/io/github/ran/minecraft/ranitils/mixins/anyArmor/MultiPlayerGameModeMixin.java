package io.github.ran.minecraft.ranitils.mixins.anyArmor;

import io.github.ran.minecraft.ranitils.util.AnyArmor;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


// FIXME: Fix this bad code cause I didn't know what I was doing and it was more of a test to see if this is possible.
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
	@Shadow public abstract void handleInventoryMouseClick(int i, int j, int k, ClickType clickType, Player player);

	@Unique
	private int prevPickSlot = -1;

	@Inject(method = "handleInventoryMouseClick", at = @At("HEAD"), cancellable = true)
	private void onClickSlot(int syncId, int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
		// If player clicked on a slot in their inventory
		if (clickType == ClickType.PICKUP && syncId == 0) {
			// If player clicked on an armor slot else set the clicked slot
			if (slotId >= 5 && slotId <= 8) {
				// Get & set the slot clicked before the armor slot
				if (prevPickSlot > -1) {
					if (prevPickSlot >= 5 && prevPickSlot <= 8) {
						// If the previously clicked slot is an armor slot, put the armor back in the slot
						putBack(syncId, slotId, button, clickType, player);
					} else {
						// Magic!
						this.handleInventoryMouseClick(syncId, prevPickSlot, button, clickType, player);
						AnyArmor.putArmor_MC(prevPickSlot, slotId);
					}
					// Reset the previously clicked slot & return
					prevPickSlot = -1;
					ci.cancel();
				} else {
					prevPickSlot = slotId;
				}
			} else {
				prevPickSlot = slotId;
			}
		}
	}

	@Unique
	private void putBack(int syncId, int slotId, int button, ClickType clickType, Player player) {
		for (Slot slot : player.containerMenu.slots) {
			if (!slot.hasItem() && slot.getContainerSlot() > 8) {
				this.handleInventoryMouseClick(syncId, slot.getContainerSlot(), button, clickType, player);
				AnyArmor.putArmor_MC(slot.getContainerSlot(), slotId);
				return;
			}
		}
	}
}
