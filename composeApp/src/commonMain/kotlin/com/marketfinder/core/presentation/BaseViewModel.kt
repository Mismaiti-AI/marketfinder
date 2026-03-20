package com.marketfinder.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Generic UI state for any screen. Use with [BaseViewModel.uiStateFrom].
 *
 * ```kotlin
 * val state: StateFlow<UiState<List<Event>>> = uiStateFrom(getEventsUseCase())
 * ```
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

/**
 * Pre-built base ViewModel with centralized error handling and loading state.
 *
 * All generated ViewModels MUST extend this class instead of ViewModel() directly.
 *
 * Provides:
 * - [safeLaunch] — coroutine launcher with Dispatchers.IO + automatic error/loading handling
 * - [isLoading] — shared loading state for combine() patterns
 * - [error] — shared error state for combine() patterns
 * - [clearError] — reset error state
 * - [uiStateFrom] — helper to combine a data flow with loading/error into [UiState]
 *
 * Usage with helper:
 * ```kotlin
 * class EventsViewModel(
 *     private val getEventsUseCase: GetEventsUseCase
 * ) : BaseViewModel() {
 *
 *     val state: StateFlow<UiState<List<Event>>> = uiStateFrom(getEventsUseCase())
 *
 *     init { loadEvents() }
 *     fun loadEvents() = safeLaunch { getEventsUseCase.load() }
 * }
 * ```
 *
 * Usage with transform:
 * ```kotlin
 * val state: StateFlow<UiState<HomeData>> = uiStateFrom(
 *     getEventsUseCase(),
 *     getNewsUseCase()
 * ) { events, news -> HomeData(events, news) }
 * ```
 */
abstract class BaseViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _isLoading.value = false
        _error.value = throwable.message ?: "An unexpected error occurred"
    }

    /**
     * Launch a coroutine on Dispatchers.IO with automatic loading/error handling.
     *
     * - Sets [isLoading] to true before execution, false after
     * - Catches all exceptions and sets [error] automatically
     * - Runs on [Dispatchers.IO] — safe for database/network calls
     *
     * @param block The suspend function to execute
     */
    protected fun safeLaunch(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            _isLoading.value = true
            _error.value = null
            try {
                block()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    /**
     * Combine a single data flow with loading/error into [UiState].
     *
     * ```kotlin
     * val state: StateFlow<UiState<List<Event>>> = uiStateFrom(getEventsUseCase())
     * ```
     */
    protected fun <T> uiStateFrom(
        dataFlow: StateFlow<T>
    ): StateFlow<UiState<T>> = combine(
        dataFlow, isLoading, error
    ) { data, loading, err ->
        when {
            loading -> UiState.Loading
            err != null -> UiState.Error(err)
            else -> UiState.Success(data)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    /**
     * Combine two data flows with loading/error into [UiState] with a transform.
     *
     * ```kotlin
     * val state: StateFlow<UiState<HomeData>> = uiStateFrom(
     *     getEventsUseCase(), getNewsUseCase()
     * ) { events, news -> HomeData(events, news) }
     * ```
     */
    protected fun <T1, T2, R> uiStateFrom(
        flow1: StateFlow<T1>,
        flow2: StateFlow<T2>,
        transform: (T1, T2) -> R
    ): StateFlow<UiState<R>> = combine(
        flow1, flow2, isLoading, error
    ) { data1, data2, loading, err ->
        when {
            loading -> UiState.Loading
            err != null -> UiState.Error(err)
            else -> UiState.Success(transform(data1, data2))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    /**
     * Combine three data flows with loading/error into [UiState] with a transform.
     *
     * ```kotlin
     * val state: StateFlow<UiState<DashboardData>> = uiStateFrom(
     *     getEventsUseCase(), getNewsUseCase(), getStatsUseCase()
     * ) { events, news, stats -> DashboardData(events, news, stats) }
     * ```
     */
    protected fun <T1, T2, T3, R> uiStateFrom(
        flow1: StateFlow<T1>,
        flow2: StateFlow<T2>,
        flow3: StateFlow<T3>,
        transform: (T1, T2, T3) -> R
    ): StateFlow<UiState<R>> = combine(
        flow1, flow2, flow3, isLoading, error
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val data1 = args[0] as T1
        @Suppress("UNCHECKED_CAST")
        val data2 = args[1] as T2
        @Suppress("UNCHECKED_CAST")
        val data3 = args[2] as T3
        val loading = args[3] as Boolean
        val err = args[4] as String?
        when {
            loading -> UiState.Loading
            err != null -> UiState.Error(err)
            else -> UiState.Success(transform(data1, data2, data3))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)
}
