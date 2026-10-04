package dev.muazkadan.myapplication.testing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking

/**
 * Cancels the ViewModel's coroutines and waits for them to finish. Call it before
 * `Dispatchers.resetMain()`: work still running on another dispatcher, such as a real DataStore's
 * read on `Dispatchers.IO`, would otherwise resume onto Main after the reset and fail whichever
 * test happens to run next.
 *
 * Main must be an `UnconfinedTestDispatcher`. A cancelled coroutine finishes on its dispatcher, and
 * a `StandardTestDispatcher` doesn't run while this blocks, so the join would never return.
 */
fun ViewModel.cancelScopeForTest() {
    val job = viewModelScope.coroutineContext[Job] ?: return
    runBlocking { job.cancelAndJoin() }
}
