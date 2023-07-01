package dev.zenhao.melon.utils.render

import melon.system.util.interfaces.DisplayEnum

enum class HAlign(override val displayName: String, val multiplier: Float, val offset: Float) : DisplayEnum {
    LEFT("Left", 0.0f, -1.0f),
    CENTER("Center", 0.5f, 0.0f),
    RIGHT("Right", 1.0f, 1.0f)
}

enum class VAlign(override val displayName: String, val multiplier: Float, val offset: Float) : DisplayEnum {
    TOP("Top", 0.0f, -1.0f),
    CENTER("Center", 0.5f, 0.0f),
    BOTTOM("Bottom", 1.0f, 1.0f)
}

enum class Align(override val displayName: String, vAlign: VAlign, val hAlign: HAlign) : DisplayEnum {
    TOP_LEFT("Top Left", VAlign.TOP, HAlign.LEFT),
    TOP_RIGHT("Top Right", VAlign.TOP, HAlign.RIGHT),
    TOP_CENTER("Top Center", VAlign.TOP, HAlign.CENTER),
    CENTER_LEFT("Center Left", VAlign.CENTER, HAlign.LEFT),
    CENTER_RIGHT("Center Right", VAlign.CENTER, HAlign.RIGHT),
    CENTER_CENTER("Center Center", VAlign.CENTER, HAlign.CENTER),
    BOTTOM_LEFT("Bottom Left", VAlign.BOTTOM, HAlign.LEFT),
    BOTTOM_RIGHT("Bottom Right", VAlign.BOTTOM, HAlign.RIGHT),
    BOTTOM_CENTER("Bottom Center", VAlign.BOTTOM, HAlign.CENTER),
}