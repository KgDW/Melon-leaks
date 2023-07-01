package dev.zenhao.melon.utils.BackgroundEffect.particle.utils;

import java.awt.*;

public class ParticleUtil {
    public static Color rainbow(float speed, float off) {
        double time = (double)System.currentTimeMillis() / (double)speed;
        time += off;
        return Color.getHSBColor((float)(time % 255.0 / 255.0), 1.0f, 1.0f);
    }
}

