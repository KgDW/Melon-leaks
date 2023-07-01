package dev.zenhao.melon.module.hud.info

import dev.zenhao.melon.manager.FriendManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.utils.chat.ChatUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color
import java.util.stream.Collectors

@HUDModule.Info(name = "Friends", x = 170, y = 170, width = 25, height = 10, category = Category.HUD)
class Friends : HUDModule() {
    private var viewText = ""
    private var DefaultWidth = 60
    override fun onRender() {
        val mutliLineText = viewText.split("\n".toRegex()).toTypedArray()
        var addY = 0
        var maxFontWidth = DefaultWidth
        for (text in mutliLineText) {
            fontRenderer.drawString(text, x, y + addY, Color.WHITE.rgb)
            maxFontWidth = maxFontWidth.coerceAtLeast(fontRenderer.getStringWidth(text))
            addY += fontRenderer.FONT_HEIGHT
        }
        height = if (addY == fontRenderer.FONT_HEIGHT) {
            fontRenderer.FONT_HEIGHT
        } else {
            addY - fontRenderer.FONT_HEIGHT
        }
        width = maxFontWidth
    }

    override fun onUpdate() {
        viewText = ""
        if (friends.isEmpty()) {
            addLine("You have no friends!")
        } else {
            addLine(ChatUtil.SECTIONSIGN.toString() + "3" + ChatUtil.SECTIONSIGN + "l" + "Your Friends")
            val var1: Iterator<*> = friends.iterator()
            while (var1.hasNext()) {
                val e = var1.next() as Entity
                addLine(ChatUtil.SECTIONSIGN.toString() + "6 " + e.name)
            }
        }
    }

    private fun addLine(str: String) {
        viewText = if (viewText.isEmpty()) {
            str
        } else {
            """
     $viewText
     $str
     """.trimIndent()
        }
    }

    companion object {
        val friends: List<EntityPlayer>
            get() = mc.world.playerEntities.stream()
                .filter { entityPlayer: EntityPlayer -> FriendManager.isFriend(entityPlayer.name) }
                .collect(Collectors.toList())
    }
}