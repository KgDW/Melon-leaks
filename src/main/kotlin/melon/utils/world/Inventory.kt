package melon.utils.world

import melon.utils.Wrapper
import net.minecraft.item.Item
import net.minecraft.util.EnumHand

fun getItem(item: Item) =
    if (Wrapper.player!!.heldItemOffhand.getItem() === item) EnumHand.OFF_HAND else EnumHand.MAIN_HAND