package melon.system.render.gui.clickgui.component

import dev.zenhao.melon.utils.vector.Vec2f
import melon.system.render.gui.clickgui.MelonClickGui
import melon.system.module.AbstractModule
import melon.system.render.component.impl.basic.BooleanSlider

class ModuleButton(val module: AbstractModule) : BooleanSlider(module.name, module.description) {
    override val progress: Float
        get() = if (module.isEnabled) 1.0f else 0.0f

    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        if (buttonId == 0) module.toggle()
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        if (buttonId == 1) MelonClickGui.displaySettingWindow(module)
    }
}