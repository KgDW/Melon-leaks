package dev.zenhao.melon.gui.clickgui.util;

import dev.zenhao.melon.manager.GuiManager;
import dev.zenhao.melon.utils.Rainbow;
import dev.zenhao.melon.utils.render.RenderUtils;
import org.lwjgl.opengl.GL11;

public class SpecialRender {
    public static void draw(double x, double y, double width, double height, double spacing, int change) {
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glShadeModel(7425);
        GL11.glEnable(2848);
        GL11.glBegin(2);
        setsRainbow(0L);
        GL11.glVertex2d(x, y);
        int d = 0;
        int s = 0;
        while ((double) s <= height / spacing) {
            setsRainbow((long) change * (long) s);
            GL11.glVertex2d(x, y + spacing * (double) s);
            d = s++;
        }
        GL11.glVertex2d(x, y + height);
        GL11.glVertex2d(x + width, y + height);
        s = 0;
        while ((double) s <= height / spacing) {
            setsRainbow((long) change * (long) (d - s));
            GL11.glVertex2d(x + width, y + height - spacing * (double) s);
            ++s;
        }
        setsRainbow(0L);
        GL11.glVertex2d(x + width, y);
        GL11.glEnd();
        GL11.glDisable(2848);
        GL11.glDisable(3042);
        GL11.glEnable(3553);
    }

    public static void drawSLine(double x, double y1, double y2, int height, int start, int change) {
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glShadeModel(7425);
        GL11.glEnable(2848);
        double miny = Math.min(y1, y2);
        double maxy = Math.max(y1, y2);
        GL11.glBegin(3);
        setsRainbow(start);
        GL11.glVertex2d(x, miny);
        int d = 0;
        int s = 0;
        while ((double) s <= (maxy - miny) / (double) height) {
            setsRainbow(start + (long) change * s);
            GL11.glVertex2d(x, miny + (double) (height * s));
            d = s++;
        }
        setsRainbow((long) start + (long) change * (long) (d + 1));
        GL11.glVertex2d(x, maxy);
        GL11.glEnd();
        GL11.glDisable(2848);
        GL11.glDisable(3042);
        GL11.glEnable(3553);
    }

    private static void setsRainbow(long add) {
        RenderUtils.setColor(Rainbow.getRainbow(GuiManager.getINSTANCE().getColorINSTANCE().rainbowSpeed.getValue(), GuiManager.getINSTANCE().getColorINSTANCE().rainbowSaturation.getValue(), GuiManager.getINSTANCE().getColorINSTANCE().rainbowBrightness.getValue(), add));
    }
}

