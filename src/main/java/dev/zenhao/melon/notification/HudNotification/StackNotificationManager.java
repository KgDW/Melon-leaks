package dev.zenhao.melon.notification.HudNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class StackNotificationManager {
    public static final LinkedBlockingQueue<StackNotification> pendingNotifications = new LinkedBlockingQueue<>();
    private static final List<StackNotification> currentNotification = new ArrayList<>();

    public static void add(StackNotification notification) {
        pendingNotifications.add(notification);
    }

    public static void update(int maxStack) {
        if (currentNotification != null && !pendingNotifications.isEmpty() && currentNotification.size() != maxStack) {
            currentNotification.add((StackNotification) pendingNotifications.poll().show());
        }
        if (currentNotification != null && !currentNotification.isEmpty()) {
            currentNotification.removeIf(notification -> !notification.isShown());
        }

    }

    public static void render(int x, int y) {
        try {
            final int[][] dir = {null};
            currentNotification.forEach(notification -> {
                if (notification != null) {
                    if (dir[0] == null) {
                        dir[0] = notification.rendering(x, y);
                    } else {
                        dir[0] = notification.rendering(x, dir[0][1]);
                    }
                }
            });
//            for (StackNotification notification : currentNotification) {
//                notification.render(x, (int) startY);
//                startY += 22 + 2;
//            }
        } catch (Exception ignored) {
        }
    }
}
