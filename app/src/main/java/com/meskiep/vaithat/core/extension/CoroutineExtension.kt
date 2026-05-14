package com.meskiep.vaithat.core.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun LifecycleOwner.launchIO(blockIO: suspend () -> Unit, blockMain: (suspend () -> Unit)? = null) {
    lifecycleScope.launch(Dispatchers.IO) {
        blockIO()

        blockMain?.let {
            withContext(Dispatchers.Main) {
                it()
            }
        }
    }
}

fun <T> LifecycleOwner.launchIO(blockIO: suspend () -> T, blockMain: (suspend (T) -> Unit)? = null) {
    lifecycleScope.launch(Dispatchers.IO) {
        val result = blockIO()

        blockMain?.let {
            withContext(Dispatchers.Main) {
                it(result)
            }
        }
    }
}

fun <T, Y, U> LifecycleOwner.launchIO(blockIO: suspend () -> Triple<T, Y, U>, blockMain: (suspend (T, Y, U) -> Unit)? = null) {
    lifecycleScope.launch(Dispatchers.IO) {
        val (t, y, u) = blockIO()

        blockMain?.let {
            withContext(Dispatchers.Main) {
                it(t, y, u)
            }
        }
    }
}