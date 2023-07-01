package dev.zenhao.melon.mixin.client.accessor.render;

import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.shader.ShaderGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(RenderGlobal.class)
public interface AccessorRenderGlobal {
    @Accessor("entityOutlineShader")
    ShaderGroup getEntityOutlineShader();

    @Accessor("damagedBlocks")
    Map<Integer, DestroyBlockProgress> MGetDamagedBlocks();

    @Accessor("renderEntitiesStartupCounter")
    int MGetRenderEntitiesStartupCounter();

    @Accessor("renderEntitiesStartupCounter")
    void MSetRenderEntitiesStartupCounter(int value);

    @Accessor("countEntitiesTotal")
    int MGetCountEntitiesTotal();

    @Accessor("countEntitiesTotal")
    void MSetCountEntitiesTotal(int value);

    @Accessor("countEntitiesRendered")
    int MGetCountEntitiesRendered();

    @Accessor("countEntitiesRendered")
    void MSetCountEntitiesRendered(int value);

    @Accessor("countEntitiesHidden")
    int MGetCountEntitiesHidden();

    @Accessor("countEntitiesHidden")
    void MSetCountEntitiesHidden(int value);
}
