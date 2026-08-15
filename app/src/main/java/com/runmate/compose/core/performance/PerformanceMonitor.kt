package com.runmate.compose.core.performance

import android.os.SystemClock
import android.util.Log

object PerformanceMonitor {
    suspend fun <T> measure(name: String, block: suspend () -> T): T {
        val startedAt = SystemClock.elapsedRealtime()
        return try {
            block()
        } finally {
            val elapsed = SystemClock.elapsedRealtime() - startedAt
            Log.i("RunMatePerformance", "$name durationMs=$elapsed")
        }
    }
}
