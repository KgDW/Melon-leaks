package melon.utils.concurrent.threads

import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
val mainScope = CoroutineScope(newSingleThreadContext("Melon Main"))
val defaultScope = CoroutineScope(Dispatchers.Default)
inline val Job?.isActiveOrFalse get() = this?.isActive ?: false