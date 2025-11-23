package androidx.lifecycle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

open class ViewModel {
    private val job = SupervisorJob()
    val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Default + job)

    protected open fun onCleared() {}

    fun clear() {
        onCleared()
        viewModelScope.cancel()
    }
}

