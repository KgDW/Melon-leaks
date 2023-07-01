package melon.system.command

import dev.zenhao.melon.utils.Wrapper
import kotlinx.coroutines.launch
import melon.system.command.args.AbstractArg
import melon.system.command.execute.ClientExecuteEvent
import melon.system.command.execute.SafeExecuteEvent
import melon.system.command.utils.BuilderBlock
import melon.system.command.utils.ExecuteBlock
import melon.system.event.AlwaysListening
import melon.utils.concurrent.threads.KernelScope
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import java.io.File

abstract class ClientCommand(
    name: String,
    alias: Array<out String> = emptyArray(),
    description: String = "No description",
) : CommandBuilder<ClientExecuteEvent>(name, alias, description), AlwaysListening {

    val prefixName get() = "$prefix$name"

    @CommandBuilder
    protected fun AbstractArg<*>.executeAsync(
        description: String = "No description",
        block: ExecuteBlock<ClientExecuteEvent>
    ) {
        val asyncExecuteBlock: ExecuteBlock<ClientExecuteEvent> = {
            KernelScope.launch { block() }
        }
        this.execute(description, block = asyncExecuteBlock)
    }

    @CommandBuilder
    protected fun AbstractArg<*>.executeSafe(
        description: String = "No description",
        block: ExecuteBlock<SafeExecuteEvent>
    ) {
        val safeExecuteBlock: ExecuteBlock<ClientExecuteEvent> = {
            toSafe()?.block()
        }
        this.execute(description, block = safeExecuteBlock)
    }

    fun ClientExecuteEvent.toSafe() =
        if (world != null && player != null && playerController != null && connection != null) SafeExecuteEvent(world, player, playerController, connection, this)
        else null

    protected companion object {
        val mc = Wrapper.mc
        val prefix: String get() = CommandManager.prefix
    }


//    @CommandBuilder
//    protected inline fun AbstractArg<*>.module(
//        name: String,
//        block: BuilderBlock<AbstractModule>
//    ) {
//        arg(ModuleArg(name), block)
//    }
//
//    @CommandBuilder
//    protected inline fun AbstractArg<*>.hudElement(
//        name: String,
//        block: BuilderBlock<AbstractHudElement>
//    ) {
//        arg(HudElementArg(name), block)
//    }

//    @CommandBuilder
//    protected inline fun AbstractArg<*>.block(
//        name: String,
//        block: BuilderBlock<Block>
//    ) {
//        arg(BlockArg(name), block)
//    }

//    @CommandBuilder
//    protected inline fun AbstractArg<*>.item(
//        name: String,
//        block: BuilderBlock<Item>
//    ) {
//        arg(ItemArg(name), block)
//    }

//    @CommandBuilder
//    protected inline fun AbstractArg<*>.player(
//        name: String,
//        block: BuilderBlock<PlayerProfile>
//    ) {
//        arg(PlayerArg(name), block)
//    }

//    @CommandBuilder
//    protected inline fun AbstractArg<*>.blockPos(
//        name: String,
//        block: BuilderBlock<BlockPos>
//    ) {
//        arg(BlockPosArg(name), block)
//    }
}