package io.github.ran.minecraft.ranitils.util.anyArmor;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.ClickType;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

@SuppressWarnings("ConstantConditions")
public class AnyArmor {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("wear").then(argument("armorSlot", integer(0, 3))
                        .executes(ctx -> execute(ctx.getSource(), getInteger(ctx, "armorSlot"))))));
    }

    public static int execute(FabricClientCommandSource ctx, int armorSlot) {
        putArmor(armorSlot);
        return Command.SINGLE_SUCCESS;
    }

    public static void putArmor(int armorSlot) {
        putArmor(36 + Minecraft.getInstance().player.getInventory().selected, armorSlot);
    }

    public static void putArmor(int slot, int armorSlot) {
        // 0 = helmet, 1 = chestplate, 2 = leggings, 3 = boots
        switch (armorSlot) {
            case 0 -> putHelmet(slot);
            case 1 -> putChestplate(slot);
            case 2 -> putLeggings(slot);
            case 3 -> putBoots(slot);
        }
    }

    public static void putArmor_MC(int slot, int armorSlot) {
        // 5 = helmet, 6 = chestplate, 7 = leggings, 8 = boots
        switch (armorSlot) {
            case 5 -> putHelmet(slot);
            case 6 -> putChestplate(slot);
            case 7 -> putLeggings(slot);
            case 8 -> putBoots(slot);
        }
    }

    // 39 = helmet, 38 = chestplate, 37 = leggings, 36 = boots

    private static void putHelmet(int slot) {
        LocalPlayer player = Minecraft.getInstance().player;
        Minecraft.getInstance().gameMode.handleInventoryMouseClick(player.containerMenu.containerId, slot, 39, ClickType.SWAP, player);
    }

    private static void putChestplate(int slot) {
        LocalPlayer player = Minecraft.getInstance().player;
        Minecraft.getInstance().gameMode.handleInventoryMouseClick(player.containerMenu.containerId, slot, 38, ClickType.SWAP, player);
    }

    private static void putLeggings(int slot) {
        LocalPlayer player = Minecraft.getInstance().player;
        Minecraft.getInstance().gameMode.handleInventoryMouseClick(player.containerMenu.containerId, slot, 37, ClickType.SWAP, player);
    }

    private static void putBoots(int slot) {
        LocalPlayer player = Minecraft.getInstance().player;
        Minecraft.getInstance().gameMode.handleInventoryMouseClick(player.containerMenu.containerId, slot, 36, ClickType.SWAP, player);
    }
}
