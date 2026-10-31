package com.spgrvl.kissos.wallet.domain.action.settings

import com.spgrvl.kissos.base.legacy.Theme
import com.spgrvl.kissos.data.db.dao.read.SettingsDao
import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.frp.then
import com.spgrvl.kissos.legacy.datamodel.Settings
import com.spgrvl.kissos.legacy.datamodel.temp.toLegacyDomain
import javax.inject.Inject

class SettingsAct @Inject constructor(
    private val settingsDao: SettingsDao
) : FPAction<Unit, Settings>() {
    override suspend fun Unit.compose(): suspend () -> Settings = suspend {
        io { settingsDao.findFirst() }
    } then { it.toLegacyDomain() }

    suspend fun getSettingsWithNextTheme(): Settings {
        val currentSettings = this(Unit)
        val newTheme = when (currentSettings.theme) {
            Theme.LIGHT -> Theme.DARK
            Theme.DARK -> Theme.AMOLED_DARK
            Theme.AMOLED_DARK -> Theme.AUTO
            Theme.AUTO -> Theme.LIGHT
        }
        return currentSettings.copy(theme = newTheme)
    }

    suspend fun getSettings(): Settings = this(Unit)
}