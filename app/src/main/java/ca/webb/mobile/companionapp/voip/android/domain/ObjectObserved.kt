package ca.webb.mobile.companionapp.voip.android.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

/**
 * To make an object observable by the UI.
 *
 * Unlike InterfaceObserved, the observe() function does not return any value that is meaningful.
 *
 * But to trigger the UI update upon requireUpdate() is called, it is necessary to call and use the value returned by observe().
 *
 * Here is an example of how to use this class:
 *
 * @sample _OBJECT_OBSERVED_SAMPLE
 *
 * @constructor to be inherited by subclasses
 */
open class ObjectObserved {
    private var observer = InterfaceObserved(Int.MIN_VALUE)

    /**
     * tell the observer to require update in the UI.
     */
    fun requireUpdate() {
        var value = observer.get()
        if (value == Int.MAX_VALUE) {
            value = Int.MIN_VALUE
        }
        observer.set(value+1)
    }

    /**
     * Observe change in a composable function
     * You should call this function in the composable function, and call on the value to take effect
     *
     * @return THIS VALUE SHOULD NOT BE USED DIRECTLY IN THE COMPOSABLE FUNCTION
     */
    @Composable
    fun observe(): State<Int> {
        return observer.observe()
    }

}

@Suppress("FunctionName", "Unused")
private fun _OBJECT_OBSERVED_SAMPLE() {
    // Sample usage of ObjectObserved
    class ExampleObject : ObjectObserved() {
        var value = 0
        fun increase() {
            value++
            requireUpdate()
        }
    }

    @Composable
    fun observeUI() {
        val exampleObject = ExampleObject()
        val observed = exampleObject.observe()
        println(observed.value)
    }
}