package melon.system.util.accessor

import dev.zenhao.melon.mixin.client.accessor.render.*
import net.minecraft.client.renderer.DestroyBlockProgress
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.client.shader.Framebuffer
import net.minecraft.client.shader.Shader
import net.minecraft.client.shader.ShaderGroup

val DestroyBlockProgress.entityID: Int
    get() = (this as AccessorDestroyBlockProgress).MGetEntityID()

val RenderGlobal.entityOutlineShader: ShaderGroup
    get() = (this as AccessorRenderGlobal).entityOutlineShader
val RenderGlobal.damagedBlocks: MutableMap<Int, DestroyBlockProgress>
    get() = (this as AccessorRenderGlobal).MGetDamagedBlocks()
var RenderGlobal.renderEntitiesStartupCounter: Int
    get() = (this as AccessorRenderGlobal).MGetRenderEntitiesStartupCounter()
    set(value) {
        (this as AccessorRenderGlobal).MSetRenderEntitiesStartupCounter(value)
    }
var RenderGlobal.countEntitiesTotal: Int
    get() = (this as AccessorRenderGlobal).MGetCountEntitiesTotal()
    set(value) {
        (this as AccessorRenderGlobal).MSetCountEntitiesTotal(value)
    }
var RenderGlobal.countEntitiesRendered: Int
    get() = (this as AccessorRenderGlobal).MGetCountEntitiesRendered()
    set(value) {
        (this as AccessorRenderGlobal).MSetCountEntitiesRendered(value)
    }
var RenderGlobal.countEntitiesHidden: Int
    get() = (this as AccessorRenderGlobal).MGetCountEntitiesHidden()
    set(value) {
        (this as AccessorRenderGlobal).MSetCountEntitiesHidden(value)
    }

val RenderManager.renderPosX: Double
    get() = (this as AccessorRenderManager).renderPosX
val RenderManager.renderPosY: Double
    get() = (this as AccessorRenderManager).renderPosY
val RenderManager.renderPosZ: Double
    get() = (this as AccessorRenderManager).renderPosZ
val RenderManager.renderOutlines: Boolean
    get() = (this as AccessorRenderManager).renderOutlines

val ShaderGroup.listShaders: List<Shader>
    get() = (this as AccessorShaderGroup).listShaders
val ShaderGroup.listFrameBuffers: List<Framebuffer>
    get() = (this as AccessorShaderGroup).listFramebuffers
