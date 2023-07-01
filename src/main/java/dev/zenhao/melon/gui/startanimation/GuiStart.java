package dev.zenhao.melon.gui.startanimation;

import dev.zenhao.melon.gui.startanimation.component.MelonUI;
import dev.zenhao.melon.utils.TimerUtils;
import dev.zenhao.melon.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiScreen;

import java.awt.*;
import java.util.concurrent.LinkedBlockingQueue;

public class GuiStart extends GuiScreen {
    private static final LinkedBlockingQueue<LComponent> pendingNotifications = new LinkedBlockingQueue<>();
    private static LComponent currentNotification = null;
    private final TimerUtils time = new TimerUtils();
    private final TimerUtils timer = new TimerUtils();

    public GuiStart() {
        pendingNotifications.add(new MelonUI(2));
        this.time.reset();
    }

    public void drawScreen(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_) {
        RenderUtils.drawRect(0.0, 0.0, this.width, this.height, Color.black);
        if (this.time.passed(1300L)) {
            if (currentNotification != null && !currentNotification.isShown()) {
                timer.reset();
                currentNotification = null;
            }
            if (currentNotification == null && !pendingNotifications.isEmpty()) {
                if (timer.passed(150)) {
                    currentNotification = pendingNotifications.poll();
                    currentNotification.show();
                }
            }
            if (currentNotification != null) {
                currentNotification.render(this.width, this.height);
            }
        }
        if (currentNotification == null) {
            RenderUtils.drawRect(0.0, 0.0, this.width, this.height, Color.black);
        }
    }
}