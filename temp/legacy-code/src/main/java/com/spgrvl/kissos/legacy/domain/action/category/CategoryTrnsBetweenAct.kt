package com.spgrvl.kissos.wallet.domain.action.category

import com.spgrvl.kissos.base.legacy.Transaction
import com.spgrvl.kissos.data.db.dao.read.TransactionDao
import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.frp.action.thenMap
import com.spgrvl.kissos.legacy.datamodel.temp.toLegacyDomain
import com.spgrvl.kissos.wallet.domain.pure.data.ClosedTimeRange
import java.util.UUID
import javax.inject.Inject

class CategoryTrnsBetweenAct @Inject constructor(
    private val transactionDao: TransactionDao
) : FPAction<CategoryTrnsBetweenAct.Input, List<Transaction>>() {

    override suspend fun Input.compose(): suspend () -> List<Transaction> = suspend {
        io {
            transactionDao.findAllByCategoryAndBetween(
                startDate = between.from,
                endDate = between.to,
                categoryId = categoryId
            )
        }
    } thenMap { it.toLegacyDomain() }

    data class Input(
        val categoryId: UUID,
        val between: ClosedTimeRange
    )
}

fun actInput(
    categoryId: UUID,
    between: ClosedTimeRange
) = CategoryTrnsBetweenAct.Input(
    categoryId = categoryId,
    between = between
)
