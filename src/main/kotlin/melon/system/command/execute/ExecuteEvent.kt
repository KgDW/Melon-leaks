package melon.system.command.execute

import melon.system.command.AbstractCommandManager
import melon.system.command.CommandManager
import melon.system.command.args.AbstractArg
import melon.system.command.args.ArgIdentifier
import melon.system.command.args.GreedyStringArg
import melon.system.event.ClientEvent
import melon.system.event.SafeClientEvent
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.PlayerControllerMP
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.network.NetHandlerPlayClient


class ClientExecuteEvent(
    args: Array<String>
) : ClientEvent(), IExecuteEvent by ExecuteEvent(CommandManager, args)

class SafeExecuteEvent internal constructor(
    world: WorldClient,
    player: EntityPlayerSP,
    playerController: PlayerControllerMP,
    connection: NetHandlerPlayClient,
    event: ClientExecuteEvent
) : SafeClientEvent(world, player, playerController, connection), IExecuteEvent by event

/**
 * Default implementation of [IExecuteEvent]
 */
open class ExecuteEvent(
    override val commandManager: AbstractCommandManager<*>,
    override val args: Array<String>
) : IExecuteEvent {

    /**
     * Mapping [ArgIdentifier] to their converted arguments
     */
    private val mappedArgs = HashMap<ArgIdentifier<*>, Any>()

    override suspend fun mapArgs(argTree: List<AbstractArg<*>>) {
        for ((index, arg) in argTree.withIndex()) {
            if (arg is GreedyStringArg) {
                arg.convertToType(args.slice(index until args.size).joinToString(" "))?.let {
                    mappedArgs[arg.identifier] = it
                }
                break
            } else {
                arg.convertToType(args.getOrNull(index))?.let {
                    mappedArgs[arg.identifier] = it
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override val <T : Any> ArgIdentifier<T>.value: T
        get() = mappedArgs[this] as T

}
