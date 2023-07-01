package melon.system.loader

import dev.zenhao.melon.Melon
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import melon.utils.ClassUtils
import melon.utils.concurrent.threads.defaultScope
import kotlin.system.measureTimeMillis

internal interface AsyncLoader<T> {
    var deferred: Deferred<T>?

    fun preLoad() {
        deferred = preLoadAsync()
    }

    private fun preLoadAsync(): Deferred<T> {
        return defaultScope.async { preLoad0() }
    }

    suspend fun load() {
        load0((deferred ?: preLoadAsync()).await())
    }

    suspend fun preLoad0(): T
    suspend fun load0(input: T)

    companion object {
        val classes =
            defaultScope.async {
                val list: List<Class<*>>
                val time = measureTimeMillis {
                    list = ClassUtils.findClasses("melon") { !it.contains("mixins") }
                }

                Melon.logger.info("${list.size} classes found, took ${time}ms")
                list
            }
    }
}
