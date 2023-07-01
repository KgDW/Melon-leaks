package melon.client.modules.render

import melon.system.module.Category
import melon.system.module.Module

internal class CameraClip : Module(
    name = "CameraClip",
    category = Category.RENDER,
    description = ""
) {
    var extend   by setting("Extend"  , false)
    var distance by setting("Distance", 0.0, 0.0..100.0, 1.0)
}