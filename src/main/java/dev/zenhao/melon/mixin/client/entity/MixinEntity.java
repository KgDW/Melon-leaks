package dev.zenhao.melon.mixin.client.entity;

import dev.zenhao.melon.event.events.entity.EntityEvent;
import dev.zenhao.melon.event.events.entity.PushEvent;
import melon.events.StatusEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(value = {Entity.class})
public abstract class MixinEntity {
    public Minecraft mc = Minecraft.getMinecraft();
    @Shadow
    public double posX;
    @Shadow
    public double posY;
    @Shadow
    public double posZ;
    @Shadow
    public float rotationPitch;
    @Shadow
    public float rotationYaw;
    @Shadow
    public Entity ridingEntity;
    @Shadow
    public double motionX;
    @Shadow
    public double motionY;
    @Shadow
    public double motionZ;
    @Shadow
    public boolean onGround;
    @Shadow
    public boolean isAirBorne;
    @Shadow
    public boolean noClip;
    @Shadow
    public boolean isInWeb;
    @Shadow
    public float stepHeight;
    @Shadow
    public float distanceWalkedModified;
    @Shadow
    public float distanceWalkedOnStepModified;
    @Shadow
    public float width;
    @Shadow
    public Random rand;
    @Shadow
    public int nextStepDistance;
    @Shadow
    public int fire;
    @Shadow
    public EntityDataManager dataManager;
    @Final
    @Shadow
    public static DataParameter<Byte> FLAGS;

    @Shadow
    public boolean isRiding() {
        return this.getRidingEntity() != null;
    }

    @Shadow
    public Entity getRidingEntity() {
        return this.ridingEntity;
    }

    @Shadow
    public abstract boolean isSprinting();

    @Shadow
    public abstract AxisAlignedBB getEntityBoundingBox();

    @Shadow
    public abstract void setEntityBoundingBox(AxisAlignedBB bb);

    @Shadow
    protected abstract boolean canTriggerWalking();

    @Shadow
    public abstract boolean isInWater();

    @Shadow
    protected abstract void dealFireDamage(int amount);

    @Shadow
    public abstract boolean isWet();

    @Shadow
    public abstract void addEntityCrashInfo(CrashReportCategory category);

    @Shadow
    protected abstract void doBlockCollisions();

    @Shadow
    protected abstract void playStepSound(BlockPos pos, Block blockIn);

    @Shadow
    public abstract boolean isSneaking();

    public int getNextStepDistance() {
        return nextStepDistance;
    }

    public void setNextStepDistance(int nextStepDistance) {
        this.nextStepDistance = nextStepDistance;
    }

    public int getFire() {
        return fire;
    }

    @Shadow
    public abstract void setFire(int seconds);

    @Redirect(method = "applyEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    public void addVelocity(Entity entity, double x, double y, double z) {
        EntityEvent entityCollisionEvent = new EntityEvent(entity, x, y, z);
        MinecraftForge.EVENT_BUS.post(entityCollisionEvent);
        if (entityCollisionEvent.isCanceled()) return;

        entity.motionX += x;
        entity.motionY += y;
        entity.motionZ += z;

        entity.isAirBorne = true;
    }

    @Shadow
    public void move(MoverType type, double x, double y, double z) {

    }

    public void jump() {
    }

    public boolean first = false;
    public StatusEvent oldStatus;

    /**
     * @author zenhao
     * @reason bypass ElytraFly
     */
    @Overwrite
    public void setFlag(int status, boolean glow) {
        byte b0 = this.dataManager.get(FLAGS);
        if (glow) {
            this.dataManager.set(FLAGS, (byte)(b0 | 1 << status));
        } else {
            this.dataManager.set(FLAGS, (byte)(b0 & ~(1 << status)));
        }
        StatusEvent event = new StatusEvent(status, glow);
        if (!first) {
            event.post();
            oldStatus = event;
            first = true;
        }
        if (oldStatus != null) {
            if (oldStatus.getStatus() != status) {
                event.post();
                oldStatus = event;
            }
        }
    }

    @Inject(method = "isEntityInsideOpaqueBlock", at = @At("HEAD"), cancellable = true)
    private void onIsEntityInsideOpaqueBlock(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Redirect(method = {"applyEntityCollision"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    public void addVelocityHook(final Entity entity, final double x, final double y, final double z) {
        final PushEvent event = new PushEvent(entity, x, y, z, true);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            entity.motionX += event.x;
            entity.motionY += event.y;
            entity.motionZ += event.z;
            entity.isAirBorne = event.airbone;
        }
    }
}