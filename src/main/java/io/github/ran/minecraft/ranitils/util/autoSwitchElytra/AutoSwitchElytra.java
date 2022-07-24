package io.github.ran.minecraft.ranitils.util.autoSwitchElytra;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.function.Predicate;

// Most of the code here is from OMMC (https://github.com/plusls/oh-my-minecraft-client/blob/multi/src/main/java/com/plusls/ommc/feature/autoSwitchElytra/AutoSwitchElytraUtil.java)
// Which is by plusls and is licensed under the GNU Lesser General Public License v3.0
public class AutoSwitchElytra {
    public static final int CHEST_SLOT_IDX = 6;

    public static boolean myCheckFallFlying(Player player) {
        return !player.isOnGround() && !player.isFallFlying() && !player.isInWater() && !player.hasEffect(MobEffects.LEVITATION);
    }

    public static void autoSwitch(int sourceSlot, Minecraft client, LocalPlayer clientPlayerEntity, Predicate<ItemStack> check) {
        if (client.gameMode != null) {
            if (clientPlayerEntity.containerMenu != clientPlayerEntity.inventoryMenu) {
                clientPlayerEntity.closeContainer();
            }
            AbstractContainerMenu screenHandler = clientPlayerEntity.containerMenu;
            ArrayList<ItemStack> itemStacks = new ArrayList<>();
            for (int i = 0; i < screenHandler.slots.size(); ++i) {
                itemStacks.add(screenHandler.slots.get(i).getItem().copy());
            }

            int idxToSwitch = -1;
            for (int i = 0; i < itemStacks.size(); ++i) {
                if (check.test(itemStacks.get(i))) {
                    idxToSwitch = i;
                    break;
                }
            }
            if (idxToSwitch != -1) {
                client.gameMode.handleInventoryMouseClick(screenHandler.containerId, idxToSwitch, 0, ClickType.PICKUP, clientPlayerEntity);
                client.gameMode.handleInventoryMouseClick(screenHandler.containerId, sourceSlot, 0, ClickType.PICKUP, clientPlayerEntity);
                client.gameMode.handleInventoryMouseClick(screenHandler.containerId, idxToSwitch, 0, ClickType.PICKUP, clientPlayerEntity);
            }
        }
    }
}
