package dev.zenhao.melon.module.hud.huds

import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.notification.HudNotification.StackNotificationManager
import dev.zenhao.melon.setting.Setting

@HUDModule.Info(name = "NotificationHUD", x = 500, y = 300, width = 100, height = 175)
class StackNotificationHUD : HUDModule() {
    var maxStack: Setting<Int> = isetting("MaxStack", 7, 1, 20)
    override fun onRender() {
        StackNotificationManager.update(maxStack.value)
        StackNotificationManager.render(x + width, y)
    }
}