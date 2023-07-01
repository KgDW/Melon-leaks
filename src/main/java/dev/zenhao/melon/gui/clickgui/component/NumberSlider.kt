package dev.zenhao.melon.gui.clickgui.component

import dev.zenhao.melon.gui.clickgui.Panel
import dev.zenhao.melon.manager.GuiManager
import dev.zenhao.melon.setting.DoubleSetting
import dev.zenhao.melon.setting.FloatSetting
import dev.zenhao.melon.setting.IntegerSetting
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.render.RenderUtils
import net.minecraft.client.gui.Gui
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11
import java.awt.Color

class NumberSlider<T>(value: Setting<T>?, width: Int, height: Int, father: Panel?) : SettingButton<T>() {
    var sliding = false

    init {
        this.width = width
        this.height = height
        this.father = father
        this.value = value
    }

    override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
        var intValue: IntegerSetting
        var floatValue: FloatSetting
        val percentBar: Double
        var doubleValue: DoubleSetting
        val font = GuiManager.getINSTANCE().font
        if (!value.visible()) {
            sliding = false
        }
        val color = if (GuiManager.getINSTANCE().isRainbow) GuiManager.getINSTANCE()
            .getRainbowColorAdd(add.toLong(), 192) else GuiManager.getINSTANCE().rgb
        val fontColor = Color(255, 255, 255).rgb
        //画出数字框
        Gui.drawRect(x, y, x + width, y + height, -2063597568)
        var iwidth = 0.0
        var displayvalue = "0"
        val sliderWidth = width - 2
        when (value) {
            is DoubleSetting -> {
                doubleValue = value as DoubleSetting
                displayvalue = String.format("%.1f", doubleValue.value)
                percentBar = (doubleValue.value - doubleValue.min) / (doubleValue.max - doubleValue.min)
                iwidth = sliderWidth.toDouble() * percentBar
            }
            is FloatSetting -> {
                floatValue = value as FloatSetting
                displayvalue = String.format("%.1f", floatValue.value)
                percentBar = ((floatValue.value - floatValue.min) / (floatValue.max - floatValue.min)).toDouble()
                iwidth = sliderWidth.toDouble() * percentBar
            }
            is IntegerSetting -> {
                intValue = value as IntegerSetting
                displayvalue = intValue.value.toString()
                percentBar = (intValue.value!! - intValue.min).toDouble() / (intValue.max - intValue.min).toDouble()
                iwidth = sliderWidth.toDouble() * percentBar
            }
        }
        //数值
        Gui.drawRect(x + 1, y + height - 4, x + 1 + iwidth.toInt(), y + height - 5, color)
        RenderUtils.drawCircle(
            x + 1 + iwidth,
            y + height - 4.5,
            2.5,
            Color(color)
        )
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        //画出设置的value
        if (sliding) {
            val diff: Double
            when (value) {
                is DoubleSetting -> {
                    doubleValue = value as DoubleSetting
                    diff = doubleValue.max - doubleValue.min
                    val modifyVal = doubleValue.min + MathHelper.clamp(
                        (mouseX.toDouble() - (x + if (doubleValue.modify != 0.0) doubleValue.modify.toInt() else 1).toDouble()) / sliderWidth.toDouble(),
                        0.0,
                        1.0
                    ) * diff
                    doubleValue.value = modifyVal
                }

                is FloatSetting -> {
                    floatValue = value as FloatSetting
                    diff = (floatValue.max - floatValue.min).toDouble()
                    val modifyVal = floatValue.min.toDouble() + MathHelper.clamp(
                        (mouseX.toDouble() - (x + if (floatValue.modify != 0.0f) floatValue.modify.toInt() else 1).toDouble()) / sliderWidth.toDouble(),
                        0.0,
                        1.0
                    ) * diff
                    floatValue.value = modifyVal.toFloat()
                }

                is IntegerSetting -> {
                    intValue = value as IntegerSetting
                    diff = (intValue.max - intValue.min).toDouble()
                    val modifyVal = intValue.min.toDouble() + MathHelper.clamp(
                        (mouseX.toDouble() - (x + if (intValue.modify != 0) intValue.modify else 1).toDouble()) / sliderWidth.toDouble(),
                        0.0,
                        1.0
                    ) * diff
                    intValue.value = modifyVal.toInt()
                }
            }
        }
        font.drawString(
            value.name,
            x + 3F,
            y + 1F,
            fontColor
        )
        font.drawString(
            displayvalue,
            x + width - 1 - font.getStringWidth(displayvalue).toFloat(),
            y + 1F,
            if (isHovered(mouseX, mouseY)) Color.WHITE.rgb else 0x909090
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (!value.visible() || !isHovered(mouseX, mouseY)) {
            return false
        }
        if (mouseButton == 0) {
            sliding = true
            return true
        }
        return false
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        sliding = false
    }
}