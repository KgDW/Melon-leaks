package dev.zenhao.melon.notification;

import dev.zenhao.melon.utils.Rainbow;
import dev.zenhao.melon.utils.render.FadeUtils;
import dev.zenhao.melon.utils.render.RenderUtils;
import dev.zenhao.melon.utils.font.CFontRenderer;
import java.awt.Color;

public abstract class Notification {
    protected final NotificationType type;
    protected final String title;
    protected final String message;
    protected long start;
    protected final long fadedIn;
    protected final long fadeOut;
    protected final long end;
    protected CFontRenderer font = RenderUtils.getFontRender();
    protected State state = State.FADE_IN;

    public Notification(NotificationType type, String title, String message, int length) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.fadedIn = 100L * (long)length;
        this.fadeOut = this.fadedIn + 150L * (long)length;
        this.end = this.fadeOut + this.fadedIn;
    }

    public Notification show() {
        this.start = System.currentTimeMillis();
        return this;
    }

    public boolean isShown() {
        return this.getTime() <= (this.end);
    }

    protected long getTime() {
        return System.currentTimeMillis() - this.start;
    }

    protected double getOffset(FadeUtils.FadeType fadeType) {
        if (this.getTime() < this.fadedIn) {
            state = State.FADE_IN;
            return FadeUtils.getFadeType(fadeType, true, (double)this.getTime() / this.fadedIn);
        }
        if (this.getTime() > this.fadeOut) {
            state = State.FADE_OUT;
            return FadeUtils.getFadeType(fadeType, false, (double) (this.getTime() - this.fadeOut) / (this.end - this.fadeOut));
        }
        state = State.STAY;
        return 1;
    }

    protected Color getDefaultTypeColor() {
        switch (this.type){
            case INFO:
                return Color.BLUE;
            case ERROR:
                return Color.RED;
            case DISABLE:
                return  Color.DARK_GRAY;
            case RAINBOW:
                return Rainbow.getRainbowColor(7.0f, 0.75f, 1.0f);
            case SUCCESS:
                return Color.GREEN;
            case WARNING:
                return Color.YELLOW;
            default:
                return Color.WHITE;
        }
    }

    public abstract void render(int var1, int var2);

    protected enum State {
        FADE_IN,
        STAY,
        FADE_OUT
    }
}

