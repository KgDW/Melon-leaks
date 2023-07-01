package dev.zenhao.melon.utils.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL41;

import java.awt.*;

public class GLWindowView {
    public static void start(int x, int y, int width, int height) {
        start(x, y, width, height, 255);
    }

    public static void start(int x, int y, int width, int height, int alpha) {
        GL11.glPushMatrix();
        GL41.glClearDepthf(1.0f);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glColorMask(false, false, false, false);
        GL11.glDepthFunc(513);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);

        RenderUtils.drawRect(x, y, width, height, new Color(255, 255, 255, alpha));

        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);
        GL11.glDepthFunc(514);
    }

    public static void end(){
        GL41.glClearDepthf(1.0f);
        GL11.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glClear(1280);
        GL11.glDisable(2929);
        GL11.glDepthFunc(515);
        GL11.glDepthMask(false);
        GL11.glPopMatrix();
    }
}




