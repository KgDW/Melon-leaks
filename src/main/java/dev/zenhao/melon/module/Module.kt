package dev.zenhao.melon.module

import dev.zenhao.melon.setting.ModeSetting

open class Module : IModule() {
    var visible: ModeSetting<*>

    init {
        moduleName = annotation.name
        this.category = annotation.category
        description = annotation.description
        setBind(annotation.keyCode)
        visible = ModeSetting("Visible", this, if (annotation.visible) Visible.ON else Visible.OFF)
        this.settingList.add(visible)
        isHUD = false
        onInit()
        INSTANCEModule = this
    }

    val isShownOnArray: Boolean
        get() = visible.value == Visible.ON

    open fun onInit() {}
    private val annotation: Info
        get() {
            if (this.javaClass.isAnnotationPresent(Info::class.java)) {
                return this.javaClass.getAnnotation(Info::class.java)
            }
            throw IllegalStateException("No Annotation on class " + this.javaClass.canonicalName + "!")
        }

    enum class Visible {
        ON, OFF
    }

    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        val name: String,
        val description: String = "",
        val keyCode: Int = 0,
        val category: Category,
        val visible: Boolean = true
    )

    companion object {
        lateinit var INSTANCEModule: Module

        @JvmStatic
        fun fullNullCheck(): Boolean {
            return mc.player == null || mc.world == null
        }

        @JvmStatic
        fun nullCheck(): Boolean {
            return mc.player == null
        }
    }
}