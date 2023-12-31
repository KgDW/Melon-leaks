package melon.system.render.component.impl.basic

import dev.zenhao.melon.utils.vector.Vec2f

class CheckButton(
    name: String,
    stateIn: Boolean,
    description: String = "",
    visibility: ((() -> Boolean))? = null
) : BooleanSlider(name, description, visibility) {
    private var state = stateIn
    override val progress: Float
        get() = if (state) 1.0f else 0.0f

    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        state = !state
    }
}