package dev.zenhao.melon.utils.BackgroundEffect.FakeMeteor;

import dev.zenhao.melon.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class MeteorSystem {
    public CopyOnWriteArrayList<Meteor> meteorList = new CopyOnWriteArrayList<>();
    private boolean rainbow;

    public MeteorSystem(int initAmount, boolean rainbow) {
        this.addParticles(initAmount);
        this.rainbow = rainbow;
    }

    public MeteorSystem(int initAmount) {
        this(initAmount, false);
    }

    public void addParticles(int amount) {
        for (int i = 0; i < amount; ++i) {
            this.meteorList.add(Meteor.generateMeteor());
        }
    }

    public void tick() {
        for (Meteor meteor : this.meteorList) {
            meteor.tick();
        }
    }

    public void setRainbow(boolean rainbow) {
        this.rainbow = rainbow;
    }

    public void render() {
        if (Minecraft.getMinecraft().currentScreen == null) {
            return;
        }
        meteorList.forEach(meteor -> {
            if (meteor != null) {
                Color color = rainbow ? meteor.randomColor : Color.WHITE;
                RenderUtils.drawLine(meteor.getX(), meteor.getY(), meteor.getX2(), meteor.getY2(), meteor.getLineWidth(), new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) meteor.getAlpha()));
            }
        });
    }
}
