package dev.zenhao.melon.mixin.client.entity;

import dev.zenhao.melon.event.events.entity.EventPlayerUpdate;
import dev.zenhao.melon.event.events.entity.PushEvent;
import dev.zenhao.melon.event.events.player.JumpEvent;
import dev.zenhao.melon.event.events.player.PlayerMotionEvent;
import dev.zenhao.melon.manager.EventAccessManager;
import dev.zenhao.melon.mixin.client.MixinAbstractClientPlayer;
import melon.events.PlayerMoveEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.MoverType;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created by 086 on 12/12/2017.
 */
@Mixin(value = EntityPlayerSP.class, priority = Integer.MAX_VALUE)
public abstract class MixinEntityPlayerSP extends MixinAbstractClientPlayer {
    @Shadow
    @Final
    public NetHandlerPlayClient connection;
    @Shadow
    public boolean serverSprintState;
    @Shadow
    public boolean serverSneakState;
    @Shadow
    public double lastReportedPosX;
    @Shadow
    public double lastReportedPosY;
    @Shadow
    public double lastReportedPosZ;
    @Shadow
    public float lastReportedYaw;
    @Shadow
    public float lastReportedPitch;
    @Shadow
    public int positionUpdateTicks;
    @Shadow
    public boolean autoJumpEnabled = true;
    @Shadow
    public boolean prevOnGround;
    @Shadow
    public MovementInput movementInput;
    @Shadow
    public Minecraft mc;
    public PlayerMotionEvent motionEvent;

    @Shadow
    public abstract void updateAutoJump(float p_189810_1_, float p_189810_2_);

    @Shadow
    public boolean isCurrentViewEntity() {
        return false;
    }

    @Redirect(method = {"move"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;move(Lnet/minecraft/entity/MoverType;DDD)V"))
    public void move(AbstractClientPlayer player, MoverType moverType, double x, double y, double z) {
        PlayerMoveEvent event2 = new PlayerMoveEvent(moverType, x, y, z);
        event2.post();
        if (!event2.getCancelled()) {
            super.move(event2.getType(), event2.getX(), event2.getY(), event2.getZ());
        }
    }

    @Inject(method = {"pushOutOfBlocks"}, at = @At("HEAD"), cancellable = true)
    public void pushOutOfBlocksHook(double x, double y, double z, CallbackInfoReturnable<Boolean> info) {
        PushEvent event = new PushEvent(1);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At(value = "HEAD"), cancellable = true)
    private void onUpdateWalkingPlayer_Head(CallbackInfo callbackInfo) {
        motionEvent = new PlayerMotionEvent(0, this.posX, this.getEntityBoundingBox().minY, this.posZ, this.rotationYaw, this.rotationPitch, this.onGround);
        motionEvent.post();
        EventAccessManager.INSTANCE.setData(motionEvent);
        if (motionEvent.getCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Redirect(method = "onUpdateWalkingPlayer", at = @At(value = "FIELD", target = "net/minecraft/client/entity/EntityPlayerSP.posX:D"))
    private double posXHook(EntityPlayerSP entityPlayerSP) {
        return motionEvent.getX();
    }

    @Redirect(method = "onUpdateWalkingPlayer", at = @At(value = "FIELD", target = "net/minecraft/util/math/AxisAlignedBB.minY:D"))
    private double minYHook(AxisAlignedBB axisAlignedBB) {
        return motionEvent.getY();
    }

    @Redirect(method = "onUpdateWalkingPlayer", at = @At(value = "FIELD", target = "net/minecraft/client/entity/EntityPlayerSP.posZ:D"))
    private double posZHook(EntityPlayerSP entityPlayerSP) {
        return motionEvent.getZ();
    }

    @Redirect(method = "onUpdateWalkingPlayer", at = @At(value = "FIELD", target = "net/minecraft/client/entity/EntityPlayerSP.rotationYaw:F"))
    private float rotationYawHook(EntityPlayerSP entityPlayerSP) {
        return motionEvent.getYaw();
    }

    @Redirect(method = "onUpdateWalkingPlayer", at = @At(value = "FIELD", target = "net/minecraft/client/entity/EntityPlayerSP.rotationPitch:F"))
    private float rotationPitchHook(EntityPlayerSP entityPlayerSP) {
        return motionEvent.getPitch();
    }

    @Redirect(method = "onUpdateWalkingPlayer", at = @At(value = "FIELD", target = "net/minecraft/client/entity/EntityPlayerSP.onGround:Z"))
    private boolean onGroundHook(EntityPlayerSP entityPlayerSP) {
        return motionEvent.isOnGround();
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At(value = "RETURN"))
    private void onUpdateWalkingPlayer_Return(CallbackInfo callbackInfo) {
        PlayerMotionEvent oldEvent = new PlayerMotionEvent(1, motionEvent);
        oldEvent.post();
        EventAccessManager.INSTANCE.setData(oldEvent);
    }

    /*
    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    private void onUpdateWalkingPlayerHead(CallbackInfo ci) {
        // Setup flags
        Vec3d position = new Vec3d(this.posX, this.getEntityBoundingBox().minY, this.posZ);
        float rotationX = this.rotationYaw;
        float rotationY = this.rotationPitch;
        boolean onGround = this.onGround;

        OnUpdateWalkingPlayerEvent.Pre eventPre = new OnUpdateWalkingPlayerEvent.Pre(position, rotationX, rotationY, onGround);
        MinecraftForge.EVENT_BUS.post(eventPre);
        PlayerPacketManager.Companion.getINSTANCE().applyPacket(eventPre);

        if (eventPre.isCanceled()) {
            ci.cancel();

            if (!eventPre.getCancelAll()) {
                // Copy flags from event
                position = eventPre.getPosition();
                rotationX = eventPre.getRotationX();
                rotationY = eventPre.getRotationY();
                onGround = eventPre.isOnGround();

                boolean moving = !eventPre.getCancelMove() && isMoving(position);
                boolean rotating = !eventPre.getCancelRotate() && isRotating(rotationX, rotationY);

                sendSprintPacket();
                sendSneakPacket();
                sendPlayerPacket(moving, rotating, position, rotationX, rotationY, onGround);

                this.prevOnGround = onGround;
            }

            ++this.positionUpdateTicks;
            this.autoJumpEnabled = this.mc.gameSettings.autoJump;
        }

        OnUpdateWalkingPlayerEvent.Post eventPos = new OnUpdateWalkingPlayerEvent.Post(position, rotationX, rotationY, onGround);
        MinecraftForge.EVENT_BUS.post(eventPos);
    }


    private void sendSprintPacket() {
        boolean sprinting = this.isSprinting();

        if (sprinting != this.serverSprintState) {
            if (sprinting) {
                this.connection.sendPacket(new CPacketEntityAction(Minecraft.getMinecraft().player, CPacketEntityAction.Action.START_SPRINTING));
            } else {
                this.connection.sendPacket(new CPacketEntityAction(Minecraft.getMinecraft().player, CPacketEntityAction.Action.STOP_SPRINTING));
            }
            this.serverSprintState = sprinting;
        }
    }

    private void sendSneakPacket() {
        boolean sneaking = this.isSneaking();

        if (sneaking != this.serverSneakState) {
            if (sneaking) {
                this.connection.sendPacket(new CPacketEntityAction(Minecraft.getMinecraft().player, CPacketEntityAction.Action.START_SNEAKING));
            } else {
                this.connection.sendPacket(new CPacketEntityAction(Minecraft.getMinecraft().player, CPacketEntityAction.Action.STOP_SNEAKING));
            }
            this.serverSneakState = sneaking;
        }
    }

    private void sendPlayerPacket(boolean moving, boolean rotating, Vec3d position, float rotationX, float rotationY, boolean onGround) {
        if (!this.isCurrentViewEntity()) return;

        if (this.isRiding()) {
            this.connection.sendPacket(new CPacketPlayer.PositionRotation(this.motionX, -999.0D, this.motionZ, rotationX, rotationY, onGround));
            moving = false;
        } else if (moving && rotating) {
            this.connection.sendPacket(new CPacketPlayer.PositionRotation(position.x, position.y, position.z, rotationX, rotationY, onGround));
        } else if (moving) {
            this.connection.sendPacket(new CPacketPlayer.Position(position.x, position.y, position.z, onGround));
        } else if (rotating) {
            this.connection.sendPacket(new CPacketPlayer.Rotation(rotationX, rotationY, onGround));
        } else if (this.prevOnGround != onGround) {
            this.connection.sendPacket(new CPacketPlayer(onGround));
        }

        if (moving) {
            this.positionUpdateTicks = 0;
        }
    }

    private boolean isMoving(Vec3d position) {
        double xDiff = position.x - this.lastReportedPosX;
        double yDiff = position.y - this.lastReportedPosY;
        double zDiff = position.z - this.lastReportedPosZ;

        return this.positionUpdateTicks >= 20 || xDiff * xDiff + yDiff * yDiff + zDiff * zDiff > 9.0E-4D;
    }

    private boolean isRotating(float rotationX, float rotationY) {
        return (rotationX - this.lastReportedYaw) != 0.0D || (rotationY - this.lastReportedPitch) != 0.0D;
    }

     */

    @Inject(method = {"onUpdate"}, at = @At("HEAD"), cancellable = true)
    public void onUpdate(CallbackInfo callbackInfo) {
        EventPlayerUpdate eventPlayerUpdate = new EventPlayerUpdate();
        MinecraftForge.EVENT_BUS.post(eventPlayerUpdate);
        if (eventPlayerUpdate.isCanceled()) {
            callbackInfo.cancel();
        }
    }

    @Override
    public void jump() {
        JumpEvent event = new JumpEvent();
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) super.jump();
    }

}
