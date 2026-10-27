package com.spgrvl.kissos.wallet.domain.action.viewmodel.home

import com.spgrvl.kissos.data.model.Category
import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.legacy.IvyWalletCtx
import javax.inject.Inject

class UpdateCategoriesCacheAct @Inject constructor(
    private val ivyWalletCtx: IvyWalletCtx
) : FPAction<List<Category>, List<Category>>() {
    override suspend fun List<Category>.compose(): suspend () -> List<Category> = suspend {
        val categories = this

        ivyWalletCtx.categoryMap.clear()
        ivyWalletCtx.categoryMap.putAll(categories.map { it.id.value to it })

        categories
    }
}
