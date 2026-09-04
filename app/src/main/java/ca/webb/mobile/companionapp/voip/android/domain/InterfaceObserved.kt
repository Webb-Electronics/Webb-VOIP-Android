package ca.webb.mobile.companionapp.voip.android.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Interface observed
 *
 * @constructor to create an observed value
 *
 * @param T the type of the value to be observed
 * @param value the value to be observed
 */
class InterfaceObserved<T>(
    value: T
) {
    private var _flow = MutableStateFlow(value)
    private val flow: StateFlow<T> = _flow.asStateFlow()

    /**
     * Observe the value as state in a composable function
     *
     * @return the value as state
     */
    @Composable
    fun observe() = flow.collectAsState()

    /**
     * Get the value
     *
     * @return the value
     */
    fun get(): T {
        return _flow.value
    }

    /**
     * Set the value
     *
     * @param value the value to set to
     */
    fun set(value: T) {
        _flow.update {
            value
        }
    }
}