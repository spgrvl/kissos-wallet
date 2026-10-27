package com.spgrvl.kissos.wallet.domain.action.transaction

import com.spgrvl.kissos.base.legacy.Transaction
import com.spgrvl.kissos.data.model.TransactionId
import com.spgrvl.kissos.data.repository.TransactionRepository
import com.spgrvl.kissos.data.repository.mapper.TransactionMapper
import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.frp.then
import com.spgrvl.kissos.legacy.datamodel.temp.toLegacy
import java.util.UUID
import javax.inject.Inject

class TrnByIdAct @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val mapper: TransactionMapper
) : FPAction<UUID, Transaction?>() {
    override suspend fun UUID.compose(): suspend () -> Transaction? = suspend {
        this // transactionId
    } then {
        transactionRepo.findById(TransactionId(it))
    } then {
        it?.toLegacy(mapper)
    }
}
