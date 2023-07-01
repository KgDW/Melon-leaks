package melon.system.render.gui.clickgui

import dev.zenhao.melon.utils.extension.remove
import dev.zenhao.melon.utils.vector.Vec2f
import melon.system.render.gui.clickgui.component.ModuleButton
import melon.system.render.gui.clickgui.window.ModuleSettingWindow
import melon.system.module.AbstractModule
import melon.system.module.ModuleManager
import melon.system.render.RenderManager
import melon.system.render.component.Component
import melon.system.render.component.impl.windows.ListWindow
import melon.system.render.gui.AbstractClientGui

object MelonClickGui : AbstractClientGui<ModuleSettingWindow, AbstractModule>() {

    private val moduleWindows = ArrayList<ListWindow>()

    init {
        val allButtons = ModuleManager.modules
            .groupBy { it.category.displayName }
            .mapValues { (_, modules) -> modules.map { ModuleButton(it) } }

        var posX = 5.0f
        var posY = 15.0f
        val screenWidth = mc.displayWidth / RenderManager.ScaleInfo.scaleFactorFloat

        for ((category, buttons) in allButtons) {
            val window = ListWindow(category, posX, posY, 90.0f, 300.0f, ).apply {
                settingGroup = Component.SettingGroup.CLICK_GUI
            }

            window.children.addAll(buttons)
            moduleWindows.add(window)
            posX += 95.0f

            if (posX > screenWidth) {
                posX = 0.0f
                posY += 100.0f
            }
        }

        windowList.addAll(moduleWindows)
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        setModuleButtonVisibility { true }
    }

    override fun newSettingWindow(element: AbstractModule, mousePos: Vec2f): ModuleSettingWindow {
        return ModuleSettingWindow(element, mousePos.x, mousePos.y)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
//        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == ClickGUI.bind.value.key && !searching && settingWindow?.listeningChild == null) {
//            ClickGUI.disable()
//        } else {
            super.keyTyped(typedChar, keyCode)

            val string = typedString.remove(' ')

            if (string.isNotEmpty()) {
                setModuleButtonVisibility { moduleButton ->
                    moduleButton.module.name.contains(string, true)
                        || moduleButton.module.alias.any { it.contains(string, true) }
                }
            } else {
                setModuleButtonVisibility { true }
            }
//        }
    }

    private fun setModuleButtonVisibility(function: (ModuleButton) -> Boolean) {
        windowList.filterIsInstance<ListWindow>().forEach {
            for (child in it.children) {
                if (child !is ModuleButton) continue
                child.visible = function(child)
            }
        }
    }
}