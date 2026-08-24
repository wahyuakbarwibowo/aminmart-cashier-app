package com.wahyuakbarwibowo.aminmartkasir.data.remote

import android.content.Context
import com.wahyuakbarwibowo.aminmartkasir.data.remote.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

object AuthManager {

    sealed interface UiState {
        data object Loading : UiState
        data object SignedOut : UiState
        data class Authenticated(val profile: UserProfile) : UiState
    }

    private const val PREFS_NAME = "auth_session"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state

    private lateinit var prefs: android.content.SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!SupabaseProvider.isConfigured) {
            _state.value = UiState.SignedOut
            return
        }

        // Simpan refresh token setiap kali sesi diperbarui
        scope.launch {
            SupabaseProvider.client.auth.sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    prefs.edit()
                        .putString(KEY_REFRESH_TOKEN, status.session.refreshToken)
                        .apply()
                }
            }
        }

        restoreSession()
    }

    /** Coba pulihkan sesi terakhir lewat refresh token tersimpan. */
    private fun restoreSession() {
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null)
        if (refreshToken == null) {
            _state.value = UiState.SignedOut
            return
        }
        scope.launch {
            try {
                SupabaseProvider.client.auth.refreshSession(refreshToken)
                val userId = SupabaseProvider.client.auth.currentSessionOrNull()?.user?.id
                    ?: error("Sesi tidak valid")
                val profile = fetchProfile(userId)
                if (profile != null && profile.isActive) {
                    _state.value = UiState.Authenticated(profile)
                } else {
                    clearLocalSession()
                    _state.value = UiState.SignedOut
                }
            } catch (_: Exception) {
                // Offline / token kedaluwarsa -> minta login ulang
                _state.value = UiState.SignedOut
            }
        }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            SupabaseProvider.client.auth.signInWith(Email) {
                this.email = email.trim()
                this.password = password
            }
            val userId = SupabaseProvider.client.auth.currentSessionOrNull()?.user?.id
                ?: error("Sesi tidak valid")
            val profile = fetchProfile(userId)
                ?: run {
                    SupabaseProvider.client.auth.signOut()
                    clearLocalSession()
                    return Result.failure(IllegalStateException("Profil tidak ditemukan. Hubungi admin."))
                }
            if (!profile.isActive) {
                SupabaseProvider.client.auth.signOut()
                clearLocalSession()
                return Result.failure(IllegalStateException("Akun dinonaktifkan. Hubungi admin."))
            }
            _state.value = UiState.Authenticated(profile)
            Result.success(Unit)
        } catch (e: RestException) {
            Result.failure(Exception("Email atau password salah."))
        } catch (e: IOException) {
            Result.failure(Exception("Tidak ada koneksi internet."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        try {
            SupabaseProvider.client.auth.signOut()
        } catch (_: Exception) {
            // abaikan; tetap bersihkan sesi lokal
        }
        clearLocalSession()
        _state.value = UiState.SignedOut
    }

    private suspend fun fetchProfile(userId: String): UserProfile? {
        return SupabaseProvider.client.postgrest.from("profiles")
            .select { filter { eq("id", userId) } }
            .decodeList<UserProfile>()
            .firstOrNull()
    }

    private fun clearLocalSession() {
        prefs.edit().remove(KEY_REFRESH_TOKEN).apply()
    }
}
