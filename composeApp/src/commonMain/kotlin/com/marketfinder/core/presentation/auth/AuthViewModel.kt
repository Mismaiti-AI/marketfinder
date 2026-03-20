package com.marketfinder.core.presentation.auth

import com.marketfinder.core.data.auth.AuthRepository
import com.marketfinder.core.data.auth.SocialSignInResult
import com.marketfinder.core.data.local.AppSettings
import com.marketfinder.core.data.local.model.UserEntity
import com.marketfinder.core.presentation.BaseViewModel
import com.marketfinder.core.presentation.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Pre-built Auth ViewModel — glue layer between AuthRepository and GenericAuthScreen.
 *
 * Manages form state, social sign-in, and session persistence via AppSettings.
 *
 * - [formState] — form fields only (email, password, name, mode toggle)
 * - [uiState] — screen-level state via generic [UiState] from BaseViewModel
 * - [isLoading] / [error] — inherited from BaseViewModel, managed by [safeLaunch]
 *
 * Usage in AppModule.kt:
 * ```kotlin
 * viewModelOf(::AuthViewModel)
 * ```
 *
 * Usage in App.kt:
 * ```kotlin
 * val authViewModel: AuthViewModel = koinViewModel()
 * AuthScreenContent(
 *     authViewModel = authViewModel,
 *     socialButtons = listOf(AuthSocialButton("Continue with Google", Icons.Default.Email)),
 *     onLoginSuccess = { appState = AppState.Home }
 * )
 * ```
 */

data class AuthFormState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val confirmPassword: String = "",
    val isLogin: Boolean = true
)

object AuthSettingsKeys {
    const val IS_LOGGED_IN = "is_logged_in"
    const val LOGGED_IN_USER_ID = "logged_in_user_id"
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val appSettings: AppSettings
) : BaseViewModel() {

    private val _formState = MutableStateFlow(AuthFormState())
    val formState: StateFlow<AuthFormState> = _formState.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<UserEntity>>(UiState.Loading)
    val uiState: StateFlow<UiState<UserEntity>> = _uiState.asStateFlow()

    val isLoggedIn: Boolean
        get() = appSettings.getBoolean(AuthSettingsKeys.IS_LOGGED_IN, false)

    fun onEmailChange(email: String) {
        _formState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _formState.update { it.copy(password = password) }
    }

    fun onNameChange(name: String) {
        _formState.update { it.copy(name = name) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _formState.update { it.copy(confirmPassword = confirmPassword) }
    }

    fun onToggleMode() {
        clearError()
        _formState.update { it.copy(isLogin = !it.isLogin) }
    }

    fun signInWithSocial() = safeLaunch {
        when (val result = authRepository.signInWithSocial()) {
            is SocialSignInResult.Success -> {
                appSettings.putBoolean(AuthSettingsKeys.IS_LOGGED_IN, true)
                appSettings.putInt(AuthSettingsKeys.LOGGED_IN_USER_ID, result.user.id)
                _uiState.value = UiState.Success(result.user)
            }
            is SocialSignInResult.Cancelled -> {
                // No-op — safeLaunch resets isLoading automatically
            }
            is SocialSignInResult.Error -> {
                throw IllegalStateException(result.message)
            }
        }
    }

    fun signInWithEmail() = safeLaunch {
        throw IllegalStateException("Email sign-in not yet implemented")
    }

    fun logout() {
        appSettings.putBoolean(AuthSettingsKeys.IS_LOGGED_IN, false)
        appSettings.remove(AuthSettingsKeys.LOGGED_IN_USER_ID)
        _formState.value = AuthFormState()
        _uiState.value = UiState.Loading
        clearError()
    }
}
