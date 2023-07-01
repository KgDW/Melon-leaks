package dev.zenhao.melon.utils.threads

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private val SERVICE: ExecutorService =
    Executors.newScheduledThreadPool((Runtime.getRuntime().availableProcessors() / 4).coerceAtMost(4).coerceAtLeast(1))

fun runAsyncThread(task: Runnable) {
    SERVICE.execute(task)
}
