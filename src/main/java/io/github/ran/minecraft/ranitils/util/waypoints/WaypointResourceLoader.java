package io.github.ran.minecraft.ranitils.util.waypoints;

import io.github.ran.minecraft.ranitils.config.ModConfig;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class WaypointResourceLoader {
    public static void register() {
        Cute.register();
        NoCute.register();
    }

    public static TextureAtlasSprite getTargetIdSprite() {
        if (ModConfig.getInstance().waypointSquareIcon) {
            return NoCute.targetIdSprite; // Why no cute waypoint? :(
        } else {
            return Cute.targetIdSprite; // Vewwy cute >~<
        }
    }

    // Code from OMMC (https://github.com/plusls/oh-my-minecraft-client)
    // Which is by plusls and is licensed under the GNU Lesser General Public License v3.0
    public static class NoCute implements SimpleSynchronousResourceReloadListener {
        private static final ResourceLocation listenerId = new ResourceLocation("ranitils", "waypoint_reload_listener_nocute");
        private static final ResourceLocation targetId = new ResourceLocation("ranitils", "images/waypoint_nocute");
        public static TextureAtlasSprite targetIdSprite;

        public static void register() {
            ClientSpriteRegistryCallback.event(InventoryMenu.BLOCK_ATLAS).register(
                    (atlasTexture, registry) -> registry.register(targetId)
            );
            ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new NoCute());
        }

        @Override
        public ResourceLocation getFabricId() {
            return listenerId;
        }

        @Override
        public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
            final Function<ResourceLocation, TextureAtlasSprite> atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
            targetIdSprite = atlas.apply(targetId);
        }
    }

    // Code from OMMC (https://github.com/plusls/oh-my-minecraft-client)
    // Which is by plusls and is licensed under the GNU Lesser General Public License v3.0
    public static class Cute implements SimpleSynchronousResourceReloadListener {
        private static final ResourceLocation listenerId = new ResourceLocation("ranitils", "waypoint_reload_listener");
        private static final ResourceLocation targetId = new ResourceLocation("ranitils", "images/waypoint");
        public static TextureAtlasSprite targetIdSprite;

        public static void register() {
            ClientSpriteRegistryCallback.event(InventoryMenu.BLOCK_ATLAS).register(
                    (atlasTexture, registry) -> registry.register(targetId)
            );
            ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new Cute());
        }

        @Override
        public ResourceLocation getFabricId() {
            return listenerId;
        }

        @Override
        public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
            final Function<ResourceLocation, TextureAtlasSprite> atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
            targetIdSprite = atlas.apply(targetId);
        }
    }
}
