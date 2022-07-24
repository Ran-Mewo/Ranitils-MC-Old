package io.github.ran.minecraft.ranitils.mixins.waypoints;

import io.github.ran.minecraft.ranitils.util.waypoints.Waypoint;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Code is from OMMC (https://github.com/plusls/oh-my-minecraft-client)
// Which is by plusls and is licensed under the GNU Lesser General Public License v3.0
@Mixin(value = ChatComponent.class, priority = 998)
public abstract class ChatComponentMixin {
    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;I)V", at = @At(value = "HEAD"))
    public void modifyMessage(Component message, int messageId, CallbackInfo ci) {
        Waypoint.parseWaypointText(message);
    }
}
