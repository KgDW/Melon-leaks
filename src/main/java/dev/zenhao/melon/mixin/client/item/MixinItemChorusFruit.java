package dev.zenhao.melon.mixin.client.item;

import dev.zenhao.melon.event.events.player.ChorusUseEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemChorusFruit;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = {ItemChorusFruit.class}, priority = Integer.MAX_VALUE)
public class MixinItemChorusFruit extends ItemFood {

    public MixinItemChorusFruit(int p_i46747_1_, float p_i46747_2_) {
        super(p_i46747_1_, p_i46747_2_, false);
    }

    /**
     * @author zenhao
     * @reason fuck
     */
    @NotNull
    @Overwrite
    public ItemStack onItemUseFinish(@NotNull ItemStack stack, @NotNull World world, @NotNull EntityLivingBase player) {
        ItemStack lvt_4_1_ = super.onItemUseFinish(stack, world, player);
        if (!world.isRemote) {
            double lvt_5_1_ = player.posX;
            double lvt_7_1_ = player.posY;
            double lvt_9_1_ = player.posZ;

            for (int lvt_11_1_ = 0; lvt_11_1_ < 16; ++lvt_11_1_) {
                double xPos = player.posX + (player.getRNG().nextDouble() - 0.5) * 16.0;
                double yPos = MathHelper.clamp(player.posY + (double) (player.getRNG().nextInt(16) - 8), 0.0, world.getActualHeight() - 1);
                double zPos = player.posZ + (player.getRNG().nextDouble() - 0.5) * 16.0;
                if (player.isRiding()) {
                    player.dismountRidingEntity();
                }
                if (player instanceof EntityPlayer) {
                    MinecraftForge.EVENT_BUS.post(new ChorusUseEvent(new BlockPos(xPos, yPos, zPos), (EntityPlayer) player, false));
                }
                if (player.attemptTeleport(xPos, yPos, zPos)) {
                    world.playSound(null, lvt_5_1_, lvt_7_1_, lvt_9_1_, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    player.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                    break;
                }
            }

            if (player instanceof EntityPlayer) {
                ((EntityPlayer) player).getCooldownTracker().setCooldown(this, 20);
            }
        }

        return lvt_4_1_;
    }
}
