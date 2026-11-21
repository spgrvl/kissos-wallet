package com.spgrvl.kissos.domain.banknotification

import com.spgrvl.kissos.base.time.TimeProvider
import com.spgrvl.kissos.data.db.dao.write.BankNotificationDao
import com.spgrvl.kissos.data.db.entity.CategoryHintMemoryEntity
import com.spgrvl.kissos.data.db.entity.DetectedPaymentEntity
import com.spgrvl.kissos.data.db.entity.DetectedPaymentStatus
import com.spgrvl.kissos.data.db.entity.PayeeMemoryEntity
import com.spgrvl.kissos.data.model.Account
import com.spgrvl.kissos.data.model.AccountId
import com.spgrvl.kissos.data.model.CategoryId
import com.spgrvl.kissos.data.model.Expense
import com.spgrvl.kissos.data.model.PositiveValue
import com.spgrvl.kissos.data.model.TransactionId
import com.spgrvl.kissos.data.model.TransactionMetadata
import com.spgrvl.kissos.data.model.primitive.NotBlankTrimmedString
import com.spgrvl.kissos.data.model.primitive.PositiveDouble
import com.spgrvl.kissos.data.repository.AccountRepository
import com.spgrvl.kissos.data.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/** What adding a detected payment fills in, from what Kissos learned about its payee and the bank's category. */
@Suppress("DataClassTypedIDs")
data class PaymentDraft(
    val payment: DetectedPaymentEntity,
    val title: String?,
    val categoryId: CategoryId?,
    /** The account the app's payments go to; null if the user hasn't picked one. */
    val account: Account?,
    /** The account already has an expense of the same amount around then, e.g. from a scanned receipt. */
    val possibleDuplicate: Boolean,
) {
    /** Whether it can be added without the user looking at it first. */
    val canAddDirectly: Boolean
        get() = account != null && account.asset.code == payment.currency &&
            payment.direction == OUT && !possibleDuplicate

    companion object {
        const val OUT = "OUT"
    }
}

/** Turns detected payments into expenses, or sets them aside, and learns from what the user changes. */
@Singleton
class DetectedPayments @Inject constructor(
    private val dao: BankNotificationDao,
    private val prefs: BankNotificationPrefs,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val timeProvider: TimeProvider,
) {
    suspend fun find(id: UUID): DetectedPaymentEntity? = dao.findPayment(id)

    /** The detected payment an expense was added from, if it was. */
    suspend fun findByTransaction(transactionId: UUID): DetectedPaymentEntity? =
        dao.findPaymentByTransaction(transactionId)

    suspend fun draft(payment: DetectedPaymentEntity): PaymentDraft {
        val app = prefs.apps.first().find { it.packageName == payment.sourcePackage }
        val account = app?.account?.let { accountRepository.findById(AccountId(it)) }
        val payee = payment.payee?.let { dao.findPayee(normalisedPayee(it)) }
        val hintCategory = payment.categoryHint
            ?.let { dao.findCategoryHint(payment.sourcePackage, it) }
            ?.categoryId
        return PaymentDraft(
            payment = payment,
            title = payee?.title ?: payment.payee,
            categoryId = (payee?.categoryId ?: hintCategory)?.let(::CategoryId),
            account = account,
            possibleDuplicate = account != null && hasSimilarExpense(account.id, payment),
        )
    }

    /** @return false if it was already handled, or it needs the user to look at it first. */
    suspend fun add(id: UUID): Boolean {
        val draft = dao.findPayment(id)?.takeIf { it.status == DetectedPaymentStatus.NEW }?.let { draft(it) }
        val account = draft?.account?.takeIf { draft.canAddDirectly }
        val amount = draft?.payment?.amount?.let { PositiveDouble.from(it).getOrNull() }
        if (account == null || amount == null) return false
        val payment = draft.payment
        val transactionId = TransactionId(UUID.randomUUID())
        transactionRepository.save(
            Expense(
                id = transactionId,
                title = draft.title?.let { NotBlankTrimmedString.from(it).getOrNull() },
                description = null,
                category = draft.categoryId,
                time = payment.postedAt,
                settled = true,
                metadata = TransactionMetadata(recurringRuleId = null, paidForDateTime = null, loanRecordId = null),
                tags = emptyList(),
                value = PositiveValue(amount, account.asset),
                account = account.id,
            )
        )
        dao.setStatus(id, DetectedPaymentStatus.ADDED, transactionId.value)
        return true
    }

    /** The user saved it themselves, e.g. after changing it in the expense screen. */
    suspend fun markAdded(id: UUID, transactionId: UUID) {
        dao.setStatus(id, DetectedPaymentStatus.ADDED, transactionId)
    }

    suspend fun dismiss(id: UUID) {
        dao.setStatus(id, DetectedPaymentStatus.DISMISSED, null)
    }

    /** Removes the expense it became and puts it back for review. */
    suspend fun undo(id: UUID) {
        val payment = dao.findPayment(id) ?: return
        payment.transactionId?.let { transactionRepository.deleteById(TransactionId(it)) }
        dao.setStatus(id, DetectedPaymentStatus.NEW, null)
    }

    /**
     * Remembers what the user called the payee and which category they chose, for the payee and for the bank's
     * own category, so the next payment like it is filled in the same way.
     */
    suspend fun learn(payment: DetectedPaymentEntity, title: String?, categoryId: UUID?) {
        val now = timeProvider.utcNow()
        val payee = payment.payee
        val name = title?.trim()?.ifEmpty { null }
        if (payee != null && name != null) {
            dao.savePayee(PayeeMemoryEntity(normalisedPayee(payee), name, categoryId, now))
        }
        val hint = payment.categoryHint
        if (hint != null && categoryId != null) {
            dao.saveCategoryHint(CategoryHintMemoryEntity(payment.sourcePackage, hint, categoryId, now))
        }
    }

    private suspend fun hasSimilarExpense(account: AccountId, payment: DetectedPaymentEntity): Boolean =
        transactionRepository.findAllByAccountAndBetween(
            accountId = account,
            startDate = payment.postedAt - DUPLICATE_WINDOW,
            endDate = payment.postedAt + DUPLICATE_WINDOW,
        ).any { it is Expense && abs(it.value.amount.value - payment.amount) < CENT / 2 }

    companion object {
        private val DUPLICATE_WINDOW: Duration = Duration.ofDays(1)
        private const val CENT = 0.01

        /** "POWERCO ", "PowerCo" and "powerco" are the same payee. */
        fun normalisedPayee(payee: String): String = payee.trim().replace(Regex("\\s+"), " ").lowercase()
    }
}
