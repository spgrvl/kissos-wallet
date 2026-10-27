package com.spgrvl.kissos.data.backup

import androidx.annotation.Keep
import com.spgrvl.kissos.data.db.entity.AccountEntity
import com.spgrvl.kissos.data.db.entity.BudgetEntity
import com.spgrvl.kissos.data.db.entity.CategoryEntity
import com.spgrvl.kissos.data.db.entity.LoanEntity
import com.spgrvl.kissos.data.db.entity.LoanRecordEntity
import com.spgrvl.kissos.data.db.entity.MerchantEntity
import com.spgrvl.kissos.data.db.entity.PlannedPaymentRuleEntity
import com.spgrvl.kissos.data.db.entity.ReceiptEntity
import com.spgrvl.kissos.data.db.entity.SettingsEntity
import com.spgrvl.kissos.data.db.entity.TagAssociationEntity
import com.spgrvl.kissos.data.db.entity.TagEntity
import com.spgrvl.kissos.data.db.entity.TransactionEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@Suppress("DataClassDefaultValues")
data class IvyWalletCompleteData(
    @SerialName("accounts")
    val accounts: List<AccountEntity> = emptyList(),
    @SerialName("budgets")
    val budgets: List<BudgetEntity> = emptyList(),
    @SerialName("categories")
    val categories: List<CategoryEntity> = emptyList(),
    @SerialName("loanRecords")
    val loanRecords: List<LoanRecordEntity> = emptyList(),
    @SerialName("loans")
    val loans: List<LoanEntity> = emptyList(),
    @SerialName("plannedPaymentRules")
    val plannedPaymentRules: List<PlannedPaymentRuleEntity> = emptyList(),
    @SerialName("settings")
    val settings: List<SettingsEntity> = emptyList(),
    @SerialName("transactions")
    val transactions: List<TransactionEntity> = emptyList(),
    @SerialName("sharedPrefs")
    val sharedPrefs: HashMap<String, String> = HashMap(),
    @SerialName("tags")
    val tags: List<TagEntity> = emptyList(),
    @SerialName("tagAssociations")
    val tagAssociations: List<TagAssociationEntity> = emptyList(),
    // Kissos only. Missing from Ivy Wallet backups.
    @SerialName("receipts")
    val receipts: List<ReceiptEntity> = emptyList(),
    @SerialName("merchants")
    val merchants: List<MerchantEntity> = emptyList(),
)
