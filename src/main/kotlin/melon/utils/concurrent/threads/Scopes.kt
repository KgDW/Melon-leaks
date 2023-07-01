package melon.utils.concurrent.threads

import dev.zenhao.melon.Melon
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext
import kotlin.math.max

private val pool0 = Runtime.getRuntime().availableProcessors().let { cpuCount ->
    val maxSize = max(cpuCount * 2, 8)
    ThreadPoolExecutor(
        cpuCount * 2,
        maxSize,
        5L,
        TimeUnit.SECONDS,
        SynchronousQueue(),
        CountingThreadFactory("${Melon.MOD_NAME}-EventPool")
    )
}

private val context0 = pool0.asCoroutineDispatcher()

/**
 * Scope for heavy loaded task
 */
internal object EventSystemScope : CoroutineScope by CoroutineScope(context0) {
    val pool = pool0
    val context = context0
}

internal object KernelScheduler : CoroutineDispatcher() {
    private val singlePoolExecutor = Executors.newSingleThreadExecutor { Thread(it, "${Melon.MOD_NAME}-Kernel") }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        singlePoolExecutor.submit(block)
    }
}

internal object KernelScope : CoroutineScope {
    override val coroutineContext: CoroutineContext = KernelScheduler + CoroutineName("${Melon.MOD_NAME}-KernelScope")
}

internal object MainScheduler : CoroutineDispatcher() {
    val taskCount = AtomicInteger(0)

    private var threadAmount = 0

    private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1) {
        threadAmount++
        Thread(it, "${Melon.MOD_NAME}-Main-$threadAmount")
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        executor.submit(block)
        taskCount.incrementAndGet()
    }
}

internal object MainScope : CoroutineScope {
    override val coroutineContext: CoroutineContext = MainScheduler + CoroutineName("${Melon.MOD_NAME}-MainScope")

    val tasks get() = MainScheduler.taskCount
}