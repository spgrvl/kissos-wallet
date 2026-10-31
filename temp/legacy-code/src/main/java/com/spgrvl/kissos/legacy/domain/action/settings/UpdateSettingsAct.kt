package com.spgrvl.kissos.legacy.domain.action.settings

import com.spgrvl.kissos.data.db.dao.write.WriteSettingsDao
import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.legacy.datamodel.Settings
import javax.inject.Inject

class UpdateSettingsAct @Inject constructor(
    private val writeSettingsDao: WriteSettingsDao
) : FPAction<Settings, Settings>() {
    override suspend fun Settings.compose(): suspend () -> Settings = suspend {
        writeSettingsDao.save(this.toEntity())
        this
    }
}
