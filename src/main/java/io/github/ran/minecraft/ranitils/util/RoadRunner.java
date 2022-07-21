package io.github.ran.minecraft.ranitils.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class RoadRunner {
    /* Returns true if the player cannot mine the block at the given position
     Which is usually the blocks underneath the player
     Returns false if the player can mine the block at the given position or the player is pressing shift */
    public static boolean cannotMine(BlockPos blockPos) {
        Level level = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;
        if (player != null && level != null) {
            if (player.isShiftKeyDown()) return false;
            return blockPos.getY() < player.getBlockY();
        }
        return false;
    }
}
