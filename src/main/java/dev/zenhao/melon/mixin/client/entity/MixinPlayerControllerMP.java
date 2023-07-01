package dev.zenhao.melon.mixin.client.entity;

import dev.zenhao.melon.event.events.block.BlockEvent;
import dev.zenhao.melon.module.ModuleManager;
import dev.zenhao.melon.module.modules.misc.PacketEat;
import dev.zenhao.melon.module.modules.player.Reach;
import melon.events.WindowClickEvent;
import melon.utils.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created by 086 on 3/10/2018.
 */
@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP {

    @Shadow
    public int currentPlayerItem;

    @Inject(method = "windowClick", at = @At("HEAD"))
    private void onWindowClick(int windowID, int currentSlot, int targetSlot, ClickType clickType, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir) {
        WindowClickEvent event = new WindowClickEvent(windowID, currentSlot, targetSlot, clickType, player);
        event.post();
    }

    @Inject(method = {"extendedReach"}, at = {@At("RETURN")}, cancellable = true)
    private void reachHook(final CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().world == null) {
            return;
        }
        if (ModuleManager.getModuleByClass(Reach.class).isEnabled()) {
            callbackInfoReturnable.setReturnValue(true);
            callbackInfoReturnable.cancel();
        }
    }

    @Inject(method = {"getBlockReachDistance"}, at = {@At("RETURN")}, cancellable = true)
    private void getReachDistanceHook(final CallbackInfoReturnable<Float> callbackInfoReturnable) {
        if (Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().world == null) {
            return;
        }
        if (ModuleManager.getModuleByClass(Reach.class).isEnabled()) {
            callbackInfoReturnable.setReturnValue(Reach.getReach());
            callbackInfoReturnable.cancel();
        }
    }

    @Inject(method = "onStoppedUsingItem", at = @At("HEAD"), cancellable = true)
    public void onStoppedUsingItem(EntityPlayer playerIn, CallbackInfo ci) {
        if (playerIn.getHeldItem(playerIn.getActiveHand()).getItem() instanceof ItemFood || playerIn.getHeldItem(playerIn.getActiveHand()).getItem() instanceof ItemPotion) {
            if (ModuleManager.getModuleByClass(PacketEat.class).isEnabled()) {
                this.syncCurrentPlayItem();
                playerIn.stopActiveHand();
                ci.cancel();
            }
        }
    }

    @Inject(method = {"onPlayerDamageBlock"}, at = @At("HEAD"))
    private void onPlayerDamageBlockHook(final BlockPos pos, final EnumFacing face, final CallbackInfoReturnable<Boolean> info) {
        new BlockEvent(pos, face).post();
    }

    @Inject(method = {"clickBlock"}, at = @At("HEAD"))
    private void onPlayerClickBlock(final BlockPos pos, final EnumFacing face, final CallbackInfoReturnable<Boolean> info) {
        new BlockEvent(pos, face).post();
    }

    /**
     * @author zenhao
     * @reason fuck
     */
    @Overwrite
    public void syncCurrentPlayItem() {
        EntityPlayerSP player = Wrapper.getPlayer();
        if (player != null) {
            int i = player.inventory.currentItem;

            if (i != this.currentPlayerItem) {
                this.currentPlayerItem = i;
                player.connection.sendPacket(new CPacketHeldItemChange(this.currentPlayerItem));
            }
        }
    }

}
