package dev.zenhao.melon.notification.HudNotification.notifications;

import dev.zenhao.melon.notification.HudNotification.StackNotification;
import dev.zenhao.melon.notification.NotificationType;
import dev.zenhao.melon.utils.TimerUtils;
import dev.zenhao.melon.utils.render.FadeUtils;
import dev.zenhao.melon.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;

import java.awt.*;

public class NormalStackNotification extends StackNotification {

    public static String ICON_NOTIFY_INFO = "\u2139";
    public static String ICON_NOTIFY_SUCCESS = "\u2713";
    public static String ICON_NOTIFY_WARN = "\u26A0";
    public static String ICON_NOTIFY_ERROR = "\u26A0";
    public static String ICON_NOTIFY_DISABLED = "\u2717";
    public double width, height;
    public Color color;

    public NormalStackNotification(NotificationType type, String message, int length) {
        super(type, message, length);
        width = Minecraft.getMinecraft().fontRenderer.getStringWidth(message) + 35;
        height = 16;//22;
        if (type.equals(NotificationType.INFO)) {
            color = new Color(37, 37, 37);
        } else if (type.equals(NotificationType.ERROR)) {
            color = new Color(36, 36, 36);
        } else if (type.equals(NotificationType.SUCCESS)) {
            color = new Color(36, 36, 36);
        } else if (type.equals(NotificationType.DISABLE)) {
            color = new Color(36, 36, 36);
        } else if (type.equals(NotificationType.WARNING)) {
            color = new Color(37, 37, 37);
        }
    }

    public static Color reAlpha(Color color, float alpha) {
        float r = 0.003921569f * color.getRed();
        float g = 0.003921569f * color.getGreen();
        float b = 0.003921569f * color.getBlue();
        return new Color(r, g, b, alpha);
    }

    public int[] rendering(int renderX, int renderY) {
        if (!isShown()) return new int[]{renderX, renderY};
        width = Minecraft.getMinecraft().fontRenderer.getStringWidth(message) + 25;
        double animationX = width * this.getOffset(FadeUtils.FadeType.FADE_EASE_QUAD);
        double animationY = height * this.getOffset(FadeUtils.FadeType.FADE_EASE_QUAD);

        int x1 = (int) (renderX - animationX);
        int y1 = (int) (state.equals(State.FADE_IN) ? renderY + (height - animationY) : state.equals(State.FADE_OUT) ? renderY + (animationY - height) : renderY);
        Color color = reAlpha(this.color, (float) this.getOffset(FadeUtils.FadeType.FADE_DEFAULT));
        int fontColor = reAlpha(new Color(-65794), (float) this.getOffset(FadeUtils.FadeType.FADE_DEFAULT)).getRGB();
        int color1 = reAlpha(Color.white, (float) this.getOffset(FadeUtils.FadeType.FADE_DEFAULT)).getRGB();

        RenderUtils.drawRect(x1, y1, width, height, color);
        RenderUtils.drawLine(x1, y1 + height, x1 + (width * FadeUtils.easeInQuad((double) this.getTime() / this.end)), y1 + height, 2, new Color(color1, true));

        if (this.getOffset(FadeUtils.FadeType.FADE_DEFAULT) >= 0.96) {
            int rt = (int) (height / 2 - Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT / 2);
            switch (type) {
                case ERROR:
                    Minecraft.getMinecraft().fontRenderer.drawString(ICON_NOTIFY_ERROR, x1 + 5, y1 + rt, fontColor);
                    break;
                case INFO:
                    Minecraft.getMinecraft().fontRenderer.drawString(ICON_NOTIFY_INFO, x1 + 5, y1 + rt, fontColor);
                    break;
                case SUCCESS:
                    Minecraft.getMinecraft().fontRenderer.drawString(ICON_NOTIFY_SUCCESS, x1 + 5, y1 + rt, fontColor);
                    break;
                case WARNING:
                    Minecraft.getMinecraft().fontRenderer.drawString(ICON_NOTIFY_WARN, x1 + 5, y1 + rt, fontColor);
                    break;
                case DISABLE:
                    Minecraft.getMinecraft().fontRenderer.drawString(ICON_NOTIFY_DISABLED, x1 + 5, y1 + rt, fontColor);
                    break;

            }
            y1 += 1;
            Minecraft.getMinecraft().fontRenderer.drawString(message, (x1 + 20), (int) (y1 + height / 4F), color1);
        }
        return new int[]{renderX, (int) (y1 + height)};
    }
}
