package dev.zenhao.melon.module.modules.render

import dev.zenhao.melon.manager.EntityManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.utils.extension.sq
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import melon.events.Render3DEvent
import melon.events.TickEvent
import melon.system.event.SafeClientEvent
import melon.system.event.listener
import melon.system.event.safeParallelListener
import melon.system.render.graphic.GlStateUtils
import melon.system.render.graphic.mask.SideMask
import melon.system.util.color.ColorRGB
import melon.utils.entity.EntityUtils.eyePosition
import melon.utils.graphics.esp.*
import melon.utils.math.vector.distanceSqTo
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.item.*
import net.minecraft.item.ItemShulkerBox
import net.minecraft.tileentity.*
import org.lwjgl.opengl.GL11.*
import java.awt.Color

@Module.Info(name = "StorageESP", description = "Draws an ESP on top of storage units", category = Category.RENDER)
internal object StorageESP : Module() {
    private val page = msetting("Page", Page.TYPE)

    /* Type settings */
    private val chest = bsetting("Chest", true).m(page, Page.TYPE)
    private val shulker = bsetting("Shulker", true).m(page, Page.TYPE)
    private val enderChest = bsetting("Ender Chest", true).m(page, Page.TYPE)
    private val frame = bsetting("Item Frame", true).m(page, Page.TYPE)
    private val withShulkerOnly = bsetting("With Shulker Only", true).m(page, Page.TYPE).b(frame)
    private val furnace = bsetting("Furnace", false).m(page, Page.TYPE)
    private val dispenser = bsetting("Dispenser", false).m(page, Page.TYPE)
    private val hopper = bsetting("Hopper", false).m(page, Page.TYPE)
    private val cart = bsetting("Minecart", false).m(page, Page.TYPE)
    private val range = fsetting("Range", 64.0f, 8.0f, 128.0f, 4.0f).m(page, Page.TYPE)

    /* Color settings */
    private val colorChest = csetting("ChestColor", Color(255, 132, 32)).m(page, Page.COLOR)
    private val colorDispenser = csetting("DispenserColor", Color(160, 160, 160)).m(page, Page.COLOR)
    private val colorShulker = csetting("ShulkerColor", Color(220, 64, 220)).m(page, Page.COLOR)
    private val colorEnderChest = csetting("EnderChest Color", Color(137, 50, 184)).m(page, Page.COLOR)
    private val colorFurnace = csetting("FurnaceColor", Color(160, 160, 160)).m(page, Page.COLOR)
    private val colorHopper = csetting("HopperColor", Color(80, 80, 80)).m(page, Page.COLOR)
    private val colorCart = csetting("CartColor", Color(32, 250, 32)).m(page, Page.COLOR)
    private val colorFrame = csetting("FrameColor", Color(255, 132, 32)).m(page, Page.COLOR)

    /* Render settings */
    private val filled = bsetting("Filled", true).m(page, Page.RENDER)
    private val outline = bsetting("Outline", true).m(page, Page.RENDER)
    private val tracer = bsetting("Tracer", true).m(page, Page.RENDER)
    private val filledAlpha = isetting("FilledAlpha", 63, 0,255, 1).m(page, Page.RENDER).b(filled)
    private val outlineAlpha = isetting("OutlineAlpha", 200, 0,255, 1).m(page, Page.RENDER).b(outline)
    private val tracerAlpha = isetting("TracerAlpha", 200, 0,255, 1).m(page, Page.RENDER).b(tracer)
    private val lineWidth = fsetting("LineWidth", 2.0f, 0.25f, 5.0f, 0.25f).m(page, Page.RENDER).or(tracer, outline)

    private enum class Page {
        TYPE, COLOR, RENDER
    }

    override fun getHudInfo(): String {
        return (dynamicBoxRenderer.size + staticBoxRenderer.size).toString()
    }

    private val dynamicBoxRenderer = DynamicBoxRenderer()
    private val staticBoxRenderer = StaticBoxRenderer()
    private val dynamicTracerRenderer = DynamicTracerRenderer()
    private val staticTracerRenderer = StaticTracerRenderer()

    init {
        listener<Render3DEvent> {
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
            GlStateManager.glLineWidth(lineWidth.value)
            GlStateUtils.depth(false)

            val filledAlpha = if (filled.value) filledAlpha.value else 0
            val outlineAlpha = if (outline.value) outlineAlpha.value else 0
            val tracerAlpha = if (tracer.value) tracerAlpha.value else 0

            dynamicBoxRenderer.render(filledAlpha, outlineAlpha)
            staticBoxRenderer.render(filledAlpha, outlineAlpha)
            dynamicTracerRenderer.render(tracerAlpha)
            staticTracerRenderer.render(tracerAlpha)

            GlStateUtils.cull(true)
            GlStateUtils.depth(true)
            GlStateManager.glLineWidth(1.0f)
        }

        safeParallelListener<TickEvent.Post> {
            coroutineScope {
                launch {
                    updateTileEntities()
                }
                launch {
                    updateEntities()
                }
            }
        }
    }

    private fun SafeClientEvent.updateTileEntities() {
        val eyePos = player.eyePosition
        val rangeSq = range.value.sq

        staticBoxRenderer.update {
            staticTracerRenderer.update {
                for (tileEntity in world.loadedTileEntityList.toList()) {
                    if (eyePos.distanceSqTo(tileEntity.pos) > rangeSq) continue
                    if (!checkTileEntityType(tileEntity)) continue

                    val color = getTileEntityColor(tileEntity)
                    if (color.rgba == 0) continue

                    val box = world.getBlockState(tileEntity.pos).getSelectedBoundingBox(world, tileEntity.pos)
                        ?: continue
                    var sideMask = SideMask.ALL

                    if (tileEntity is TileEntityChest) {
                        // Leave only the colliding face and then flip the bits (~) to have ALL but that face
                        if (tileEntity.adjacentChestZNeg != null) sideMask -= SideMask.NORTH
                        if (tileEntity.adjacentChestZPos != null) sideMask -= SideMask.SOUTH
                        if (tileEntity.adjacentChestXNeg != null) sideMask -= SideMask.WEST
                        if (tileEntity.adjacentChestXPos != null) sideMask -= SideMask.EAST
                    }

                    putBox(box, color, sideMask, sideMask.toOutlineMaskInv())
                    putTracer(box, color)
                }
            }
        }
    }

    private fun checkTileEntityType(tileEntity: TileEntity): Boolean {
        return chest.value && tileEntity is TileEntityChest
                || dispenser.value && tileEntity is TileEntityDispenser
                || shulker.value && tileEntity is TileEntityShulkerBox
                || enderChest.value && tileEntity is TileEntityEnderChest
                || furnace.value && tileEntity is TileEntityFurnace
                || hopper.value && tileEntity is TileEntityHopper
    }

    private fun getTileEntityColor(tileEntity: TileEntity): ColorRGB {
        return when (tileEntity) {
            is TileEntityChest -> ColorRGB(colorChest.value)
            is TileEntityDispenser -> ColorRGB(colorDispenser.value)
            is TileEntityShulkerBox -> ColorRGB(colorShulker.value)
            is TileEntityEnderChest -> ColorRGB(colorEnderChest.value)
            is TileEntityFurnace -> ColorRGB(colorFurnace.value)
            is TileEntityHopper -> ColorRGB(colorHopper.value)
            else -> ColorRGB(0, 0, 0, 0)
        }
    }

    private fun SafeClientEvent.updateEntities() {
        val eyePos = player.eyePosition
        val rangeSq = range.value.sq

        dynamicBoxRenderer.update {
            dynamicTracerRenderer.update {
                for (entity in EntityManager.entity) {
                    if (entity.distanceSqTo(eyePos) > rangeSq) continue
                    if (!checkEntityType(entity)) continue

                    val box = entity.entityBoundingBox ?: continue
                    val color = getEntityColor(entity)
                    if (color.rgba == 0) continue

                    val xOffset = entity.posX - entity.lastTickPosX
                    val yOffset = entity.posY - entity.lastTickPosY
                    val zOffset = entity.posZ - entity.lastTickPosZ

                    putBox(box, xOffset, yOffset, zOffset, color)
                    putTracer(entity.posX, entity.posY, entity.posZ, xOffset, yOffset, zOffset, color)
                }
            }
        }
    }

    private fun checkEntityType(entity: Entity): Boolean {
        return frame.value && entity is EntityItemFrame && (!withShulkerOnly.value || entity.displayedItem.item is ItemShulkerBox)
                || cart.value && (entity is EntityMinecartChest || entity is EntityMinecartHopper || entity is EntityMinecartFurnace)
    }

    private fun getEntityColor(entity: Entity): ColorRGB {
        return when (entity) {
            is EntityMinecartContainer -> ColorRGB(colorCart.value)
            is EntityItemFrame -> ColorRGB(colorFrame.value)
            else -> ColorRGB(0, 0, 0, 0)
        }
    }
}
