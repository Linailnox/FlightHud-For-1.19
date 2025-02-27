package com.plr.flighthud.api;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Vector3f;
import com.mojang.math.Matrix4f;
import com.plr.flighthud.common.Dimensions;
import com.plr.flighthud.common.FlightComputer;
import com.plr.flighthud.common.config.HudConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

public abstract class HudComponent {
    
    public abstract void render(PoseStack ctx, float partial, Minecraft client);
    
    public static HudConfig CONFIG;
    
    protected int i(double d) {
        return (int) Math.round(d);
    }
    
    protected void drawPointer(PoseStack ctx, float x, float y, float rot) {
        this.drawPointer(ctx, x, y, rot, CONFIG.getColorRGB());
    }
    
    protected void drawPointer(PoseStack ctx, float x, float y, float rot, int color) {
        ctx.pushPose();
        ctx.translate(x, y, 0);
        ctx.mulPose(Vector3f.ZP.rotationDegrees(rot + 45));
        drawVerticalLine(ctx, 0, 0, 5, color);
        drawHorizontalLine(ctx, 0, 5, 0, color);
        ctx.popPose();
    }
    
    protected float wrapHeading(float degrees) {
        degrees = degrees % 360;
        while (degrees < 0) {
            degrees += 360;
        }
        return degrees;
    }
    
    protected void drawFont(Minecraft mc, PoseStack ctx, String s, float x, float y) {
        this.drawFont(mc, ctx, s, x, y, CONFIG.getColorRGB());
    }

    protected void drawFont(Minecraft mc, PoseStack ctx, String s, float x, float y, int color) {
        mc.font.draw(ctx, s, x, y, color);
    }
    
    protected void drawRightAlignedFont(Minecraft mc, PoseStack ctx, String s, float x, float y) {
        this.drawRightAlignedFont(mc, ctx, s, x, y, CONFIG.getColorRGB());
    }
    
    protected void drawRightAlignedFont(Minecraft mc, PoseStack ctx, String s, float x, float y, int color) {
        drawFont(mc, ctx, s, x - mc.font.width(s), y, color);
    }
    
    protected void drawBox(PoseStack ctx, float x, float y, float w, float h) {
        this.drawBox(ctx, x, y, w, h, CONFIG.getColorRGB());
    }
    
    protected void drawBox(PoseStack ctx, float x, float y, float w, float h, int color) {
        drawHorizontalLine(ctx, x, x + w, y, color);
        drawHorizontalLine(ctx, x, x + w, y + h, color);
        drawVerticalLine(ctx, x, y, y + h, color);
        drawVerticalLine(ctx, x + w, y, y + h, color);
    }
    
    protected void drawHorizontalLineDashed(PoseStack ctx, float x1, float x2, float y,
                                            int dashCount) {
        this.drawHorizontalLineDashed(ctx, x1, x2, y, dashCount, CONFIG.getColorRGB());
    }
    
    protected void drawHorizontalLineDashed(PoseStack ctx, float x1, float x2, float y,
                                            int dashCount, int color) {
        float width = x2 - x1;
        int segmentCount = dashCount * 2 - 1;
        float dashSize = width / segmentCount;
        for (int i = 0; i < segmentCount; i++) {
            if (i % 2 != 0) {
                continue;
            }
            float dx1 = i * dashSize + x1;
            float dx2;
            if (i == segmentCount - 1) {
                dx2 = x2;
            } else {
                dx2 = ((i + 1) * dashSize) + x1;
            }
            drawHorizontalLine(ctx, dx1, dx2, y, color);
        }
    }
    
    protected void drawHorizontalLine(PoseStack ctx, float x1, float x2, float y) {
        this.drawHorizontalLine(ctx, x1, x2, y, CONFIG.getColorRGB());
    }
    
    protected void drawHorizontalLine(PoseStack ctx, float x1, float x2, float y, int color) {
        if (x2 < x1) {
            float i = x1;
            x1 = x2;
            x2 = i;
        }
        final float half = CONFIG.getHalfThickness();
        fill(ctx, x1 - half, y - half, x2 + half, y + half, color);
    }
    
    protected void drawVerticalLine(PoseStack ctx, float x, float y1, float y2) {
        this.drawVerticalLine(ctx, x, y1, y2, CONFIG.getColorRGB());
    }
    
    protected void drawVerticalLine(PoseStack ctx, float x, float y1, float y2, int color) {
        if (y2 < y1) {
            float i = y1;
            y1 = y2;
            y2 = i;
        }
        final float half = CONFIG.getHalfThickness();
        fill(ctx, x - half, y1 + half, x + half, y2 - half, color);
    }
    
    public static void fill(PoseStack ctx, float x1, float x2, float y1, float y2) {
        fill(ctx, x1, y1, x2, y2, CONFIG.getColorRGB());
    }
    
    public static void fill(PoseStack ctx, float x1, float y1, float x2, float y2, int color) {
        float j;

        if (x1 < x2) {
            j = x1;
            x1 = x2;
            x2 = j;
        }

        if (y1 < y2) {
            j = y1;
            y1 = y2;
            y2 = j;
        }
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = ctx.last().pose();
        bufferBuilder.vertex(matrix, x1, y2, 0.0F).color(r, g, b, alpha).endVertex();
        bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(r, g, b, alpha);
        bufferBuilder.vertex(matrix, x2, y1, 0.0F).color(r, g, b, alpha);
        bufferBuilder.vertex(matrix, x1, y1, 0.0F).color(r, g, b, alpha);
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public interface Provider {
        HudComponent provide(FlightComputer computer, Dimensions dim);
    }
}