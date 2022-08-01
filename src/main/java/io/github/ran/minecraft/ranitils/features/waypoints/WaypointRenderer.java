package io.github.ran.minecraft.ranitils.features.waypoints;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.util.Mth;

// Code from Minecraft's BeaconRenderer
// Some of the code is also from OMMC (https://github.com/plusls/oh-my-minecraft-client)
// Which is by plusls and is licensed under the GNU Lesser General Public License v3.0
public class WaypointRenderer {
    public static void renderBeam(PoseStack poseStack, float partialTick, float textureScale, long gameTime, int yOffset, int height, float[] colors, float beamRadius, float glowRadius) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, BeaconRenderer.BEAM_LOCATION);
        int i = yOffset + height;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        float f = (float)Math.floorMod(gameTime, 40) + partialTick;
        float g = height < 0 ? f : -f;
        float h = Mth.frac(g * 0.2f - (float)Mth.floor(g * 0.1f));
        float j = colors[0];
        float k = colors[1];
        float l = colors[2];
        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(f * 2.25f - 45.0f));
        float m;
        float n = beamRadius;
        float o = beamRadius;
        float p;
        float q = -beamRadius;
        float r;
        float s;
        float t = -beamRadius;
        float w = -1.0f + h;
        float x = (float)height * textureScale * (0.5f / beamRadius) + w;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        renderPart(poseStack, bufferBuilder, j, k, l, 1.0f, yOffset, i, 0.0f, n, o, 0.0f, q, 0.0f, 0.0f, t, 0.0f, 1.0f, x, w);
        tesselator.end();
        poseStack.popPose();
        m = -glowRadius;
        n = -glowRadius;
        o = glowRadius;
        p = -glowRadius;
        q = -glowRadius;
        r = glowRadius;
        s = glowRadius;
        t = glowRadius;
        w = -1.0f + h;
        x = (float)height * textureScale + w;
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        WaypointRenderer.renderPart(poseStack, bufferBuilder, j, k, l, 0.125f, yOffset, i, m, n, o, p, q, r, s, t, 0.0f, 1.0f, x, w);
        tesselator.end();
        poseStack.popPose();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void renderPart(PoseStack poseStack, VertexConsumer consumer, float red, float green, float blue, float alpha, int minY, int maxY, float x0, float z0, float x1, float z1, float x2, float z2, float x3, float z3, float minU, float maxU, float minV, float maxV) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        WaypointRenderer.renderQuad(matrix4f, consumer, red, green, blue, alpha, minY, maxY, x0, z0, x1, z1, minU, maxU, minV, maxV);
        WaypointRenderer.renderQuad(matrix4f, consumer, red, green, blue, alpha, minY, maxY, x3, z3, x2, z2, minU, maxU, minV, maxV);
        WaypointRenderer.renderQuad(matrix4f, consumer, red, green, blue, alpha, minY, maxY, x1, z1, x3, z3, minU, maxU, minV, maxV);
        WaypointRenderer.renderQuad(matrix4f, consumer, red, green, blue, alpha, minY, maxY, x2, z2, x0, z0, minU, maxU, minV, maxV);
    }

    private static void renderQuad(Matrix4f pose, VertexConsumer consumer, float red, float green, float blue, float alpha, int minY, int maxY, float minX, float minZ, float maxX, float maxZ, float minU, float maxU, float minV, float maxV) {
        WaypointRenderer.addVertex(pose, consumer, red, green, blue, alpha, maxY, minX, minZ, maxU, minV);
        WaypointRenderer.addVertex(pose, consumer, red, green, blue, alpha, minY, minX, minZ, maxU, maxV);
        WaypointRenderer.addVertex(pose, consumer, red, green, blue, alpha, minY, maxX, maxZ, minU, maxV);
        WaypointRenderer.addVertex(pose, consumer, red, green, blue, alpha, maxY, maxX, maxZ, minU, minV);
    }

    private static void addVertex(Matrix4f pose, VertexConsumer consumer, float red, float green, float blue, float alpha, int y, float x, float z, float u, float v) {
        consumer.vertex(pose, x, (float) y, z).uv(u, v).color(red, green, blue, alpha).endVertex();
    }
}
