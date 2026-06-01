package dev.codexbat.vesper.client.plush;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

/**
 * Renders a single PlushyCuboid by emitting six quads (skipping null faces)
 * directly into a VertexConsumer.
 *
 * UV convention: [u0,v0] is the top-left corner of the texture rectangle,
 * [u1,v1] is the bottom-right. Reversed UV (u0 > u1) mirrors that face —
 * handled automatically by passing the values through unmodified.
 */
public final class PlushyCuboidRenderer {

    /** 1 pixel = 1/16 of a block. */
    private static final float S = 1f / 16f;

    public static void render(MatrixStack ms, VertexConsumer vc, PlushyCuboid c, int light, int overlay) {
        ms.push();

        // Apply this element's own Euler rotation around its pivot
        if (c.rotX() != 0 || c.rotY() != 0 || c.rotZ() != 0) {
            float px = c.pivX() * S, py = c.pivY() * S, pz = c.pivZ() * S;
            ms.translate(px, py, pz);
            if (c.rotX() != 0) ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(c.rotX()));
            if (c.rotY() != 0) ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(c.rotY()));
            if (c.rotZ() != 0) ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(c.rotZ()));
            ms.translate(-px, -py, -pz);
        }

        float x1 = c.x1() * S, y1 = c.y1() * S, z1 = c.z1() * S;
        float x2 = c.x2() * S, y2 = c.y2() * S, z2 = c.z2() * S;
        MatrixStack.Entry e = ms.peek();

        // Each face: 4 vertices in CCW order when viewed from outside.
        // Vertex layout → UV layout: v0=top-right, v1=top-left, v2=bot-left, v3=bot-right
        // UV: (u1,v0) (u0,v0) (u0,v1) (u1,v1)
        if (c.north() != null) quad(e,vc, c.north(), 0, 0,-1, light,overlay, x2,y2,z1, x1,y2,z1, x1,y1,z1, x2,y1,z1);
        if (c.south() != null) quad(e,vc, c.south(), 0, 0, 1, light,overlay, x1,y2,z2, x2,y2,z2, x2,y1,z2, x1,y1,z2);
        if (c.east() != null) quad(e,vc, c.east(), 1, 0, 0, light,overlay, x2,y2,z2, x2,y2,z1, x2,y1,z1, x2,y1,z2);
        if (c.west() != null) quad(e,vc, c.west(), -1, 0, 0, light,overlay, x1,y2,z1, x1,y2,z2, x1,y1,z2, x1,y1,z1);
        if (c.up() != null) quad(e,vc, c.up(), 0, 1, 0, light,overlay, x2,y2,z1, x1,y2,z1, x1,y2,z2, x2,y2,z2);
        if (c.down() != null) quad(e,vc, c.down(), 0,-1, 0, light,overlay, x1,y1,z1, x2,y1,z1, x2,y1,z2, x1,y1,z2);

        ms.pop();
    }

    private static void quad(MatrixStack.Entry e, VertexConsumer vc, FaceUV uv, float nx, float ny, float nz, int light, int overlay, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
        // UV from 0-16 Blockbench space → 0-1 texture coords
        float u0 = uv.u0() / 16f, v0 = uv.v0() / 16f;
        float u1 = uv.u1() / 16f, v1 = uv.v1() / 16f;
        Matrix4f pm = e.getPositionMatrix();
        vc.vertex(pm,x0,y0,z0).color(255,255,255,255).texture(u1,v0).overlay(overlay).light(light).normal(e,nx,ny,nz);
        vc.vertex(pm,x1,y1,z1).color(255,255,255,255).texture(u0,v0).overlay(overlay).light(light).normal(e,nx,ny,nz);
        vc.vertex(pm,x2,y2,z2).color(255,255,255,255).texture(u0,v1).overlay(overlay).light(light).normal(e,nx,ny,nz);
        vc.vertex(pm,x3,y3,z3).color(255,255,255,255).texture(u1,v1).overlay(overlay).light(light).normal(e,nx,ny,nz);
    }
}