package dev.zenhao.melon.mixin.client.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ItemAppleGold.class)
public class MixinItemAppleGold {
    /*
    @Overwrite
    protected void onFoodEaten(ItemStack p_77849_1_, World p_77849_2_, EntityPlayer p_77849_3_) {
        if (!p_77849_2_.isRemote) {
            if (p_77849_1_.getMetadata() > 0) {
                p_77849_3_.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 400, 20));
                p_77849_3_.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 6000, 100));
                p_77849_3_.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 6000, 0));
                p_77849_3_.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 2400, 3));
            } else {
                p_77849_3_.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 100, 20));
                p_77849_3_.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 2400, 100));
            }
        }
    }
    */
}
