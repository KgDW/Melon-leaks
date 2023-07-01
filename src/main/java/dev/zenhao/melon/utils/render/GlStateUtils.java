package dev.zenhao.melon.utils.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class GlStateUtils {
    public static Minecraft mc = Minecraft.getMinecraft();
    private static boolean colorLock;
    private static int bindProgram = 0;

    public static boolean getColorLock() {
        return colorLock;
    }

    public static boolean useVbo() {
        return mc.gameSettings.useVbo;
    }

    public static void matrix(boolean state) {
        if (state) {
            GL11.glPushMatrix();
        } else {
            GL11.glPopMatrix();
        }
    }

    public static void blend(boolean state) {
        if (state) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        } else {
            GlStateManager.disableBlend();
        }
    }

    public static void alpha(boolean state) {
        if (state) {
            GlStateManager.enableAlpha();
        } else {
            GlStateManager.disableAlpha();
        }
    }

    public static void smooth(boolean state) {
        if (state) {
            GlStateManager.shadeModel(7425);
        } else {
            GlStateManager.shadeModel(7424);
        }
    }

    public static void lineSmooth(boolean state) {
        if (state) {
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
        } else {
            GL11.glDisable(2848);
            GL11.glHint(3154, 4352);
        }
    }

    public static void hintPolygon(boolean state) {
        if (state) {
            GL11.glHint(3155, 4354);
        } else {
            GL11.glHint(3155, 4352);
        }
    }

    public static void depth(boolean state) {
        if (state) {
            GlStateManager.enableDepth();
        } else {
            GlStateManager.disableDepth();
        }
    }

    public static void depthMask(boolean state) {
        GlStateManager.depthMask(state);
    }

    public static void texture2d(boolean state) {
        if (state) {
            GlStateManager.enableTexture2D();
        } else {
            GlStateManager.disableTexture2D();
        }
    }

    public static void cull(boolean state) {
        if (state) {
            GlStateManager.enableCull();
        } else {
            GlStateManager.disableCull();
        }
    }

    public static void lighting(boolean state) {
        if (state) {
            GlStateManager.enableLighting();
        } else {
            GlStateManager.disableLighting();
        }
    }

    public static void polygon(boolean state) {
        if (state) {
            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(1.0f, -1500000.0f);
        } else {
            GlStateManager.disablePolygonOffset();
            GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
        }
    }

    public static void smoothTexture() {
        GL11.glTexParameteri(3553, 10241, 9729);
        GL11.glTexParameteri(3553, 10240, 9729);
    }

    public static void resetColour() {
        RenderUtils.glColor(1, 1, 1, 1);
    }

    public static void colorLock(boolean state) {
        colorLock = state;
    }

    public static void resetTexParam() {
        GlStateManager.bindTexture(0);
        GL11.glTexParameteri(3553, 10240, 9729);
        GL11.glTexParameteri(3553, 10241, 9986);
        GL11.glTexParameteri(3553, 10242, 10497);
        GL11.glTexParameteri(3553, 10243, 10497);
        GL11.glTexParameteri(3553, 33085, 1000);
        GL11.glTexParameteri(3553, 33083, 1000);
        GL11.glTexParameteri(3553, 33082, -1000);
    }

    public static void rescale(double width, double height) {
        GlStateManager.clear(256);
        GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0, width, height, 0.0, 1000.0, 3000.0);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0f, 0.0f, -2000.0f);
    }

    public static void rescaleActual() {
        GlStateUtils.rescale(mc.displayWidth, mc.displayHeight);
    }

    public static void rescaleMc() {
        ScaledResolution resolution = new ScaledResolution(mc);
        GlStateUtils.rescale(resolution.getScaledWidth_double(), resolution.getScaledHeight_double());
    }

    public static void useProgram(int id) {
        if (id != bindProgram) {
            GL20.glUseProgram(id);
            bindProgram = id;
        }
    }
}
