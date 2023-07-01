package dev.zenhao.melon.module.hud.info

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.module.hud.HudRenderUtil
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

@HUDModule.Info(name = "Obsidian", x = 60, y = 90, width = 16, height = 16, category = Category.HUD)
class Obsidian : HUDModule() {
    override fun onRender() {
        var var1 = mc.player.inventory.mainInventory.stream()
            .filter { var0: ItemStack -> var0.getItem() === Item.getItemFromBlock(Blocks.OBSIDIAN) }
            .mapToInt { obj: ItemStack -> obj.count }.sum()
        if (mc.player.heldItemOffhand.getItem() === Item.getItemFromBlock(Blocks.OBSIDIAN)) {
            var1 += mc.player.heldItemOffhand.stackSize
        }
        val var2 = ItemStack(Item.getItemFromBlock(Blocks.OBSIDIAN), var1)
        HudRenderUtil.itemRender(x, y, var2)
    }
}