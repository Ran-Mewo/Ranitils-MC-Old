package io.github.ran.minecraft.ranitils.util.waypoints;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.brigadier.Command;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.github.ran.minecraft.ranitils.config.ModConfig;
import io.github.ran.minecraft.ranitils.mixins.waypoints.TextComponentAccessor;
import io.github.ran.minecraft.ranitils.mixins.waypoints.TranslatableComponentAccessor;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;

// Some of the code is from OMMC (https://github.com/plusls/oh-my-minecraft-client)
// Which is by plusls and is licensed under the GNU Lesser General Public License v3.0
public class Waypoint {
    // Me when you need university level maths just for a simple waypoint

    private final BlockPos blockPos;
    public final ResourceKey<Level> actualDimension;

    public static Pattern pattern1 = Pattern.compile("\\[(\\w+\\s*:\\s*[-#]?[^\\[\\]]+)(,\\s*\\w+\\s*:\\s*[-#]?[^\\[\\]]+)+]", Pattern.CASE_INSENSITIVE);
    public static Pattern pattern2 = Pattern.compile("\\((\\w+\\s*:\\s*[-#]?[^\\[\\]]+)(,\\s*\\w+\\s*:\\s*[-#]?[^\\[\\]]+)+\\)", Pattern.CASE_INSENSITIVE);
    public static Pattern pattern3 = Pattern.compile("\\[(-?\\d+)(,\\s*-?\\d+)(,\\s*-?\\d+)]", Pattern.CASE_INSENSITIVE);
    public static Pattern pattern4 = Pattern.compile("\\((-?\\d+)(,\\s*-?\\d+)(,\\s*-?\\d+)\\)", Pattern.CASE_INSENSITIVE);

    public static ArrayList<Tuple<Integer, String>> getWaypointStrings(String message) {
        ArrayList<Tuple<Integer, String>> ret = new ArrayList<>();
        if ((message.contains("[") && message.contains("]")) || (message.contains("(") && message.contains(")"))) {
            getWaypointStringsByPattern(message, ret, pattern1);
            getWaypointStringsByPattern(message, ret, pattern2);
            getWaypointStringsByPattern(message, ret, pattern3);
            getWaypointStringsByPattern(message, ret, pattern4);
        }
        ret.sort(Comparator.comparingInt(Tuple::getA));
        return ret;
    }

    private static void getWaypointStringsByPattern(String message, ArrayList<Tuple<Integer, String>> ret, Pattern pattern) {
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String match = matcher.group();
            BlockPos pos = parseWaypoint(match.substring(1, match.length() - 1));
            if (pos == null) {
                continue;
            }
            ret.add(new Tuple<>(matcher.start(), match));
        }
    }

    private static BlockPos parseWaypoint(String details) {
        String[] pairs = details.split(",");
        Integer x = null;
        Integer z = null;
        int y = 64;
        try {
            for (int i = 0; i < pairs.length; ++i) {
                int splitIndex = pairs[i].indexOf(":");
                String key, value;
                if (splitIndex == -1 && pairs.length == 3) {
                    if (i == 0) {
                        key = "x";
                    } else if (i == 1) {
                        key = "y";
                    } else {
                        key = "z";
                    }
                    value = pairs[i];
                } else {
                    key = pairs[i].substring(0, splitIndex).toLowerCase().trim();
                    value = pairs[i].substring(splitIndex + 1).trim();
                }

                switch (key) {
                    case "x" -> x = Integer.parseInt(value.replace(" ", ""));
                    case "y" -> y = Integer.parseInt(value.replace(" ", ""));
                    case "z" -> z = Integer.parseInt(value.replace(" ", ""));
                }
            }

        } catch (NumberFormatException ignored) {
        }
        if (x == null || z == null) {
            return null;
        }
        return new BlockPos(x, y, z);
    }

    public static void parseWaypointText(Component chat) {
        if (chat.getSiblings().size() > 0) {
            for (Component text : chat.getSiblings()) {
                parseWaypointText(text);
            }
        }

        ComponentContents componentContents = chat.getContents();
        if (componentContents instanceof TranslatableContents) {
            Object[] args = ((TranslatableContents) componentContents).getArgs();

            boolean updateTranslatableText = false;
            for (int i = 0; i < args.length; ++i) {
                if (args[i] instanceof Component) {
                    parseWaypointText((Component) args[i]);
                } else if (args[i] instanceof String) {
                    Component text = Component.literal((String) args[i]);
                    if (updateWaypointsText(text)) {
                        args[i] = text;
                        updateTranslatableText = true;
                    }
                }
            }
            if (updateTranslatableText) {
                ((TranslatableComponentAccessor) componentContents).setDecomposedWith(null);
            }
        }
        updateWaypointsText(chat);
    }


    public static boolean updateWaypointsText(Component chat) {
        ComponentContents componentContents = chat.getContents();
        if (!(componentContents instanceof LiteralContents literalChatText)) {
            return false;
        }

        String message = ((TextComponentAccessor) (Object) literalChatText).getText();
        ArrayList<Tuple<Integer, String>> waypointPairs = getWaypointStrings(message);
        if (waypointPairs.size() > 0) {
            Style style = chat.getStyle();
            ClickEvent clickEvent = style.getClickEvent();
            TextColor color = style.getColor();

            if (color == null) {
                color = TextColor.fromLegacyFormat(ChatFormatting.GOLD);
            }

            ArrayList<Component> texts = new ArrayList<>();
            int prevIdx = 0;
            for (Tuple<Integer, String> waypointPair : waypointPairs) {
                String waypointString = waypointPair.getB();
                int waypointIdx = waypointPair.getA();
                Component prevText = Component.literal(message.substring(prevIdx, waypointIdx)).withStyle(style);
                texts.add(prevText);

                MutableComponent clickableWaypoint = Component.literal(waypointString);

                Style chatStyle = clickableWaypoint.getStyle();
                BlockPos pos = parseWaypoint(waypointString.substring(1, waypointString.length() - 1));
                Component hover = Component.literal("Click to add waypoint");
                if (clickEvent == null) {
                    clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            String.format("/addWaypoint %d %d %d", pos.getX(), pos.getY(), pos.getZ()));
                }
                chatStyle = chatStyle.withClickEvent(clickEvent).withColor(color).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
                clickableWaypoint.withStyle(chatStyle);
                texts.add(clickableWaypoint);
                prevIdx = waypointIdx + waypointString.length();
            }
            if (prevIdx < message.length() - 1) {
                Component lastText = Component.literal(message.substring(prevIdx)).withStyle(style);
                texts.add(lastText);
            }
            for (int i = 0; i < texts.size(); ++i) {
                chat.getSiblings().add(i, texts.get(i));
            }
            ((TextComponentAccessor) (Object) literalChatText).setText("");
            ((MutableComponent) chat).withStyle(Style.EMPTY);
            return true;
        }
        return false;
    }

    public static final KeyMapping removeKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.ranitils.removeWaypoint",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_MINUS,
            "category.ranitils"
    ));
    private static boolean removeKeyPressed = false;

    public static final KeyMapping sendKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.ranitils.sendWaypoint",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "category.ranitils"
    ));
    private static boolean sendKeyPressed = false;

    private static final List<Waypoint> waypoints = new ArrayList<>();

    public static void register() {
        WaypointResourceLoader.register();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (removeKey.isDown() && !removeKeyPressed) {
                removeKeyPressed = true;
                removeAll();
                if (mc.player != null)
                    mc.player.sendSystemMessage(Component.literal("[Ranitils] Removed all waypoints").withStyle(ChatFormatting.GRAY));
            } else if (removeKeyPressed && !removeKey.isDown()) {
                removeKeyPressed = false;
            }

            if (sendKey.isDown() && !sendKeyPressed) {
                sendKeyPressed = true;
                Entity cameraEntity = mc.cameraEntity;
                MultiPlayerGameMode gameMode = mc.gameMode;
                LocalPlayer localPlayer = mc.player;
                if (cameraEntity != null && gameMode != null && localPlayer != null) {
                    HitResult result = cameraEntity.pick(gameMode.getPickRange(), mc.getFrameTime(), true);
                    if (result.getType() == HitResult.Type.BLOCK && result instanceof BlockHitResult blockHitResult) {
                        BlockPos lookPos = blockHitResult.getBlockPos();
                        localPlayer.chat(String.format("[%d, %d, %d]", lookPos.getX(), lookPos.getY(), lookPos.getZ()));
                    }
                }
            } else if (sendKeyPressed && !sendKey.isDown()) {
                sendKeyPressed = false;
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("removeWaypoint").then(
                        ClientCommandManager.argument("x", integer()).then(
                                ClientCommandManager.argument("y", integer()).then(
                                        ClientCommandManager.argument("z", integer())
                                                .executes(context -> {
                                                    BlockPos pos = new BlockPos(getInteger(context, "x"), getInteger(context, "y"), getInteger(context, "z"));
                                                    removeWaypoint(pos);

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                        )
                )));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("addWaypoint").then(
                        ClientCommandManager.argument("x", integer()).then(
                                ClientCommandManager.argument("y", integer()).then(
                                        ClientCommandManager.argument("z", integer())
                                                .executes(context -> {
                                                    BlockPos pos = new BlockPos(getInteger(context, "x"), getInteger(context, "y"), getInteger(context, "z"));
                                                    ClientLevel level = Minecraft.getInstance().level;
                                                    if (level != null) new Waypoint(pos, level.dimension());

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                        )
                )));

        ClientPlayConnectionEvents.DISCONNECT.register((listener, minecraft) -> removeAll());
    }

    public Waypoint(BlockPos position, ResourceKey<Level> dimension) {
        blockPos = position;
        actualDimension = dimension;
        add(this);
    }

    public static void add(Waypoint wp) {
        if (waypoints.stream().filter(waypoint -> waypoint.getWaypointCoordinates(Minecraft.getInstance()).equals(wp.getWaypointCoordinates(Minecraft.getInstance())))
                .findFirst().isEmpty()) {
            waypoints.add(wp);
        }
    }

    public void remove() {
        waypoints.remove(this);
    }

    public static void removeWaypoint(BlockPos blockPos) {
        waypoints.stream().filter(waypoint ->
                Minecraft.getInstance().level.dimension().equals(waypoint.actualDimension) && waypoint.getWaypointCoordinates(Minecraft.getInstance()).equals(blockPos))
                .findFirst().ifPresent(Waypoint::remove);
    }

    public static void removeAll() {
        waypoints.clear();
    }

    private boolean isPointedAt(BlockPos pos, double distance, boolean shouldScaleX, Entity cameraEntity, float tickDelta) {
        if (!shouldScaleX) return true; // FIXME I don't know how to do check if it's being pointed at with my modified distance scaling

        // FIXME I don't know how to do check if it's being pointed at with my modified distance scaling
        Vec3 actualCameraPos = cameraEntity.getEyePosition(tickDelta);
        Vec3 cameraPos = new Vec3(actualCameraPos.x, 0, actualCameraPos.z);
        double degrees = 5.0 + Math.min((5.0 / distance), 5.0);
        double angle = degrees * 0.0174533;
        double size = Math.sin(angle) * distance;
        Vec3 actualCameraPosPlusDirection = cameraEntity.getViewVector(tickDelta);
        Vec3 cameraPosPlusDirection = new Vec3(actualCameraPosPlusDirection.x, 0, actualCameraPosPlusDirection.z);
        Vec3 cameraPosPlusDirectionTimesDistance = cameraPos.add(cameraPosPlusDirection.x() * distance, 0, cameraPosPlusDirection.z() * distance);
        AABB axisalignedbb = new AABB(pos.getX() + 0.5f - size, 0, pos.getZ() + 0.5f - size,
                pos.getX() + 0.5f + size, 0, pos.getZ() + 0.5f + size);
        Optional<Vec3> raycastResult = axisalignedbb.clip(cameraPos, cameraPosPlusDirectionTimesDistance);
        return axisalignedbb.contains(cameraPos) ? distance >= 1.0 : raycastResult.isPresent();
    }

    public BlockPos getWaypointCoordinates(Minecraft mc) {
        if (blockPos == null) return null;
        if (!ModConfig.getInstance().waypointCoordinateConvert || mc.level == null) return blockPos;

        int x = blockPos.getX();
        int z = blockPos.getZ();

        if (actualDimension.equals(Level.OVERWORLD) && mc.level.dimension().equals(Level.NETHER)) {
            x = x / 8;
            z = z / 8;
        } else if (actualDimension.equals(Level.NETHER) && mc.level.dimension().equals(Level.OVERWORLD)) {
            x = x * 8;
            z = z * 8;
        }

        return new BlockPos(x, blockPos.getY(), z);
    }

    private double getDistanceToEntity(Entity entity, BlockPos pos) {
        double dist_x = pos.getX() + 0.5 - entity.getX();
        double dist_y = pos.getY() + 0.5 - entity.getY();
        double dist_z = pos.getZ() + 0.5 - entity.getZ();
        return Math.sqrt(dist_x * dist_x + dist_y * dist_y + dist_z * dist_z);
    }

    private double getDistanceToEntityNoY(Entity entity, BlockPos pos) {
        double dist_x = pos.getX() + 0.5 - entity.getX();
        double dist_z = pos.getZ() + 0.5 - entity.getZ();
        return Math.sqrt(dist_x * dist_x + dist_z * dist_z);
    }

    public static void renderWaypoint(PoseStack poseStack, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        for (Waypoint waypoint : waypoints) {
            BlockPos blockPos = waypoint.getWaypointCoordinates(mc);
            if (blockPos != null) {
                Entity camera = mc.cameraEntity;
                if (camera != null) {
                    RenderSystem.enableBlend();
                    RenderSystem.disableDepthTest();
                    RenderSystem.depthMask(false);

                    double distance = waypoint.getDistanceToEntity(camera, blockPos);
                    double distanceNoY = waypoint.getDistanceToEntityNoY(camera, blockPos);

                    double maxDistance = mc.options.renderDistance().get();
                    boolean shouldScaleX = ((int) distanceNoY - ((int) maxDistance * 8)) > -60;

                    waypoint.render(mc, blockPos, poseStack, distance, shouldScaleX, distanceNoY, waypoint.isPointedAt(blockPos, distanceNoY, shouldScaleX, camera, partialTick), partialTick, camera);
                    RenderSystem.enableDepthTest();
                    RenderSystem.depthMask(true);
                    RenderSystem.disableBlend();
                }
            }
        }
    }

    public void render(Minecraft mc, BlockPos blockPos, PoseStack poseStack, double distance, boolean shouldScaleX, double distanceNoY, boolean isPointedAt, float partialTick, Entity camera) {
        if (mc.level == null) return;

        String name = String.format("x:%d, y:%d, z:%d (%dm)", blockPos.getX(), blockPos.getY(), blockPos.getZ(), (int) distance);

        double lerpX = blockPos.getX() - Mth.lerp(partialTick, camera.xo, camera.getX());
        double lerpY = blockPos.getY() - Mth.lerp(partialTick, camera.yo, camera.getY()) - 1.5;
        double lerpZ = blockPos.getZ() - Mth.lerp(partialTick, camera.zo, camera.getZ());

        double maxDistance = mc.options.renderDistance().get();
        double adjustedDistance = distance;
        if (distance > maxDistance) {
            lerpY = lerpY / distance * maxDistance;
            adjustedDistance = maxDistance;
        }
        if (shouldScaleX) {
            lerpX = lerpX / distanceNoY * maxDistance;
            lerpZ = lerpZ / distanceNoY * maxDistance;
            adjustedDistance = maxDistance;
        }
        float scale = (float) (adjustedDistance * 0.1f + 1.0f) * 0.0266f;

        poseStack.pushPose();
        poseStack.translate(lerpX, lerpY, lerpZ);

        if (!ModConfig.getInstance().noWaypointBeam) {
            float[] colour = { 0.0f, 0.404f, 1f };
            WaypointRenderer.renderBeam(poseStack, partialTick, 0.25f, mc.level.getGameTime(),
                    (int) (lerpY - 512), 1024, colour, 0.25f, 0.25f);
            RenderSystem.enableBlend();
        }

        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(new Vector3f(0.0F, 1.0F, 0.0F).rotationDegrees(-camera.getYRot()));
        poseStack.mulPose(new Vector3f(1.0F, 0.0F, 0.0F).rotationDegrees(mc.getEntityRenderDispatcher().camera.getXRot()));
        poseStack.scale(-scale, -scale, -scale);

        Matrix4f matrix4f = poseStack.last().pose();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder vertexBuffer = tessellator.getBuilder();

        float fade = distance < 5.0 ? 1.0f : (float) distance / 5.0f;
        fade = Math.min(fade, 1.0f);

        float xWidth = 10.0f;
        float yWidth = 10.0f;

        float iconR = 0.0f;
        float iconG = 0.404f;
        float iconB = 1f;

        TextureAtlasSprite icon = WaypointResourceLoader.getTargetIdSprite();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

        RenderSystem.enableTexture();
        vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        vertexBuffer.vertex(matrix4f, -xWidth, -yWidth, 0.0f).uv(icon.getU0(), icon.getV0()).color(iconR, iconG, iconB, fade).endVertex();
        vertexBuffer.vertex(matrix4f, -xWidth, yWidth, 0.0f).uv(icon.getU0(), icon.getV1()).color(iconR, iconG, iconB, fade).endVertex();
        vertexBuffer.vertex(matrix4f, xWidth, yWidth, 0.0f).uv(icon.getU1(), icon.getV1()).color(iconR, iconG, iconB, fade).endVertex();
        vertexBuffer.vertex(matrix4f, xWidth, -yWidth, 0.0f).uv(icon.getU1(), icon.getV0()).color(iconR, iconG, iconB, fade).endVertex();
        tessellator.end();
        RenderSystem.disableTexture();

        Font fontRenderer = mc.font;
        if (isPointedAt && fontRenderer != null) {
            int elevateBy = -19;
            RenderSystem.enablePolygonOffset();
            int halfStringWidth = fontRenderer.width(name) / 2;
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            RenderSystem.polygonOffset(1.0f, 11.0f);
            vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            vertexBuffer.vertex(matrix4f, -halfStringWidth - 2, -2 + elevateBy, 0.0f).color(0.0f, 0.0f, 0.0f, 0.6f * fade).endVertex();
            vertexBuffer.vertex(matrix4f, -halfStringWidth - 2, 9 + elevateBy, 0.0f).color(0.0f, 0.0f, 0.0f, 0.6f * fade).endVertex();
            vertexBuffer.vertex(matrix4f, halfStringWidth + 2, 9 + elevateBy, 0.0f).color(0.0f, 0.0f, 0.0f, 0.6f * fade).endVertex();
            vertexBuffer.vertex(matrix4f, halfStringWidth + 2, -2 + elevateBy, 0.0f).color(0.0f, 0.0f, 0.0f, 0.6f * fade).endVertex();
            tessellator.end();

            RenderSystem.polygonOffset(1.0f, 9.0f);
            vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            vertexBuffer.vertex(matrix4f, -halfStringWidth - 1, -1 + elevateBy, 0.0f).color(0.0f, 0.0f, 0.0f, 0.15f * fade).endVertex();
            vertexBuffer.vertex(matrix4f, -halfStringWidth - 1, 8 + elevateBy, 0.0f).color(0.0f, 0.0f, 0.0f, 0.15f * fade).endVertex();
            vertexBuffer.vertex(matrix4f, halfStringWidth + 1, 8 + elevateBy, 0.0f).color(0.0f, 0.0f, 0.0f, 0.15f * fade).endVertex();
            vertexBuffer.vertex(matrix4f, halfStringWidth + 1, -1 + elevateBy, 0.0f).color(0.0f, 0.0f, 0.0f, 0.15f * fade).endVertex();
            tessellator.end();
            RenderSystem.disablePolygonOffset();

            RenderSystem.enableTexture();
            int textColor = (int) (255.0f * fade) << 24 | 0xCCCCCC;
            RenderSystem.disableDepthTest();
            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(tessellator.getBuilder());
            fontRenderer.drawInBatch(Component.literal(name), (float) (-fontRenderer.width(name) / 2), (float) elevateBy, textColor, false, matrix4f, bufferSource, true, 0, 0xF000F0);
            bufferSource.endLastBatch();
            bufferSource.endBatch();
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
        RenderSystem.enableTexture();
    }
}
