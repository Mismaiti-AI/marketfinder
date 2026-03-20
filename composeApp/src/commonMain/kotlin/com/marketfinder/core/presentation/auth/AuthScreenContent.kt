package com.marketfinder.core.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.marketfinder.core.data.local.model.UserEntity
import com.marketfinder.core.presentation.UiState
import com.marketfinder.core.presentation.screens.GenericAuthScreen
import com.marketfinder.core.presentation.screens.SocialButton

/**
 * Configuration for a social login button.
 * Unlike [SocialButton], onClick is auto-wired to [AuthViewModel.signInWithSocial].
 *
 * ```kotlin
 * AuthSocialButton("Continue with Google", Icons.Default.Email)
 * ```
 */
data class AuthSocialButton(
    val label: String,
    val icon: ImageVector? = null
)

/**
 * Pre-built Auth Screen wrapper — auto-wires [AuthViewModel] to [GenericAuthScreen].
 *
 * Collects formState/uiState from the ViewModel, maps [AuthSocialButton]s to
 * [SocialButton]s with onClick wired to signInWithSocial(), and fires
 * [onLoginSuccess] via LaunchedEffect when auth succeeds.
 *
 * Usage:
 * ```kotlin
 * composable<Routes.Login> {
 *     val authViewModel: AuthViewModel = koinViewModel()
 *     AuthScreenContent(
 *         authViewModel = authViewModel,
 *         socialButtons = listOf(
 *             AuthSocialButton("Continue with Google", Icons.Default.Email)
 *         ),
 *         onLoginSuccess = { user -> appState = AppState.Home }
 *     )
 * }
 * ```
 */
@Composable
fun AuthScreenContent(
    authViewModel: AuthViewModel,
    socialButtons: List<AuthSocialButton> = emptyList(),
    onLoginSuccess: (UserEntity) -> Unit,
    onToggleMode: (() -> Unit)? = null,
    onForgotPassword: (() -> Unit)? = null,
    headerContent: (@Composable () -> Unit)? = null
) {
    val formState by authViewModel.formState.collectAsState()
    val uiState by authViewModel.uiState.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()

    // Fire onLoginSuccess when auth succeeds
    LaunchedEffect(uiState) {
        val state = uiState
        if (state is UiState.Success) {
            onLoginSuccess(state.data)
        }
    }

    // Map AuthSocialButtons to SocialButtons with auto-wired onClick
    val wiredSocialButtons = socialButtons.map { authButton ->
        SocialButton(
            label = authButton.label,
            icon = authButton.icon,
            onClick = { authViewModel.signInWithSocial() }
        )
    }

    GenericAuthScreen(
        isLogin = formState.isLogin,
        email = formState.email,
        password = formState.password,
        onEmailChange = authViewModel::onEmailChange,
        onPasswordChange = authViewModel::onPasswordChange,
        onSubmit = { authViewModel.signInWithEmail() },
        onToggleMode = onToggleMode ?: { authViewModel.onToggleMode() },
        isLoading = isLoading,
        error = error,
        onForgotPassword = onForgotPassword,
        socialButtons = wiredSocialButtons,
        showName = !formState.isLogin,
        name = formState.name,
        onNameChange = authViewModel::onNameChange,
        showConfirmPassword = !formState.isLogin,
        confirmPassword = formState.confirmPassword,
        onConfirmPasswordChange = authViewModel::onConfirmPasswordChange,
        headerContent = headerContent
    )
}
