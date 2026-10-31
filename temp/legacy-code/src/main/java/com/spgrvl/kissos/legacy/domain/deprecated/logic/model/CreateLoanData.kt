package com.spgrvl.kissos.wallet.domain.deprecated.logic.model

import androidx.compose.ui.graphics.Color
import com.spgrvl.kissos.data.model.LoanType
import com.spgrvl.kissos.legacy.datamodel.Account
import java.time.LocalDateTime

data class CreateLoanData(
    val name: String,
    val amount: Double,
    val type: LoanType,
    val color: Color,
    val icon: String?,
    val account: Account? = null,
    val note: String?,
    val createLoanTransaction: Boolean = false,
    val dateTime: LocalDateTime
)
