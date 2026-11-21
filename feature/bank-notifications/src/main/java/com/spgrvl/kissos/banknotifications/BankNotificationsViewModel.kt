package com.spgrvl.kissos.banknotifications

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.spgrvl.kissos.banknotifications.parse.BankNotificationParsers
import com.spgrvl.kissos.base.time.TimeProvider
import com.spgrvl.kissos.data.db.dao.write.BankNotificationDao
import com.spgrvl.kissos.data.db.entity.DetectedPaymentStatus
import com.spgrvl.kissos.data.db.entity.UnparsedNotificationEntity
import com.spgrvl.kissos.data.model.Account
import com.spgrvl.kissos.data.repository.AccountRepository
import com.spgrvl.kissos.domain.banknotification.BankApp
import com.spgrvl.kissos.domain.banknotification.BankNotificationPrefs
import com.spgrvl.kissos.legacy.legacy.ui.theme.components.OptionUi
import com.spgrvl.kissos.ui.ComposeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@Immutable
data class BankNotificationsState(
    val enabled: Boolean,
    /** Whether the system lets Kissos read notifications; checked again whenever the screen resumes. */
    val accessGranted: Boolean,
    val apps: ImmutableList<BankAppUi>,
    /** Apps the user can add: the supported ones first. */
    val installedApps: ImmutableList<InstalledApp>,
    val accounts: ImmutableList<OptionUi>,
    val waitingCount: Int,
    val keepUnparsed: Boolean,
    val unparsed: ImmutableList<UnparsedUi>,
)

@Immutable
data class BankAppUi(
    val packageName: String,
    val label: String,
    val supported: Boolean,
    val account: UUID?,
    val accountName: String?,
    val autoAdd: Boolean,
)

@Immutable
data class InstalledApp(val packageName: String, val label: String, val supported: Boolean)

@Immutable
@Suppress("DataClassTypedIDs")
data class UnparsedUi(val id: UUID, val app: String, val text: String)

sealed interface BankNotificationsEvent {
    /** The user turned the feature on (after reading what it does) or off. */
    data class SetEnabled(val enabled: Boolean) : BankNotificationsEvent
    data object Resumed : BankNotificationsEvent
    data class AddApp(val packageName: String) : BankNotificationsEvent
    data class RemoveApp(val packageName: String) : BankNotificationsEvent
    data class SaveApp(val packageName: String, val account: UUID?, val autoAdd: Boolean) : BankNotificationsEvent
    data class SetKeepUnparsed(val keep: Boolean) : BankNotificationsEvent
}

@Stable
@HiltViewModel
class BankNotificationsViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val prefs: BankNotificationPrefs,
    private val dao: BankNotificationDao,
    private val accountRepository: AccountRepository,
    private val timeProvider: TimeProvider,
) : ComposeViewModel<BankNotificationsState, BankNotificationsEvent>() {
    private var accessGranted by mutableStateOf(false)
    private var accounts by mutableStateOf<List<Account>>(emptyList())
    private var installedApps by mutableStateOf<List<InstalledApp>>(emptyList())
    private val waitingCount = dao.observePayments(DetectedPaymentStatus.NEW).map { it.size }
    private val unparsedNotifications = dao.observeUnparsed()

    @Composable
    override fun uiState(): BankNotificationsState {
        LaunchedEffect(Unit) {
            accounts = accountRepository.findAll()
            installedApps = launcherApps()
        }
        val enabled by prefs.enabled.collectAsState(false)
        val apps by prefs.apps.collectAsState(emptyList())
        val keepUnparsed by prefs.keepUnparsed.collectAsState(false)
        val waiting by waitingCount.collectAsState(0)
        val unparsed by unparsedNotifications.collectAsState(emptyList())

        return BankNotificationsState(
            enabled = enabled,
            accessGranted = accessGranted,
            apps = apps.map(::toUi).toImmutableList(),
            installedApps = installedApps.filter { app -> apps.none { it.packageName == app.packageName } }
                .toImmutableList(),
            accounts = accounts.map { OptionUi(it.id.value, it.name.value, it.color.value) }.toImmutableList(),
            waitingCount = waiting,
            keepUnparsed = keepUnparsed,
            unparsed = unparsed.map(::toUi).toImmutableList(),
        )
    }

    override fun onEvent(event: BankNotificationsEvent) {
        viewModelScope.launch {
            when (event) {
                is BankNotificationsEvent.SetEnabled -> setEnabled(event.enabled)
                BankNotificationsEvent.Resumed -> {
                    accessGranted = ListenerAccess.isGranted(context)
                    if (prefs.enabled.first()) ListenerAccess.rebind(context)
                }
                is BankNotificationsEvent.AddApp -> prefs.updateApps {
                    it + BankApp(
                        packageName = event.packageName,
                        accountId = null,
                        autoAdd = false,
                        addedAt = timeProvider.utcNow().toEpochMilli(),
                    )
                }
                is BankNotificationsEvent.RemoveApp -> prefs.updateApps { apps ->
                    apps.filterNot { it.packageName == event.packageName }
                }

                is BankNotificationsEvent.SaveApp -> prefs.updateApps { apps ->
                    apps.map {
                        if (it.packageName == event.packageName) {
                            it.copy(accountId = event.account?.toString(), autoAdd = event.autoAdd)
                        } else {
                            it
                        }
                    }
                }

                is BankNotificationsEvent.SetKeepUnparsed -> {
                    prefs.setKeepUnparsed(event.keep)
                    if (!event.keep) dao.deleteAllUnparsed()
                }
            }
        }
    }

    private suspend fun setEnabled(enabled: Boolean) {
        prefs.setEnabled(enabled, timeProvider.utcNow())
        if (enabled) {
            ListenerAccess.startListening(context)
        } else {
            prefs.setKeepUnparsed(false)
            dao.deleteAllUnparsed()
        }
    }

    private fun toUi(app: BankApp): BankAppUi {
        val account = accounts.find { it.id.value == app.account }
        return BankAppUi(
            packageName = app.packageName,
            label = appLabel(app.packageName),
            supported = app.packageName in BankNotificationParsers.supportedPackages,
            account = account?.id?.value,
            accountName = account?.name?.value,
            autoAdd = app.autoAdd,
        )
    }

    private fun toUi(entity: UnparsedNotificationEntity): UnparsedUi = UnparsedUi(
        id = entity.id,
        app = appLabel(entity.sourcePackage),
        text = listOfNotNull(entity.title, entity.text).joinToString("\n"),
    )

    private fun appLabel(packageName: String): String = try {
        val packageManager = context.packageManager
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        packageName
    }

    private fun launcherApps(): List<InstalledApp> {
        val packageManager = context.packageManager
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(launcher, 0)
            .map { it.activityInfo.packageName to it.loadLabel(packageManager).toString() }
            .filter { (packageName, _) -> packageName != context.packageName }
            .distinctBy { it.first }
            .map { (packageName, label) ->
                InstalledApp(packageName, label, packageName in BankNotificationParsers.supportedPackages)
            }
            .sortedWith(compareBy<InstalledApp> { !it.supported }.thenBy { it.label.lowercase() })
    }
}

internal val previewState = BankNotificationsState(
    enabled = true,
    accessGranted = true,
    apps = persistentListOf(
        BankAppUi("com.revolut.revolut", "Revolut", true, null, "Revolut", true),
        BankAppUi("gr.payzy", "Magenta Pay", true, null, null, false),
    ),
    installedApps = persistentListOf(),
    accounts = persistentListOf(),
    waitingCount = 2,
    keepUnparsed = false,
    unparsed = persistentListOf(),
)
