package com.evangelidisapps.ttworkoutlog.ui.auth

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.evangelidisapps.ttworkoutlog.data.repository.UserPreferencesRepository
import com.evangelidisapps.ttworkoutlog.data.repository.WorkoutRepository
import com.facebook.AccessToken
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val isSignedIn: Boolean = false,
    val isGuest: Boolean = false,
    val displayName: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val syncMessage: String? = null
)

class AuthViewModel(application: Application) : ViewModel() {

    private val auth: FirebaseAuth? = runCatching { Firebase.auth }.getOrNull()
    private val analytics = runCatching { FirebaseAnalytics.getInstance(application) }.getOrNull()
    private val prefs = UserPreferencesRepository(application)
    private val workoutRepo = WorkoutRepository.create(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val hasOnboarded = prefs.hasCompletedOnboarding.first()
            val isGuest = prefs.isGuest.first()
            when {
                hasOnboarded && isGuest -> _uiState.value = AuthUiState(
                    isSignedIn = true, isGuest = true, displayName = "Guest", isLoading = false
                )
                hasOnboarded && auth?.currentUser != null -> _uiState.value = AuthUiState(
                    isSignedIn = true, isGuest = false,
                    displayName = auth.currentUser?.displayName ?: auth.currentUser?.email,
                    isLoading = false
                )
                else -> _uiState.value = AuthUiState(isLoading = false)
            }
        }
    }

    // ── Email / Password ─────────────────────────────────────────────────────

    fun signInWithEmail(email: String, password: String) {
        val authInstance = auth ?: return setFirebaseError()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { authInstance.signInWithEmailAndPassword(email, password).await() }
                .onSuccess { result ->
                    analytics?.logEvent(FirebaseAnalytics.Event.LOGIN) {
                        param(FirebaseAnalytics.Param.METHOD, "email")
                    }
                    result.user?.let { onSignInSuccess(it) }
                }
                .onFailure { setError(it) }
        }
    }

    fun registerWithEmail(email: String, password: String, displayName: String) {
        val authInstance = auth ?: return setFirebaseError()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val result = authInstance.createUserWithEmailAndPassword(email, password).await()
                if (displayName.isNotBlank()) {
                    result.user?.updateProfile(
                        UserProfileChangeRequest.Builder().setDisplayName(displayName).build()
                    )?.await()
                }
                result
            }
                .onSuccess { result ->
                    analytics?.logEvent(FirebaseAnalytics.Event.SIGN_UP) {
                        param(FirebaseAnalytics.Param.METHOD, "email")
                    }
                    result.user?.let { onSignInSuccess(it) }
                }
                .onFailure { setError(it) }
        }
    }

    // ── Google ───────────────────────────────────────────────────────────────

    fun signInWithGoogle(idToken: String) {
        val authInstance = auth ?: return setFirebaseError()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            runCatching { authInstance.signInWithCredential(credential).await() }
                .onSuccess { result ->
                    val event = if (result.additionalUserInfo?.isNewUser == true)
                        FirebaseAnalytics.Event.SIGN_UP else FirebaseAnalytics.Event.LOGIN
                    analytics?.logEvent(event) {
                        param(FirebaseAnalytics.Param.METHOD, "google")
                    }
                    result.user?.let { onSignInSuccess(it) }
                }
                .onFailure { setError(it) }
        }
    }

    // ── Facebook ─────────────────────────────────────────────────────────────

    fun signInWithFacebook(accessToken: AccessToken) {
        val authInstance = auth ?: return setFirebaseError()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val credential = FacebookAuthProvider.getCredential(accessToken.token)
            runCatching { authInstance.signInWithCredential(credential).await() }
                .onSuccess { result ->
                    val event = if (result.additionalUserInfo?.isNewUser == true)
                        FirebaseAnalytics.Event.SIGN_UP else FirebaseAnalytics.Event.LOGIN
                    analytics?.logEvent(event) {
                        param(FirebaseAnalytics.Param.METHOD, "facebook")
                    }
                    result.user?.let { onSignInSuccess(it) }
                }
                .onFailure { setError(it) }
        }
    }

    // ── Guest ────────────────────────────────────────────────────────────────

    fun continueAsGuest() {
        analytics?.logEvent("guest_session_started", null)
        viewModelScope.launch {
            prefs.setOnboardingCompleted(isGuest = true)
            _uiState.value = AuthUiState(
                isSignedIn = true, isGuest = true, displayName = "Guest", isLoading = false
            )
        }
    }

    // ── Sign out ─────────────────────────────────────────────────────────────

    fun signOut() {
        viewModelScope.launch {
            auth?.signOut()
            prefs.clearOnboarding()
            _uiState.value = AuthUiState(isLoading = false)
        }
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private suspend fun onSignInSuccess(user: FirebaseUser) {
        if (_uiState.value.isGuest) {
            analytics?.logEvent("guest_converted", null)
        }
        prefs.setOnboardingCompleted(isGuest = false)

        // Upload any local guest data to Firestore
        _uiState.update { it.copy(syncMessage = "Uploading local data...") }
        runCatching { workoutRepo.uploadLocalToFirestore() }

        // Delete all local data — Firestore is now the source of truth
        _uiState.update { it.copy(syncMessage = "Transferring data...") }
        runCatching { workoutRepo.clearAllLocalWorkouts() }

        // Fetch all remote workouts into Room
        _uiState.update { it.copy(syncMessage = "Syncing your workouts...") }
        val synced = runCatching { workoutRepo.syncFromFirestore() }.getOrElse { 0 }

        _uiState.update {
            it.copy(
                isSignedIn = true,
                isGuest = false,
                displayName = user.displayName ?: user.email ?: user.uid,
                isLoading = false,
                syncMessage = if (synced > 0) "Synced $synced workouts" else null
            )
        }
    }

    private fun setFirebaseError() {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = "Firebase not configured. Add google-services.json."
            )
        }
    }

    private fun setError(e: Throwable) {
        _uiState.update {
            it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Authentication failed")
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AuthViewModel(application) as T
            }
    }
}
