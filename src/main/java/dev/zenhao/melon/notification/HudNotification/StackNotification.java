package dev.zenhao.melon.notification.HudNotification;

import dev.zenhao.melon.notification.Notification;
import dev.zenhao.melon.notification.NotificationType;

public abstract class StackNotification extends Notification {

    public StackNotification(NotificationType type, String message, int length) {
        super(type, "", message, length);
    }

    @Override
    public void render(int var1, int var2) {
    }

    protected abstract int[] rendering(int x, int y);
}
