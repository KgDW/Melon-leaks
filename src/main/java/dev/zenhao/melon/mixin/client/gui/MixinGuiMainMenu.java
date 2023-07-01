package dev.zenhao.melon.mixin.client.gui;

import dev.zenhao.melon.gui.alt.GuiAlt;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(value = {GuiMainMenu.class}, priority = 3500)
public abstract class MixinGuiMainMenu extends GuiScreen {
    @Shadow
    public static String MORE_INFO_TEXT;
    @Shadow
    private static Logger LOGGER = LogManager.getLogger();
    @Shadow
    private static Random RANDOM = new Random();
    @Shadow
    private static ResourceLocation SPLASH_TEXTS;
    @Shadow
    private static ResourceLocation MINECRAFT_TITLE_TEXTURES;
    @Shadow
    private static ResourceLocation field_194400_H;
    @Shadow
    private static ResourceLocation[] TITLE_PANORAMA_PATHS;
    @Shadow
    private final Object threadLock = new Object();
    @Shadow
    private float minceraftRoll;
    @Shadow
    private String splashText;
    @Shadow
    private GuiButton buttonResetDemo;
    @Shadow
    private float panoramaTimer;
    @Shadow
    private DynamicTexture viewportTexture;
    @Shadow
    private int openGLWarning2Width;
    @Shadow
    private int openGLWarning1Width;
    @Shadow
    private int openGLWarningX1;
    @Shadow
    private int openGLWarningY1;
    @Shadow
    private int openGLWarningX2;
    @Shadow
    private int openGLWarningY2;
    @Shadow
    private String openGLWarning1;
    @Shadow
    private String openGLWarning2;
    @Shadow
    private String openGLWarningLink;
    @Shadow
    private ResourceLocation backgroundTexture;
    @Shadow
    private GuiButton realmsButton;
    @Shadow
    private boolean hasCheckedForRealmsNotification;
    @Shadow
    private GuiScreen realmsNotification;
    @Shadow
    private int widthCopyright;
    @Shadow
    private int widthCopyrightRest;
    @Shadow
    private GuiButton modButton;
    /*
    private static BufferedImage image;

    static {
        try {
            image = ImageIO.read(Melon.class.getResourceAsStream("/assets/melon/menu/nahida.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
         */

    @Shadow
    private void addDemoButtons(int p_73972_1_, int p_73972_2_) {
    }

    @Shadow
    private void addSingleplayerMultiplayerButtons(int p_73969_1_, int p_73969_2_) {
    }
    /*
    private static boolean first = true;

    @Inject(method = {"initGui"}, at = {@At(value = "RETURN")})
    public void initGui(CallbackInfo ci) {
        if (first) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiStart());
            first = false;
        }
    }

     */

    @Inject(method = "<init>", at = @At("RETURN"))
    public void postConstructor(final CallbackInfo ci) {
        this.splashText = "NIGGER.BLACK";
    }

    @Inject(method = "actionPerformed", at = @At(value = "HEAD"))
    public void actionPerformed(GuiButton button, CallbackInfo info) {
        if (button.id == 114514) {
            this.mc.displayGuiScreen(new GuiAlt(this));
        }
    }

    @Inject(method = {"initGui"}, at = @At("RETURN"))
    public void initGui2(final CallbackInfo info) {
        buttonList.add(new GuiButton(114514, 25, 90, fontRenderer.getStringWidth("AltManager") + 10, 20, "AltManager"));
        //backgroundTexture = this.mc.getTextureManager().getDynamicTextureLocation("background", new DynamicTexture(image));
        //Minecraft.getMinecraft().displayGuiScreen(new StartMenu());
    }

    /**
     * @author zenhao
     * @reason nahida 1st
     */
    /*
    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.panoramaTimer += partialTicks;
        int j = this.width / 2 - 137;
        this.drawGradientRect(0, 0, this.width, this.height, -2130706433, 16777215);
        this.drawGradientRect(0, 0, this.width, this.height, 0, Integer.MIN_VALUE);
        this.mc.getTextureManager().bindTexture(MINECRAFT_TITLE_TEXTURES);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if ((double) this.minceraftRoll < 1.0E-4) {
            this.drawTexturedModalRect(j, 30, 0, 0, 99, 44);
            this.drawTexturedModalRect(j + 99, 30, 129, 0, 27, 44);
            this.drawTexturedModalRect(j + 99 + 26, 30, 126, 0, 3, 44);
            this.drawTexturedModalRect(j + 99 + 26 + 3, 30, 99, 0, 26, 44);
            this.drawTexturedModalRect(j + 155, 30, 0, 45, 155, 44);
        } else {
            this.drawTexturedModalRect(j, 30, 0, 0, 155, 44);
            this.drawTexturedModalRect(j + 155, 30, 0, 45, 155, 44);
        }

        this.mc.getTextureManager().bindTexture(field_194400_H);
        drawModalRectWithCustomSizedTexture(j + 88, 67, 0.0F, 0.0F, 98, 14, 128.0F, 16.0F);
        this.splashText = "NIGGER.BLACK";
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) (this.width / 2 + 90), 70.0F, 0.0F);
        GlStateManager.rotate(-20.0F, 0.0F, 0.0F, 1.0F);
        float f = 1.8F - MathHelper.abs(MathHelper.sin((float) (Minecraft.getSystemTime() % 1000L) / 1000.0F * 6.2831855F) * 0.1F);
        f = f * 100.0F / (float) (this.fontRenderer.getStringWidth(this.splashText) + 32);
        GlStateManager.scale(f, f, f);
        this.drawCenteredString(this.fontRenderer, this.splashText, 0, -8, -256);
        GlStateManager.popMatrix();
        String s = "Minecraft 1.12.2";
        if (this.mc.isDemo()) {
            s = s + " Demo";
        } else {
            s = s + ("release".equalsIgnoreCase(this.mc.getVersionType()) ? "" : "/" + this.mc.getVersionType());
        }

        List<String> brandings = Lists.reverse(FMLCommonHandler.instance().getBrandings(true));

        for (int brdline = 0; brdline < brandings.size(); ++brdline) {
            String brd = brandings.get(brdline);
            if (!Strings.isNullOrEmpty(brd)) {
                this.drawString(this.fontRenderer, brd, 2, this.height - (10 + brdline * (this.fontRenderer.FONT_HEIGHT + 1)), 16777215);
            }
        }

        this.drawString(this.fontRenderer, "Copyright Mojang AB. Do not distribute!", this.widthCopyrightRest, this.height - 10, -1);
        if (mouseX > this.widthCopyrightRest && mouseX < this.widthCopyrightRest + this.widthCopyright && mouseY > this.height - 10 && mouseY < this.height && Mouse.isInsideWindow()) {
            drawRect(this.widthCopyrightRest, this.height - 1, this.widthCopyrightRest + this.widthCopyright, this.height, -1);
        }

        if (this.openGLWarning1 != null && !this.openGLWarning1.isEmpty()) {
            drawRect(this.openGLWarningX1 - 2, this.openGLWarningY1 - 2, this.openGLWarningX2 + 2, this.openGLWarningY2 - 1, 1428160512);
            this.drawString(this.fontRenderer, this.openGLWarning1, this.openGLWarningX1, this.openGLWarningY1, -1);
            this.drawString(this.fontRenderer, this.openGLWarning2, (this.width - this.openGLWarning2Width) / 2, this.buttonList.get(0).y - 12, -1);
        }

        GL11.glPushMatrix();
        GL11.glTranslated((double) this.width / 2.0, (double) this.height / 2.0, 0.0);
        GL11.glScaled(1.2f, 1.2f, 0f);
        GL11.glPopMatrix();
        float xOffset = -1.0f * (((float) mouseX - (float) this.width / 2.0f) / ((float) this.width / 16.0f));
        float yOffset = -1.0f * (((float) mouseY - (float) this.height / 2.0f) / ((float) this.height / 9.0f));
        float width = this.width + 78;
        float height = this.height + 60;
        float x = 20.0f + xOffset - width / 2.0f;
        float y = -18.0f + yOffset - height / 2.0f;
        /*
        MultiThreading.runAsync(() -> {
            if (!setup) {
                OpenGlHelper.initializeTextures();
                ProgressManager.ProgressBar bar = ProgressManager.push("Rendering Setup", 5, true);
                bar.step("GL Setup");
                GlStateManager.enableTexture2D();
                GlStateManager.shadeModel(7425);
                GlStateManager.clearDepth(1.0);
                GlStateManager.enableDepth();
                GlStateManager.depthFunc(515);
                GlStateManager.enableAlpha();
                GlStateManager.alphaFunc(516, 0.1F);
                GlStateManager.cullFace(GlStateManager.CullFace.BACK);
                GlStateManager.matrixMode(5889);
                GlStateManager.loadIdentity();
                GlStateManager.matrixMode(5888);
                setup = true;
            }
            this.mc.getTextureManager().bindTexture(this.backgroundTexture);
            RenderUtils.drawTexture(x, y, width, height, 255);
        });

        //GlStateManager.scale(f, f, f);
        this.mc.getTextureManager().bindTexture(this.backgroundTexture);
        RenderUtils.drawTexture(x, y, width, height, 255);
        //this.drawCenteredString(this.fontRenderer, this.splashText, 0, -28, -256);
        /*
        Media media = new Media(new File("Nahida.mp3").toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
         */
}