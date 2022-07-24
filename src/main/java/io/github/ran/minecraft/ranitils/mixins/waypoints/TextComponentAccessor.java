package io.github.ran.minecraft.ranitils.mixins.waypoints;

import net.minecraft.network.chat.contents.LiteralContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

// Code is from OMMC (https://github.com/plusls/oh-my-minecraft-client)
// Which is by plusls and is licensed under the GNU Lesser General Public License v3.0
@Mixin(LiteralContents.class)
public interface TextComponentAccessor {
    @Accessor
    String getText();

    @Mutable
    @Accessor
    void setText(String text);
}
