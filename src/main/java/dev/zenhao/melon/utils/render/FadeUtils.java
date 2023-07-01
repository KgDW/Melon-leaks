package dev.zenhao.melon.utils.render;

public class FadeUtils {

    protected long start;
    protected long length;

    public FadeUtils(long ms) {
        length = ms;
        reset();
    }

    public void reset() {
        this.start = System.currentTimeMillis();
    }

    public boolean isEnd() {
        return this.getTime() >= this.length;
    }

    public FadeUtils end(){
        this.start = System.currentTimeMillis() - this.length;
        return this;
    }

    protected long getTime() {
        return System.currentTimeMillis() - this.start;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getLength() {
        return this.length;
    }

    private double getFadeOne() {
        return isEnd() ? 1.0 : (double)getTime() / this.length;
    }

    public double getFade(FadeMode fadeMode){
        return getFade(fadeMode, getFadeOne());
    }

    public static double getFade(FadeMode fadeMode, double current){
        switch (fadeMode) {
            case FADE_IN:
                return getFadeInDefault(current);
            case FADE_OUT:
                return getFadeOutDefault(current);
            case FADE_EPS_IN:
                return getEpsEzFadeIn(current);
            case FADE_EPS_OUT:
                return getEpsEzFadeOut(current);
            case FADE_EASE_IN_QUAD:
                return easeInQuad(current);
            case FADE_EASE_OUT_QUAD:
                return easeOutQuad(current);
            default:
                return current;
        }
    }

    public static double getFadeType(FadeType fadeType, boolean FadeIn, double current){
        switch (fadeType) {
            case FADE_DEFAULT:
                return FadeIn ? getFadeInDefault(current) : getFadeOutDefault(current);
            case FADE_EPS:
                return FadeIn ? getEpsEzFadeIn(current) : getEpsEzFadeOut(current);
            case FADE_EASE_QUAD:
                return FadeIn ? easeInQuad(current) : easeOutQuad(current);
            default:
                return FadeIn ? current : 1.0 - current;
        }
    }

    public enum FadeType {
        FADE_DEFAULT,
        FADE_ONE,
        FADE_EPS,
        FADE_EASE_QUAD,
    }

    public enum FadeMode {
        FADE_IN,
        FADE_OUT,
        FADE_ONE,
        FADE_EPS_IN,
        FADE_EPS_OUT,
        FADE_EASE_OUT_QUAD,
        FADE_EASE_IN_QUAD
    }

    private static double checkOne(double one){
        return Math.max(0.0, Math.min(1.0, one));
    }

    public static double getFadeInDefault(double current) {
        return Math.tanh(checkOne(current) * 3.0);
    }

    public static double getFadeOutDefault(double current) {
        return 1.0 - getFadeInDefault(current);
    }

    public static double getEpsEzFadeIn(double current) {
        return 1.0 - getEpsEzFadeOut(current);
    }

    public static double getEpsEzFadeOut(double current) {
        return Math.cos(0.5 * Math.PI * checkOne(current)) * Math.cos(0.8 * Math.PI * checkOne(current));
    }

    public static double easeOutQuad(double current) {
        return 1 - easeInQuad(current);
    }

    public static double easeInQuad(double current) {
        return checkOne(current) * checkOne(current);
    }
}