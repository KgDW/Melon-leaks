package dev.zenhao.melon.manager

import dev.zenhao.melon.utils.TpsCalculator
import dev.zenhao.melon.utils.inventory.ClickFuture
import dev.zenhao.melon.utils.inventory.InventoryTask
import dev.zenhao.melon.utils.inventory.StepFuture
import dev.zenhao.melon.utils.inventory.removeHoldingItem
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap
import melon.events.ConnectionEvent
import melon.events.PacketEvents
import melon.events.RunGameLoopEvent
import melon.system.event.AlwaysListening
import melon.system.event.SafeClientEvent
import melon.system.event.listener
import melon.system.event.safeEventListener
import melon.utils.TickTimer
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.network.play.server.SPacketConfirmTransaction
import java.util.*

object InventoryTaskManager : AlwaysListening {
    private val confirmMap = Short2ObjectOpenHashMap<ClickFuture>()
    private val taskQueue = PriorityQueue<InventoryTask>()
    private val timer = TickTimer()
    private var lastTask: InventoryTask? = null

    fun onInit() {
        listener<PacketEvents.Receive> {
            if (it.packet !is SPacketConfirmTransaction) return@listener
            synchronized(InventoryTaskManager) {
                confirmMap.remove(it.packet.actionNumber)?.confirm()
            }
        }

        safeEventListener<RunGameLoopEvent.Render> {
            if (lastTask == null && taskQueue.isEmpty()) return@safeEventListener
            if (!timer.tick(0L)) return@safeEventListener

            lastTaskOrNext()?.let {
                runTask(it)
            }
        }

        listener<ConnectionEvent.Disconnect> {
            reset()
        }
    }

    fun addTask(task: InventoryTask) {
        synchronized(InventoryTaskManager) {
            taskQueue.add(task)
        }
    }

    fun runNow(event: SafeClientEvent, task: InventoryTask) {
        event {
            if (!player.inventory.itemStack.isEmpty) {
                removeHoldingItem()
            }

            while (!task.finished) {
                task.runTask(event)?.let {
                    handleFuture(it)
                }
            }

            timer.reset((task.postDelay * TpsCalculator.multiplier).toLong())
        }
    }

    private fun SafeClientEvent.lastTaskOrNext(): InventoryTask? {
        return lastTask ?: run {
            val newTask = synchronized(InventoryTaskManager) {
                taskQueue.poll()?.also { lastTask = it }
            } ?: return null

            if (!player.inventory.itemStack.isEmpty) {
                removeHoldingItem()
                return null
            }

            newTask
        }
    }

    private fun SafeClientEvent.runTask(task: InventoryTask) {
        if (mc.currentScreen is GuiContainer && !task.runInGui && !player.inventory.itemStack.isEmpty) {
            timer.reset(500L)
            return
        }

        if (task.delay == 0L) {
            runNow(this, task)
        } else {
            task.runTask(this)?.let {
                handleFuture(it)
                timer.reset((task.delay * TpsCalculator.multiplier).toLong())
            }
        }

        if (task.finished) {
            timer.reset((task.postDelay * TpsCalculator.multiplier).toLong())
            lastTask = null
            return
        }
    }

    private fun handleFuture(future: StepFuture) {
        if (future is ClickFuture) {
            synchronized(InventoryTaskManager) {
                confirmMap[future.id] = future
            }
        }
    }

    private fun reset() {
        synchronized(InventoryTaskManager) {
            confirmMap.clear()
            lastTask?.cancel()
            lastTask = null
            taskQueue.clear()
        }
    }

}