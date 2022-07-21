package io.github.ran.minecraft.ranitils.util;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.ran.minecraft.ranitils.config.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VerticalTP {

    public static final KeyMapping key = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.ranitils.verticaltp",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            "category.ranitils"
    ));

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (key.isDown()) {
                ModConfig.getInstance().verticalTP = !ModConfig.getInstance().verticalTP;
                ModConfig.getConfigHolder().save();
                if (mc.player != null) mc.player.sendSystemMessage(Component.literal("\u00a77[Ranitils] Vertical TP: " + ModConfig.getInstance().verticalTP));
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(VerticalTP::tryTeleport);
    }

    private static void tryTeleport(Minecraft minecraft) {
        if (!ModConfig.getInstance().verticalTP) return;

        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null) return;
        if (!player.isUsingItem() && minecraft.options.keyUse.isDown()) {
            HitResult result = player.pick(3.5, 1f / 20f, false);

            if (result.getType() == HitResult.Type.BLOCK && result instanceof BlockHitResult blockHitResult) {
                BlockPos blockPos = blockHitResult.getBlockPos();

                BlockState blockState = level.getBlockState(blockPos);

                if (Math.abs(player.getBlockX() - blockPos.getX()) > 2) return;
                if (Math.abs(player.getBlockZ() - blockPos.getZ()) > 2) return;
                if (Math.abs(player.getBlockY() - blockPos.getY()) < 2) return;

                if (blockState.use(level, player, InteractionHand.MAIN_HAND, blockHitResult) != InteractionResult.PASS) {
                    return;
                }

                Direction direction = blockHitResult.getDirection();

                VoxelShape collisionShape = blockState.getCollisionShape(level, blockPos);
                if (collisionShape.isEmpty()) collisionShape = blockState.getShape(level, blockPos);

                double top = collisionShape.isEmpty() ? 1 : collisionShape.max(Direction.Axis.Y);

                player.setPos(blockPos.getX() + 0.5 + direction.getStepX(), blockPos.getY() + top, blockPos.getZ() + 0.5 + direction.getStepZ());
            }
        }
    }
}
