package dev.zenhao.melon.gui.clickgui.font;

import java.awt.Color;

public interface FontRenderer {
    int getFontHeight();

    int getStringHeight(String var1);

    int getStringWidth(String var1);

    void drawString(int var1, int var2, String var3);

    void drawString(int var1, int var2, int var3, int var4, int var5, String var6);

    void drawString(int var1, int var2, Color var3, String var4);

    void drawString(int var1, int var2, int var3, String var4);

    void drawStringWithShadow(int var1, int var2, int var3, int var4, int var5, String var6);
}

