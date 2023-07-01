package dev.zenhao.melon.module.modules.combat

import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.event.events.render.RenderEvent
import dev.zenhao.melon.event.events.render.item.RenderItemAnimationEvent
import dev.zenhao.melon.manager.FriendManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.block.BlockInteractionHelper
import dev.zenhao.melon.utils.entity.EntityUtil
import dev.zenhao.melon.utils.gl.MelonTessellator
import dev.zenhao.melon.utils.inventory.ItemUtil
import dev.zenhao.melon.utils.math.RandomUtil
import dev.zenhao.melon.utils.math.deneb.LagCompensator
import dev.zenhao.melon.utils.render.RenderUtils3D
import melon.system.event.safeEventListener
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.passive.AbstractChestHorse
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityFireball
import net.minecraft.entity.projectile.EntityShulkerBullet
import net.minecraft.init.Items
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.EnumHandSide
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.Cylinder
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

@Module.Info(name = "KillAura", category = Category.COMBAT)
class KillAura : Module() {
    var page = msetting("Page", Page.ONE)
    var Mode = msetting("Mode", Modes.Closest)
    var render = msetting("RenderMode", Render.Novo)
    var attackMode = msetting("AttackMode", AttackMode.Tick)
    var Distance: Setting<Float> = fsetting("Range", 5.5f, 0f, 8f).m(page, Page.ONE)
    var TPSSync: Setting<Boolean> = bsetting("TpsSync", true).m(page, Page.ONE)
    var Players: Setting<Boolean> = bsetting("Players", true).m(page, Page.ONE)
    var Monsters: Setting<Boolean> = bsetting("Monsters", true).m(page, Page.ONE)
    var Neutrals: Setting<Boolean> = bsetting("Neutrals", true).m(page, Page.ONE)
    var Animals: Setting<Boolean> = bsetting("Animals", true).m(page, Page.ONE)
    var Tamed: Setting<Boolean> = bsetting("Tamed", false).m(page, Page.ONE)
    var Projectiles: Setting<Boolean> = bsetting("Projectiles", false).m(page, Page.ONE)
    var SwordOnly: Setting<Boolean> = bsetting("SwordOnly", false).m(page, Page.ONE)
    var PauseIfCrystal: Setting<Boolean> = bsetting("PauseIfCA", true).m(page, Page.ONE)
    var PauseIfEating: Setting<Boolean> = bsetting("PauseIfEating", false).m(page, Page.ONE)
    var AutoSwitch: Setting<Boolean> = bsetting("AutoSwitch", true).m(page, Page.ONE)
    var Only32k: Setting<Boolean> = bsetting("Only32K", false).m(page, Page.ONE)

    //Page Two
    var minCPS = isetting("MinCPS", 8, 0, 150).m(page, Page.TWO)
    var maxCPS = isetting("MaxCPS", 14, 0, 300).m(page, Page.TWO)
    var rotate = bsetting("Rotate", true).m(page, Page.TWO)
    var Hyp: Setting<Boolean> = bsetting("HypCheck", false).m(page, Page.TWO)
    var Team: Setting<Boolean> = bsetting("Team", false).m(page, Page.TWO)
    var hurtTimeCheck = bsetting("HurtTimeCheck", false).m(page, Page.TWO)
    var autoblock: Setting<Boolean> = bsetting("AutoBlock", false).m(page, Page.TWO)
    var animation: Setting<Boolean> = bsetting("Animation", false).m(page, Page.TWO)
    var RenderTarget: Setting<Boolean> = bsetting("Render", false).m(page, Page.TWO)
    var attackTimerUtils = TimerUtils()
    var updateTimerUtils = TimerUtils()
    var CurrentTarget: Entity? = null
    var canRender = false
    var step = false
    var cps = 0
    var delay = 0
    var b = 0
    fun ShouldAttack(): Boolean {
        val az = RandomUtil.nextInt(minCPS.value, maxCPS.value)
        cps = az
        return attackTimerUtils.passed(az) && attackMode.value == AttackMode.Cps
    }

    fun drawCircle(entity: Entity, partialTicks: Float, rad: Double, height: Float) {
        MelonTessellator.prepare(GL11.GL_QUADS)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glDepthMask(false)
        GL11.glLineWidth(2.0f)
        GL11.glBegin(GL11.GL_LINE_LOOP)
        val x =
            entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX
        val y =
            entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY
        val z =
            entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ
        val pix2 = Math.PI * 2.0
        GlStateManager.color(224 / 255f, 63 / 255f, 216 / 255f, 255 / 255f)
        for (i in 0..180) {
            GL11.glVertex3d(
                x + rad * cos(i * pix2 / 45),
                y + height * (i / 180f),
                z + rad * sin(i * pix2 / 45)
            )
        }
        GL11.glEnd()
        GL11.glDepthMask(true)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        MelonTessellator.release()
    }

    override fun onEnable() {
        if (fullNullCheck()) {
            return
        }
        canRender = false
        cps = 0
    }

    override fun onDisable() {
        if (fullNullCheck()) {
            return
        }
        canRender = false
        cps = 0
    }

    override fun getHudInfo(): String {
        return "CPS: $cps"
    }

    fun IsValidTarget(p_Entity: Entity): Boolean {
        if (p_Entity is EntityArmorStand) {
            return false
        }
        if (p_Entity !is EntityLivingBase) {
            val l_IsProjectile = p_Entity is EntityShulkerBullet || p_Entity is EntityFireball
            if (!l_IsProjectile) return false
            if (!Projectiles.value) return false
        }
        if (p_Entity is EntityPlayer) {
            /// Ignore if it's us
            if (p_Entity === mc.player) return false
            if (!Players.value) return false

            /// They are a friend, ignore it.
            if (FriendManager.isFriend(p_Entity.getName())) return false
            if (Team.value) {
                if (isTeam(mc.player, p_Entity)) {
                    return false
                }
            }
        }
        if (EntityUtil.isHostileMob(p_Entity) && !Monsters.value) {
            return false
        }
        if (EntityUtil.isPassive(p_Entity)) {
            if (p_Entity is AbstractChestHorse) {
                if (p_Entity.isTame && !Tamed.value) {
                    return false
                }
            }
            if (!Animals.value) {
                return false
            }
        }
        if (EntityUtil.isHostileMob(p_Entity) && !Monsters.value) {
            return false
        }
        if (EntityUtil.isNeutralMob(p_Entity) && !Neutrals.value) {
            return false
        }
        var l_HealthCheck = true
        if (p_Entity is EntityLivingBase) {
            val l_Base = p_Entity
            l_HealthCheck = !l_Base.isDead && l_Base.health > 0.0f
        }
        return l_HealthCheck && p_Entity.getDistance(p_Entity) <= Distance.value
    }

    init {
        safeEventListener<PlayerMotionEvent> { event ->
            if (fullNullCheck()) {
                return@safeEventListener
            }
            if (updateTimerUtils.passed(1000)) {
                cps = 0
                updateTimerUtils.reset()
            }
            var oao = 0
            while (oao < 360) {
                oao = ++b
                oao++
            }
            if (mc.player.heldItemMainhand.getItem() !is ItemSword) {
                if (mc.player.heldItemMainhand.getItem() === Items.END_CRYSTAL && PauseIfCrystal.value) return@safeEventListener
                if (mc.player.heldItemMainhand.getItem() === Items.GOLDEN_APPLE && PauseIfEating.value) return@safeEventListener
            }
            if (Only32k.value) {
                if (!ItemUtil.Is32k(mc.player.heldItemMainhand)) return@safeEventListener
            }
            if (Mode.value === Modes.Closest) {
                CurrentTarget = mc.world.loadedEntityList.stream()
                    .filter { p_Entity: Entity -> IsValidTarget(p_Entity) }
                    .min(Comparator.comparing { p_Entity: Entity? -> mc.player.getDistance(p_Entity!!) })
                    .orElse(null)
            }
            if (Mode.value === Modes.Priority) {
                if (CurrentTarget == null) {
                    CurrentTarget = mc.world.loadedEntityList.stream()
                        .filter { p_Entity: Entity -> IsValidTarget(p_Entity) }
                        .min(Comparator.comparing { p_Entity: Entity? -> mc.player.getDistance(p_Entity!!) })
                        .orElse(null)
                }
            }
            if (Mode.value === Modes.Switch) {
                CurrentTarget = mc.world.loadedEntityList.stream()
                    .filter { p_Entity: Entity -> IsValidTarget(p_Entity) }
                    .min(Comparator.comparing { p_Entity: Entity? -> mc.player.getDistance(p_Entity!!) })
                    .orElse(null)
            }
            if (CurrentTarget == null || CurrentTarget!!.getDistance(mc.player) > Distance.value) {
                CurrentTarget = null
                cps = 0
                canRender = false
                return@safeEventListener
            }
            if (CurrentTarget != null) {
                if (AutoSwitch.value) {
                    for (l_I in 0..8) {
                        if (mc.player.inventory.getStackInSlot(l_I).getItem() is ItemSword) {
                            mc.player.inventory.currentItem = l_I
                            mc.playerController.updateController()
                            break
                        }
                    }
                }
                if (SwordOnly.value && mc.player.heldItemMainhand.getItem() !is ItemSword) {
                    return@safeEventListener
                }
                if (rotate.value) {
                    event.setRotation(
                        BlockInteractionHelper.getLegitRotations(
                            Vec3d(
                                CurrentTarget!!.posX,
                                CurrentTarget!!.posY,
                                CurrentTarget!!.posZ
                            )
                        )[0], BlockInteractionHelper.getLegitRotations(
                            Vec3d(
                                CurrentTarget!!.posX,
                                CurrentTarget!!.posY,
                                CurrentTarget!!.posZ
                            )
                        )[1]
                    )
                }
                val l_Ticks = 20.0f - LagCompensator.INSTANCE.tickRate
                val l_IsAttackReady = mc.player.getCooledAttackStrength(if (TPSSync.value) -l_Ticks else 0.0f) >= 1
                if (attackMode.value == AttackMode.Tick) {
                    if (!l_IsAttackReady) {
                        cps = 0
                        return@safeEventListener
                    }
                }
                if (mc.connection != null) {
                    if (Hyp.value) {
                        if (!mc.player.canEntityBeSeen(CurrentTarget!!)) {
                            CurrentTarget = null
                            canRender = false
                            return@safeEventListener
                        }
                    }
                    if (hurtTimeCheck.value) {
                        if ((CurrentTarget as EntityLivingBase).hurtTime >= 0) {
                            return@safeEventListener
                        }
                    }
                    if (attackMode.value == AttackMode.Cps) {
                        if (ShouldAttack()) {
                            attack(CurrentTarget)
                            attackTimerUtils.reset()
                        }
                    } else if (attackMode.value == AttackMode.Tick) {
                        attack(CurrentTarget)
                        ++cps
                    }
                    if (!animation.value) {
                        mc.player.swingArm(EnumHand.MAIN_HAND)
                    } else {
                        mc.player.connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
                    }
                }
                mc.player.resetCooldown()
            }
        }
    }

    fun attack(e: Entity?) {
        canRender = true
        if (autoblock.value) {
            mc.player.connection.sendPacket(
                CPacketPlayerDigging(
                    CPacketPlayerDigging.Action.RELEASE_USE_ITEM,
                    BlockPos.ORIGIN,
                    EnumFacing.DOWN
                )
            )
        }
        mc.player.connection.sendPacket(CPacketUseEntity(e!!))
    }

    @SubscribeEvent
    fun onTransformItem(event: RenderItemAnimationEvent.Transform) {
        if (fullNullCheck() || CurrentTarget == null || !canRender) return
        if (event.hand == EnumHand.MAIN_HAND && CurrentTarget != null && animation.value) {
            val i: Float = if (mc.player.primaryHand == EnumHandSide.RIGHT) {
                1f
            } else {
                -1f
            }
            GlStateManager.translate(0.15f * i, 0.3f, 0.0f)
            GlStateManager.rotate(5f * i, 0.0f, 0.0f, 0.0f)
            if (i > 0f) GlStateManager.translate(0.56f, -0.52f, -0.72f * i) else GlStateManager.translate(
                0.56f,
                -0.52f,
                0.5f
            )
            GlStateManager.translate(0.0f, 0.2f * 0.6f, 0.0f)
            GlStateManager.rotate(b.inc().toFloat(), b / 2f, b / i * 2f, b / 2f)
            GlStateManager.scale(1.625f, 1.625f, 1.625f)
        }
    }

    override fun onWorldRender(event: RenderEvent) {
        if (CurrentTarget != null && canRender && RenderTarget.value) {
            if (delay > 200) {
                step = false
            }
            if (delay < 0) {
                step = true
            }
            if (step) {
                delay += 3
            } else {
                delay -= 3
            }
            val x = (CurrentTarget!!.lastTickPosX
                    + (CurrentTarget!!.posX - CurrentTarget!!.lastTickPosX) * mc.timer.renderPartialTicks
                    - mc.renderManager.renderPosX)
            val y = (CurrentTarget!!.lastTickPosY
                    + (CurrentTarget!!.posY - CurrentTarget!!.lastTickPosY) * mc.timer.renderPartialTicks
                    - mc.renderManager.renderPosY)
            val z = (CurrentTarget!!.lastTickPosZ
                    + (CurrentTarget!!.posZ - CurrentTarget!!.lastTickPosZ) * mc.timer.renderPartialTicks
                    - mc.renderManager.renderPosZ)
            if (render.value == Render.Novo) {
                if (CurrentTarget is EntityPlayer) {
                    val width = (CurrentTarget!!.entityBoundingBox.maxX
                            - CurrentTarget!!.entityBoundingBox.minX)
                    val height = CurrentTarget!!.entityBoundingBox.maxY
                    -CurrentTarget!!.entityBoundingBox.minY + 0.25
                    val red = if ((CurrentTarget as EntityPlayer).hurtTime > 0) 1.0f else 0.0f
                    val green = if ((CurrentTarget as EntityPlayer).hurtTime > 0) 0.2f else 0.5f
                    val blue = if ((CurrentTarget as EntityPlayer).hurtTime > 0) 0.0f else 1.0f
                    val alpha = 0.2f
                    val lineRed = if ((CurrentTarget as EntityPlayer).hurtTime > 0) 1.0f else 0.0f
                    val lineGreen = if ((CurrentTarget as EntityPlayer).hurtTime > 0) 0.2f else 0.5f
                    val lineBlue = if ((CurrentTarget as EntityPlayer).hurtTime > 0) 0.0f else 1.0f
                    val lineAlpha = 1.0f
                    val lineWdith = 2.0f
                    drawEntityESP(
                        x, y, z, width, height, red, green, blue, alpha, lineRed, lineGreen, lineBlue,
                        lineAlpha, lineWdith
                    )
                } else {
                    val width = (CurrentTarget!!.entityBoundingBox.maxZ
                            - CurrentTarget!!.entityBoundingBox.minZ)
                    val height = 0.1
                    val red = 0.0f
                    val green = 0.5f
                    val blue = 1.0f
                    val alpha = 0.5f
                    val lineRed = 0.0f
                    val lineGreen = 0.5f
                    val lineBlue = 1.0f
                    val lineAlpha = 1.0f
                    val lineWdith = 2.0f
                    drawEntityESP(
                        x, y + CurrentTarget!!.eyeHeight + 0.25, z, width, height, red, green,
                        blue, alpha, lineRed, lineGreen, lineBlue, lineAlpha, lineWdith
                    )
                }
            }
            if (render.value == Render.Circle) {
                drawCircle(CurrentTarget!!, mc.renderPartialTicks, 0.8, delay / 100f)
            }
            if (render.value == Render.New) {
                val entity = CurrentTarget as EntityLivingBase
                drawESP(entity, if (entity.hurtTime >= 1) Color(255, 0, 0, 160).rgb else Color(47, 116, 253, 255).rgb)
            }
        }
    }

    enum class AttackMode {
        Tick, Cps
    }

    enum class Page {
        ONE, TWO
    }

    enum class Modes {
        Closest, Priority, Switch
    }

    enum class Render {
        Circle, Novo, New, Off
    }

    companion object {
        var INSTANCE = KillAura()
        fun drawEntityESP(
            x: Double,
            y: Double,
            z: Double,
            width: Double,
            height: Double,
            red: Float,
            green: Float,
            blue: Float,
            alpha: Float,
            lineRed: Float,
            lineGreen: Float,
            lineBlue: Float,
            lineAlpha: Float,
            lineWdith: Float
        ) {
            MelonTessellator.prepare(GL11.GL_QUADS)
            GL11.glLineWidth(width.toFloat())
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GlStateManager.color(red / 255f, green / 255f, blue / 255f, alpha / 255f)
            RenderUtils3D.drawBoundingBox(AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width))
            GL11.glLineWidth(lineWdith)
            GL11.glColor4f(lineRed, lineGreen, lineBlue, lineAlpha)
            RenderUtils3D.drawOutlinedBoundingBox(
                AxisAlignedBB(
                    x - width,
                    y,
                    z - width,
                    x + width,
                    y + height,
                    z + width
                )
            )
            GlStateManager.color(red / 255f, green / 255f, blue / 255f, alpha / 255f)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            MelonTessellator.release()
        }

        fun isTeam(e: EntityPlayer, e2: EntityPlayer): Boolean {
            return if (e2.team != null && e.team != null) {
                val target = e2.displayName.formattedText[1]
                val player = e.displayName.formattedText[1]
                target == player
            } else {
                true
            }
        }

        fun glColor(hex: Int) {
            val alpha = (hex shr 24 and 255).toFloat() / 255.0f
            val red = (hex shr 16 and 255).toFloat() / 255.0f
            val green = (hex shr 8 and 255).toFloat() / 255.0f
            val blue = (hex and 255).toFloat() / 255.0f
            GL11.glColor4f(red, green, blue, if (alpha == 0.0f) 1.0f else alpha)
        }

        fun enableSmoothLine(width: Float) {
            GL11.glDisable(3008)
            GL11.glEnable(3042)
            GL11.glBlendFunc(770, 771)
            GL11.glDisable(3553)
            GL11.glDisable(2929)
            GL11.glDepthMask(false)
            GL11.glEnable(2884)
            GL11.glEnable(2848)
            GL11.glHint(3154, 4354)
            GL11.glHint(3155, 4354)
            GL11.glLineWidth(width)
        }

        fun disableSmoothLine() {
            GL11.glEnable(3553)
            GL11.glEnable(2929)
            GL11.glDisable(3042)
            GL11.glEnable(3008)
            GL11.glDepthMask(true)
            GL11.glCullFace(1029)
            GL11.glDisable(2848)
            GL11.glHint(3154, 4352)
            GL11.glHint(3155, 4352)
        }

        fun drawESP(entity: EntityLivingBase, color: Int) {
            val x = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.renderPartialTicks.toDouble()
                    - mc.getRenderManager().renderPosX)
            val y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.renderPartialTicks.toDouble()
                    - mc.getRenderManager().renderPosY)
            val z = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.renderPartialTicks.toDouble()
                    - mc.getRenderManager().renderPosZ)
            val radius = 0.2f
            val side = 6
            GL11.glPushMatrix()
            GL11.glTranslated(x, y + 2, z)
            GL11.glRotatef(-entity.width, 0.0f, 1.0f, 0.0f)
            glColor(
                Color(
                    Math.max(Color(color).red - 75, 0), Math.max(Color(color).green - 75, 0),
                    Math.max(Color(color).blue - 75, 0), Color(color).alpha
                ).rgb
            )
            enableSmoothLine(1.0f)
            val c = Cylinder()
            GL11.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f)
            c.drawStyle = 100012
            c.draw(0f, radius, 0.3f, side, 1)
            glColor(color)
            c.drawStyle = 100012
            GL11.glTranslated(0.0, 0.0, 0.3)
            c.draw(radius, 0f, 0.3f, side, 1)
            GL11.glRotatef(90.0f, 0.0f, 0.0f, 1.0f)
            disableSmoothLine()
            GL11.glPopMatrix()
        }
    }
}