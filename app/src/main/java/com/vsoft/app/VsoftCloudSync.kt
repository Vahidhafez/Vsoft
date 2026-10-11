package com.vsoft.app

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Keeps Vsoft's string-backed DataStore preferences in sync for all devices
 * authenticated as the same Firebase user. Local edits are debounced and
 * remote payloads are applied atomically to avoid partially restored state.
 */
class VsoftCloudSync(
    context: Context,
    private val userId: String,
    private val onStatus: (String) -> Unit
) {
    private val appContext = context.applicationContext
    private val firestore = FirebaseFirestore.getInstance()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var listener: ListenerRegistration? = null
    private var collector: Job? = null
    @Volatile private var remoteReady = false
    @Volatile private var applyingRemote = false
    @Volatile private var lastCloudPayload: Map<String, String>? = null
    @Volatile private var lastCloudUpdatedAt = 0L
    @Volatile private var pendingLocalPayload: Map<String, String>? = null

    fun start() {
        onStatus("در حال اتصال به فضای همگام‌سازی…")
        listener = firestore.collection("users")
            .document(userId)
            .collection("sync")
            .document("vsoft")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onStatus("خطای همگام‌سازی: ${error.localizedMessage ?: "اتصال ناموفق"}")
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                val cloudPayload = (snapshot.get("payload") as? Map<*, *>)
                    ?.entries
                    ?.mapNotNull { (key, value) ->
                        if (key is String && value is String) key to value else null
                    }
                    ?.toMap()

                if (!snapshot.exists() || cloudPayload == null) {
                    remoteReady = true
                    onStatus("در حال بارگذاری اطلاعات این گوشی…")
                    scope.launch { uploadCurrentLocalData(force = true) }
                    return@addSnapshotListener
                }

                val updatedAt = snapshot.getLong("updatedAt") ?: 0L
                if (updatedAt >= lastCloudUpdatedAt && cloudPayload != lastCloudPayload) {
                    lastCloudUpdatedAt = updatedAt
                    lastCloudPayload = cloudPayload
                    remoteReady = true
                    scope.launch {
                        applyingRemote = true
                        try {
                            appContext.dataStore.edit { preferences ->
                                preferences.clear()
                                cloudPayload.forEach { (key, value) ->
                                    preferences[stringPreferencesKey(key)] = value
                                }
                            }
                            pendingLocalPayload = cloudPayload
                            onStatus("همگام‌سازی انجام شد")
                        } catch (t: Throwable) {
                            onStatus("دریافت اطلاعات ناموفق بود: ${t.localizedMessage ?: "خطای ناشناخته"}")
                        } finally {
                            delay(500)
                            applyingRemote = false
                        }
                    }
                } else {
                    remoteReady = true
                    onStatus("همگام‌سازی فعال است")
                }
            }

        collector = scope.launch {
            appContext.dataStore.data
                .map(::stringPayload)
                .distinctUntilChanged()
                .collectLatest { payload ->
                    pendingLocalPayload = payload
                    delay(900)
                    if (remoteReady && !applyingRemote) upload(payload)
                }
        }
    }

    private fun stringPayload(preferences: Preferences): Map<String, String> =
        preferences.asMap().mapNotNull { (key, value) ->
            (value as? String)?.let { key.name to it }
        }.toMap()

    private suspend fun uploadCurrentLocalData(force: Boolean) {
        val payload = stringPayload(appContext.dataStore.data.first())
        if (force) {
            lastCloudPayload = null
            upload(payload)
        }
    }

    private fun upload(payload: Map<String, String>) {
        if (applyingRemote || payload == lastCloudPayload) return
        val updatedAt = System.currentTimeMillis().coerceAtLeast(lastCloudUpdatedAt + 1)
        firestore.collection("users")
            .document(userId)
            .collection("sync")
            .document("vsoft")
            .set(mapOf(
                "payload" to payload,
                "updatedAt" to updatedAt,
                "schemaVersion" to 1
            ))
            .addOnSuccessListener {
                lastCloudUpdatedAt = updatedAt
                lastCloudPayload = payload
                onStatus("همگام‌سازی انجام شد")
            }
            .addOnFailureListener { error ->
                onStatus("ذخیره ابری انجام نشد: ${error.localizedMessage ?: "قوانین دسترسی را بررسی کنید"}")
            }
    }

    fun stop() {
        listener?.remove()
        listener = null
        collector?.cancel()
        collector = null
        scope.cancel()
    }
}
