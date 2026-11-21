package com.spgrvl.kissos.domain.banknotification

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** A bank app whose notifications the user lets Kissos read. */
@Suppress("DataClassDefaultValues")
@Serializable
data class BankApp(
    @SerialName("packageName")
    val packageName: String,
    /** The account its payments go to; the user picks one when adding a payment if it's null. */
    @SerialName("accountId")
    val accountId: String? = null,
    /** Adds its payments right away, with Undo, instead of asking first. */
    @SerialName("autoAdd")
    val autoAdd: Boolean = false,
    /** When the user picked it, in epoch millis: its notifications from before aren't read. */
    @SerialName("addedAt")
    val addedAt: Long = 0,
) {
    val account: UUID? get() = accountId?.let { runCatching { UUID.fromString(it) }.getOrNull() }
}

/** Kept on the device only: not in backups, since it depends on which apps are installed. */
@Singleton
class BankNotificationPrefs @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val json = Json { ignoreUnknownKeys = true }

    val enabled: Flow<Boolean> = dataStore.data.map { it[ENABLED] ?: false }.distinctUntilChanged()

    val apps: Flow<List<BankApp>> = dataStore.data.map { decode(it[APPS]) }.distinctUntilChanged()

    /**
     * The apps the listener may read, each with the time (epoch millis) its notifications are read from:
     * when the user picked it or turned the feature on, whichever is later. None while the feature is off.
     */
    val allowedPackages: Flow<Map<String, Long>> = dataStore.data
        .map { prefs ->
            if (prefs[ENABLED] == true) {
                val enabledAt = prefs[ENABLED_AT] ?: 0
                decode(prefs[APPS]).associate { it.packageName to maxOf(it.addedAt, enabledAt) }
            } else {
                emptyMap()
            }
        }
        .distinctUntilChanged()

    /** Whether to keep the notifications no parser understood, for a week, so the user can report them. */
    val keepUnparsed: Flow<Boolean> = dataStore.data.map { it[KEEP_UNPARSED] ?: false }.distinctUntilChanged()

    /** @param now so that notifications from before turning it on aren't read, e.g. those still shown. */
    suspend fun setEnabled(enabled: Boolean, now: Instant) {
        dataStore.edit {
            it[ENABLED] = enabled
            if (enabled) it[ENABLED_AT] = now.toEpochMilli()
        }
    }

    suspend fun setKeepUnparsed(keep: Boolean) {
        dataStore.edit { it[KEEP_UNPARSED] = keep }
    }

    suspend fun updateApps(transform: (List<BankApp>) -> List<BankApp>) {
        dataStore.edit { prefs ->
            prefs[APPS] = json.encodeToString(transform(decode(prefs[APPS])).distinctBy(BankApp::packageName))
        }
    }

    private fun decode(value: String?): List<BankApp> = try {
        value?.let { json.decodeFromString<List<BankApp>>(it) }.orEmpty()
    } catch (e: SerializationException) {
        emptyList()
    } catch (e: IllegalArgumentException) {
        emptyList()
    }

    private companion object {
        val ENABLED = booleanPreferencesKey("bank_notifications_enabled")
        val APPS = stringPreferencesKey("bank_notifications_apps")
        val KEEP_UNPARSED = booleanPreferencesKey("bank_notifications_keep_unparsed")
        val ENABLED_AT = longPreferencesKey("bank_notifications_enabled_at")
    }
}
