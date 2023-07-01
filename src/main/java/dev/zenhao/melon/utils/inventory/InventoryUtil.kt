package dev.zenhao.melon.utils.inventory

import dev.zenhao.melon.manager.HotbarManager.onSpoof
import melon.utils.concurrent.threads.runSafe
import melon.utils.extension.packetClick
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiCrafting
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Enchantments
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Predicate

object InventoryUtil {
    var mc = Minecraft.getMinecraft()
    var currentItem = 0
    fun holdingItem(clazz: Class<*>): Boolean {
        val stack = mc.player.heldItemMainhand
        var result = isInstanceOf(stack, clazz)
        if (!result) {
            result = isInstanceOf(stack, clazz)
        }
        return result
    }

    fun push() {
        currentItem = mc.player.inventory.currentItem
    }

    fun pop() {
        mc.player.inventory.currentItem = currentItem
    }

    @JvmStatic
    fun setSlot(slot: Int) {
        if (slot > 8 || slot < 0) return
        mc.player.inventory.currentItem = slot
    }

    @JvmStatic
    fun pickItem(item: Int, allowInventory: Boolean): Int {
        val filter = ArrayList<ItemStack>()
        for (i1 in 0 until if (allowInventory) mc.player.inventory.mainInventory.size else 9) {
            if (Item.getIdFromItem(mc.player.inventory.mainInventory[i1].getItem()) == item) {
                filter.add(mc.player.inventory.mainInventory[i1])
            }
        }
        return if (filter.size >= 1) mc.player.inventory.mainInventory.indexOf(filter[0]) else -1
    }

    fun isInstanceOf(stack: ItemStack?, clazz: Class<*>): Boolean {
        if (stack == null) {
            return false
        }
        val item = stack.getItem()
        if (clazz.isInstance(item)) {
            return true
        }
        if (item is ItemBlock) {
            val block = Block.getBlockFromItem(item)
            return clazz.isInstance(block)
        }
        return false
    }

    fun findArmorSlot(type: EntityEquipmentSlot, binding: Boolean): Int {
        var slot = -1
        var damage = 0.0f
        for (i in 0..44) {
            var cursed: Boolean
            val s = Minecraft.getMinecraft().player.inventoryContainer.getSlot(i).stack
            if (s.getItem() === Items.AIR || s.getItem() !is ItemArmor) continue
            val armor = s.getItem() as ItemArmor
            if (armor.armorType != type) continue
            val currentDamage =
                (armor.damageReduceAmount + EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, s)).toFloat()
            cursed = binding && EnchantmentHelper.hasBindingCurse(s)
            val bl = cursed
            if (currentDamage <= damage || cursed) continue
            damage = currentDamage
            slot = i
        }
        return slot
    }

    fun findArmorSlot(type: EntityEquipmentSlot, binding: Boolean, withXCarry: Boolean): Int {
        var slot = findArmorSlot(type, binding)
        if (slot == -1 && withXCarry) {
            var damage = 0.0f
            for (i in 1..4) {
                var cursed: Boolean
                val craftingSlot = mc.player.inventoryContainer.inventorySlots[i]
                val craftingStack = craftingSlot.stack
                if (craftingStack.getItem() === Items.AIR || craftingStack.getItem() !is ItemArmor) continue
                val armor = craftingStack.getItem() as ItemArmor
                if (armor.armorType != type) continue
                val currentDamage = (armor.damageReduceAmount + EnchantmentHelper.getEnchantmentLevel(
                    Enchantments.PROTECTION,
                    craftingStack
                )).toFloat()
                cursed = binding && EnchantmentHelper.hasBindingCurse(craftingStack)
                if (currentDamage <= damage || cursed) continue
                damage = currentDamage
                slot = i
            }
        }
        return slot
    }

    fun findElytraSlot(type: EntityEquipmentSlot, binding: Boolean, withXCarry: Boolean): Int {
        var slot = findArmorSlot(type, binding)
        if (slot == -1 && withXCarry) {
            for (i in 1..4) {
                val craftingSlot = mc.player.inventoryContainer.inventorySlots[i]
                val craftingStack = craftingSlot.stack
                if (craftingStack.getItem() === Items.AIR || craftingStack.getItem() !is ItemElytra) continue
                slot = i
            }
        }
        return slot
    }

    fun findInInventory(
        condition: Predicate<ItemStack?>,
        xCarry: Boolean
    ): Int {
        for (i in 9..44) {
            val stack = mc.player.inventoryContainer
                .inventory[i]
            if (condition.test(stack)) {
                return i
            }
        }
        if (xCarry) {
            for (i in 1..4) {
                val stack = mc.player.inventoryContainer
                    .inventory[i]
                if (condition.test(stack)) {
                    return i
                }
            }
        }
        return -1
    }

    fun findItemInventorySlot(item: Item, offHand: Boolean, withXCarry: Boolean): Int {
        var slot = findItemInventorySlot(item, offHand)
        if (slot == -1 && withXCarry) {
            for (i in 1..4) {
                var craftingStackItem: Item?
                val craftingSlot = mc.player.inventoryContainer.inventorySlots[i]
                val craftingStack = craftingSlot.stack
                if (craftingStack.getItem() === Items.AIR || craftingStack.getItem()
                        .also { craftingStackItem = it } !== item
                ) continue
                slot = i
            }
        }
        return slot
    }

    fun findItemInventorySlot(item: Item, offHand: Boolean): Int {
        val slot = AtomicInteger()
        slot.set(-1)
        for ((key, value) in inventoryAndHotbarSlots) {
            if (value.getItem() !== item || key == 45 && !offHand) continue
            slot.set(key)
            return slot.get()
        }
        return slot.get()
    }

    val inventoryAndHotbarSlots: Map<Int, ItemStack>
        get() = if (mc.currentScreen is GuiCrafting) {
            fuckYou3arthqu4kev2(10, 45)
        } else getInventorySlots(9, 44)

    private fun getInventorySlots(currentI: Int, last: Int): Map<Int, ItemStack> {
        val fullInventorySlots = HashMap<Int, ItemStack>()
        for (current in currentI..last) {
            fullInventorySlots[current] = mc.player.inventoryContainer.inventory[current]
        }
        return fullInventorySlots
    }

    private fun fuckYou3arthqu4kev2(currentI: Int, last: Int): Map<Int, ItemStack> {
        val fullInventorySlots = HashMap<Int, ItemStack>()
        for (current in currentI..last) {
            fullInventorySlots[current] = mc.player.openContainer.inventory[current]
        }
        return fullInventorySlots
    }

    fun findEmptySlots(withXCarry: Boolean): List<Int> {
        val outPut = ArrayList<Int>()
        for ((key, value) in inventoryAndHotbarSlots) {
            if (!value.isEmpty && value.getItem() !== Items.AIR) continue
            outPut.add(key)
        }
        if (withXCarry) {
            for (i in 1..4) {
                val craftingSlot = mc.player.inventoryContainer.inventorySlots[i]
                val craftingStack = craftingSlot.stack
                if (!craftingStack.isEmpty() && craftingStack.getItem() !== Items.AIR) continue
                outPut.add(i)
            }
        }
        return outPut
    }

    fun isBlock(item: Item?, clazz: Class<*>): Boolean {
        if (item is ItemBlock) {
            val block = item.block
            return clazz.isInstance(block)
        }
        return false
    }

    fun getItemInventory(item: Item): List<Int> {
        val ints: MutableList<Int> = ArrayList()
        for (i in 9..35) {
            val target = mc.player.inventory.getStackInSlot(i).getItem()
            if (item is ItemBlock && item.block == item) {
                ints.add(i)
            }
        }
        if (ints.size == 0) {
            ints.add(-1)
        }
        return ints
    }

    fun switchToHotbarSlot(slot: Int) {
        onSpoof(slot)
    }

    fun switchToHotbarSlot(clazz: Class<*>, silent: Boolean) {
        val slot = findHotbarBlock(clazz)
        if (slot > -1) {
            switchToHotbarSlot(slot)
        }
    }

    fun findHotbarItem(clazz: Class<*>): Int {
        for (i in 0..8) {
            val stack = mc.player.inventory.getStackInSlot(i)
            if (stack != ItemStack.EMPTY) {
                if (clazz.isInstance(stack.getItem())) {
                    return i
                }
            }
        }
        return -1
    }

    fun findHotbarBlock(clazz: Class<*>): Int {
        for (i in 0..8) {
            val stack = mc.player.inventory.getStackInSlot(i)
            if (stack != ItemStack.EMPTY) {
                if (clazz.isInstance(stack.getItem())) {
                    return i
                }
                if (stack.getItem() is ItemBlock) {
                    val block = (stack.getItem() as ItemBlock).block
                    if (clazz.isInstance(block)) {
                        return i
                    }
                }
            }
        }
        return -1
    }

    @JvmStatic
    fun getItemHotbar(input: Item?): Int {
        for (i in 0..8) {
            if (mc.player.inventory != null) {
                val item = mc.player.inventory.getStackInSlot(i).getItem()
                if (Item.getIdFromItem(item) == Item.getIdFromItem(input)) {
                    return i
                }
            }
        }
        return -1
    }

    fun getItemInv(input: Item?): Int {
        for (i in 0..44) {
            if (mc.player.inventory != null) {
                val item = mc.player.inventory.getStackInSlot(i).getItem()
                if (Item.getIdFromItem(item) == Item.getIdFromItem(input)) {
                    return i
                }
            }
        }
        return -1
    }

    @JvmStatic
    fun findHotbarBlock(block: Block?): Int {
        if (ItemUtil.areSame(mc.player.heldItemOffhand, block)) {
            return -2
        }
        var result = -1
        for (i in 0..8) {
            val stack = mc.player.inventory.getStackInSlot(i)
            if (ItemUtil.areSame(stack, block)) {
                result = i
                if (mc.player.inventory.currentItem == i) {
                    break
                }
            }
        }
        return result
    }

    @JvmStatic
    fun findHotbarItem(item: Item?): Int {
        if (ItemUtil.areSame(mc.player.heldItemOffhand, item)) {
            return -2
        }
        var result = -1
        for (i in 0..8) {
            val stack = mc.player.inventory.getStackInSlot(i)
            if (ItemUtil.areSame(stack, item)) {
                result = i
                if (mc.player.inventory.currentItem == i) {
                    break
                }
            }
        }
        return result
    }

    fun findItem(item: Item, xCarry: Boolean): Int {
        if (mc.player.inventory.getItemStack().getItem() === item) {
            return -2
        }
        for (i in 9..44) {
            val stack = mc.player.inventoryContainer.inventory[i]
            if (stack.getItem() === item) {
                return i
            }
        }
        if (xCarry) {
            for (i in 1..4) {
                val stack = mc.player.inventoryContainer.inventory[i]
                if (stack.getItem() === item) {
                    return i
                }
            }
        }
        return -1
    }

    fun getCount(item: Item): Int {
        var result = 0
        for (i in 0..45) {
            val stack = mc.player.inventoryContainer.inventory[i]
            if (stack.getItem() === item) {
                result += stack.count
            }
        }
        if (mc.player.inventory.getItemStack().getItem() === item) {
            result += mc.player.inventory.getItemStack().count
        }
        return result
    }

    fun isHolding(item: Item?): Boolean {
        return isHolding(mc.player, item)
    }

    fun isHolding(block: Block?): Boolean {
        return isHolding(mc.player, block)
    }

    fun isHolding(entity: EntityLivingBase, item: Item?): Boolean {
        val mainHand = entity.heldItemMainhand
        val offHand = entity.heldItemOffhand
        return ItemUtil.areSame(mainHand, item) || ItemUtil.areSame(offHand, item)
    }

    fun isHolding(entity: EntityLivingBase, block: Block?): Boolean {
        val mainHand = entity.heldItemMainhand
        val offHand = entity.heldItemOffhand
        return ItemUtil.areSame(mainHand, block) || ItemUtil.areSame(offHand, block)
    }

    class Task {
        private val slot: Int
        private val quickClick: Boolean
        private val packetClick: Boolean

        constructor() {
            slot = -1
            quickClick = false
            packetClick = false
        }

        constructor(slot: Int, packetClick: Boolean = false) {
            this.slot = slot
            quickClick = false
            this.packetClick = packetClick
        }

        constructor(slot: Int, quickClick: Boolean, packetClick: Boolean = false) {
            this.slot = slot
            this.quickClick = quickClick
            this.packetClick = packetClick
        }

        fun runTask() {
            runSafe {
                if (slot != -1) {
                    if (packetClick) {
                        connection.sendPacket(
                            packetClick(
                                slot,
                                if (quickClick) ClickType.QUICK_MOVE else ClickType.PICKUP
                            )
                        )
                    } else {
                        playerController.updateController()
                        playerController.windowClick(
                            player.inventoryContainer.windowId,
                            slot,
                            0,
                            if (quickClick) ClickType.QUICK_MOVE else ClickType.PICKUP,
                            player
                        )
                        playerController.updateController()
                        playerController.windowClick(
                            player.inventoryContainer.windowId,
                            slot,
                            0,
                            if (quickClick) ClickType.QUICK_MOVE else ClickType.PICKUP,
                            player
                        )
                    }
                }
            }
        }
    }
}