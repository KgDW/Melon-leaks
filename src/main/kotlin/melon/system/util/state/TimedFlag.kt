package melon.system.util.state

import dev.zenhao.melon.module.IModule

open class TimedFlag<T>(value: T): IModule() {
    var value = value
        set(value) {
            if (value != field) {
                lastUpdateTime = System.currentTimeMillis()
                field = value
            }
        }

    var lastUpdateTime = System.currentTimeMillis()
        private set

    fun resetTime() {
        lastUpdateTime = System.currentTimeMillis()
    }
}