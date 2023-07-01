package melon.system.command.args

import melon.system.util.interfaces.Nameable

/**
 * The ID for an argument
 */
@Suppress("UNUSED")
data class ArgIdentifier<T : Any>(override val name: String) : Nameable
