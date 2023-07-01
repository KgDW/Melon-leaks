package dev.zenhao.melon.gui.startanimation.component;

import dev.zenhao.melon.gui.startanimation.LComponent;
import dev.zenhao.melon.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.awt.*;

public class MelonUI extends LComponent {
    private DynamicTexture texture;

    public MelonUI(int length) {
        super(length);
    }

    @Override
    public void render(int displayWidth, int displayHeight) {
        double height = displayHeight / 2f;
        double width = displayWidth / 2f;
        //GL11.glBindTexture(3553, this.texture.getGlTextureId());
        //RenderUtils.drawTexture(width - 50.0, height - 50.0 + 7.0, 100.0, 100.0, this.getAlpha());
        RenderUtils.drawRect(0, 0, displayWidth, displayHeight, new Color(0, 0, 0, 255));
        Minecraft.getMinecraft().fontRenderer.drawString("屎菌奇.NIGGER", (int) (width - 50), (int) (height - 50 + 7), new Color(255, 255, 255, 255).getRGB());
    }
}