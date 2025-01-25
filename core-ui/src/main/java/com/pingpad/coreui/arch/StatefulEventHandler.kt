package com.pingpad.coreui.arch

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

abstract class StatefulEventHandler<E, O, S, M>(
    initialState: S
) where S : ConvertibleState<S, M>, M : MutableConvertibleState<S> {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()
    val stateValue: S get() = _state.value

    private val _uiEvent = Channel<O>(Channel.BUFFERED)
    val uiEvent: Flow<O> = _uiEvent.receiveAsFlow()

    abstract suspend fun process(event: E, args: Any? = null)

    protected fun updateUiState(update: M.() -> Unit) {
        _state.update { currentState ->
            currentState.toMutable().apply(update).toImmutable()
        }
    }

    protected suspend fun O.processOneTimeEvent() {
        _uiEvent.send(this)
    }
}
