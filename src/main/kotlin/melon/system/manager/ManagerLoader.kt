package melon.system.manager

import dev.zenhao.melon.Melon
import kotlinx.coroutines.Deferred
import melon.system.loader.AsyncLoader
import melon.utils.ClassUtils.instance
import java.lang.reflect.Modifier
import kotlin.system.measureTimeMillis

internal object ManagerLoader : AsyncLoader<List<Class<out Manager>>> {
    override var deferred: Deferred<List<Class<out Manager>>>? = null

    override suspend fun preLoad0(): List<Class<out Manager>> {
        val classes = AsyncLoader.classes.await()
        val list: List<Class<*>>

        val time = measureTimeMillis {
            val clazz = Manager::class.java

            list = classes.asSequence()
                .filter { Modifier.isFinal(it.modifiers) }
                .filter { it.name.startsWith("melon.managers") }
                .filter { clazz.isAssignableFrom(it) }
                .sortedBy { it.simpleName }
                .toList()
        }

        Melon.logger.info("${list.size} managers found, took ${time}ms")

        @Suppress("UNCHECKED_CAST")
        return list as List<Class<out Manager>>
    }

    override suspend fun load0(input: List<Class<out Manager>>) {
        val time = measureTimeMillis {
            for (clazz in input) {
                clazz.instance.subscribe()
            }
        }

        Melon.logger.info("${input.size} managers loaded, took ${time}ms")
    }
}