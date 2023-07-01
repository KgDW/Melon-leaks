package dev.zenhao.melon.utils.BackgroundEffect.FakeMeteor;

import dev.zenhao.melon.utils.Rainbow;
import dev.zenhao.melon.utils.TimerUtils;
import dev.zenhao.melon.utils.render.FadeUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.Random;

public class Meteor {
    public static Random random = new Random();
    private final TimerUtils timerUtils = new TimerUtils();
    private final FadeUtils fadeUtils = new FadeUtils(170L);
    public Vector2f pos, pos2;
    public float LineWidth, LineLength, alpha;
    public long speedMS;
    public Color randomColor;

    public Meteor(long speedMS, float x, float y, float LineWidth, float LineLength) {
        this.speedMS = speedMS;
        this.pos = new Vector2f(x, y);
        this.pos2 = new Vector2f(x, y);
        this.LineWidth = LineWidth;
        this.LineLength = LineLength;
        this.randomColor = new Color(Color.HSBtoRGB(random.nextInt(360), 0.4f, 1.0f));
        fadeUtils.setLength((speedMS / 5) * 2);
    }

    public static Meteor generateMeteor() {
        long speedMS = 3000 + random.nextInt(1200);
        float x = random.nextInt(Display.getWidth());
        float y = random.nextInt(Display.getHeight());
        float lineLength = 50 + random.nextInt(300);
        float lineWidth = (float) (Math.random() * 2.0f) + 1.0f;
        return new Meteor(speedMS, x, y, lineWidth, lineLength);
    }

    public float getAlpha() {
        return this.alpha;
    }

    public float getLineWidth() {
        return this.LineWidth;
    }

    public void setLineWidth(float f) {
        this.LineWidth = f;
    }

    public float getX() {
        return this.pos.x;
    }

    public float getY() {
        return this.pos.y;
    }

    public float getX2() {
        return this.pos2.x;
    }

    public float getY2() {
        return this.pos2.y;
    }

    public void tick() {
        if (timerUtils.passed(speedMS)) {
            timerUtils.reset();
            pos.x = (random.nextInt(Display.getWidth()));
            pos.y = (random.nextInt(Display.getHeight()));
            LineLength = 70 + random.nextInt(300);
            randomColor = new Color(Rainbow.getRainbow(random.nextInt(360), 0.4f, 1.0f));
            fadeUtils.reset();
            alpha = 0;
        }

        double speedMoves = speedMS / 5f;
        if (timerUtils.passed(speedMoves * 3)) {
            pos.x = (float) (pos2.x + (LineLength * fadeUtils.getFade(FadeUtils.FadeMode.FADE_EASE_OUT_QUAD)));
            pos.y = (float) (pos2.y - (LineLength * fadeUtils.getFade(FadeUtils.FadeMode.FADE_EASE_OUT_QUAD)));
        } else if (timerUtils.passed(speedMoves * 2)) {
            fadeUtils.reset();
        } else {
            pos2.x = (float) (pos.x - (LineLength * fadeUtils.getFade(FadeUtils.FadeMode.FADE_EASE_IN_QUAD)));
            pos2.y = (float) (pos.y + (LineLength * fadeUtils.getFade(FadeUtils.FadeMode.FADE_EASE_IN_QUAD)));
        }

        /*

        if (timerUtils.passed(speedMoves * 3)) {
            pos.x = (float) (pos2.x + (LineLength * fadeUtils.getFade(FadeUtils.FadeMode.FADE_OUT)));
            pos.y = (float) (pos2.y - (LineLength * fadeUtils.getFade(FadeUtils.FadeMode.FADE_OUT)));
        } else if (timerUtils.passed(speedMoves * 2)) {
            fadeUtils.reset();
        } else {
            pos2.x = (float) (pos.x - (LineLength * fadeUtils.getFade(FadeUtils.FadeMode.FADE_IN)));
            pos2.y = (float) (pos.y + (LineLength * fadeUtils.getFade(FadeUtils.FadeMode.FADE_IN)));
        }
         */

        if (this.alpha < 255.0f) {
            this.alpha += 15f;
        }
    }
}
