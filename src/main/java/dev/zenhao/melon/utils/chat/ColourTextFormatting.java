package dev.zenhao.melon.utils.chat;

import net.minecraft.util.text.TextFormatting;

import java.util.HashMap;
import java.util.Map;

/**
 * @author S-B99
 * Updated by S-B99 on 25/03/20
 */
public class ColourTextFormatting {
    public enum ColourCode {
        BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE
    }

    public static Map<ColourCode, TextFormatting> toTextMap = new HashMap<ColourCode, TextFormatting>() {{
        put(ColourCode.BLACK, TextFormatting.BLACK);
        put(ColourCode.DARK_BLUE, TextFormatting.DARK_BLUE);
        put(ColourCode.DARK_GREEN, TextFormatting.DARK_GREEN);
        put(ColourCode.DARK_AQUA, TextFormatting.DARK_AQUA);
        put(ColourCode.DARK_RED, TextFormatting.DARK_RED);
        put(ColourCode.DARK_PURPLE, TextFormatting.DARK_PURPLE);
        put(ColourCode.GOLD, TextFormatting.GOLD);
        put(ColourCode.GRAY, TextFormatting.GRAY);
        put(ColourCode.DARK_GRAY, TextFormatting.DARK_GRAY);
        put(ColourCode.BLUE, TextFormatting.BLUE);
        put(ColourCode.GREEN, TextFormatting.GREEN);
        put(ColourCode.AQUA, TextFormatting.AQUA);
        put(ColourCode.RED, TextFormatting.RED);
        put(ColourCode.LIGHT_PURPLE, TextFormatting.LIGHT_PURPLE);
        put(ColourCode.YELLOW, TextFormatting.YELLOW);
        put(ColourCode.WHITE, TextFormatting.WHITE);
    }};

}
