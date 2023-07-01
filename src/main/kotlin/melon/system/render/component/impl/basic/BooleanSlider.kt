package melon.system.render.component.impl.basic

open class BooleanSlider(
    name: String,
    description: String,
    visibility: (() -> Boolean)? = null
) : Slider(name, description, visibility)