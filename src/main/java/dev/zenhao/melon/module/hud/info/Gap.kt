package dev.zenhao.melon.module.hud.info

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.module.hud.HudRenderUtil
import net.minecraft.init.Items
import net.minecraft.item.ItemStack

@HUDModule.Info(name = "Gap", x = 260, y = 100, width = 100, height = 10, category = Category.HUD)
class Gap : HUDModule() {
    override fun onRender() {
        var var1 = mc.player.inventory.mainInventory.stream()
            .filter { var0: ItemStack -> var0.getItem() === Items.GOLDEN_APPLE }
            .mapToInt { obj: ItemStack -> obj.count }.sum()
        if (mc.player.heldItemOffhand.getItem() === Items.GOLDEN_APPLE) {
            var1 += mc.player.heldItemOffhand.stackSize
        }
        val var2 = ItemStack(Items.GOLDEN_APPLE, var1)
        HudRenderUtil.itemRender(x, y, var2)
    }
}