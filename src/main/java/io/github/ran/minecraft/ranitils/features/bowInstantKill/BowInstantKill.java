
package io.github.ran.minecraft.ranitils.features.bowInstantKill;

import io.github.ran.minecraft.ranitils.config.ModConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

public class BowInstantKill {
    // Unknown Origin
    public static void addVelocity(LocalPlayer player) {
        player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
        for (int i = 0; i < ModConfig.getInstance().bowInstantKillCharges; i++) {
            player.connection.send(new ServerboundMovePlayerPacket.Pos(player.getX(), player.getY() - 0.000000001, player.getZ(), true));
            player.connection.send(new ServerboundMovePlayerPacket.Pos(player.getX(), player.getY() + 0.000000001, player.getZ(), false));
        }
    }
}
