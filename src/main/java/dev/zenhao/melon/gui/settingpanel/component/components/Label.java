package dev.zenhao.melon.gui.settingpanel.component.components;

import dev.zenhao.melon.gui.settingpanel.Window;
import dev.zenhao.melon.gui.settingpanel.component.AbstractComponent;

public class Label
        extends AbstractComponent {
    private String text;

    public Label(String text) {
        this.setText(text);
    }

    @Override
    public void render() {
        Window.getFontRenderer().drawString(this.text, this.x, this.y, Window.FONT.getRGB());
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.setWidth(Window.getFontRenderer().getStringWidth(text));
        this.setHeight(Window.getFontRenderer().getHeight());
        this.text = text;
    }
}

