package dev.zenhao.melon.mixin;

import dev.zenhao.melon.Melon;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("Melon")
public class MixinLoaderForge implements IFMLLoadingPlugin {

    public MixinLoaderForge() {
        Melon.logger.info("Melon mixins initialized");
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.melon.json");
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
        Melon.logger.info(MixinEnvironment.getDefaultEnvironment().getObfuscationContext());
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
