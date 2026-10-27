package com.spgrvl.kissos.home.customerjourney

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.spgrvl.kissos.base.time.TimeProvider
import com.spgrvl.kissos.design.l0_system.Gradient
import com.spgrvl.kissos.domain.RootScreen
import com.spgrvl.kissos.legacy.KissosWalletCtx
import com.spgrvl.kissos.navigation.Navigation

@Immutable
data class CustomerJourneyCardModel(
    val id: String,
    @Suppress("MaximumLineLength", "ParameterWrapping", "MaxLineLength", "ParameterListWrapping")
    val condition: suspend (trnCount: Long, plannedPaymentsCount: Long, kissosContext: KissosWalletCtx, deps: CustomerJourneyDeps) -> Boolean,
    val title: String,
    val description: String,
    val cta: String?,
    @DrawableRes val ctaIcon: Int,

    val hasDismiss: Boolean = true,

    val background: Gradient,
    val onAction: (Navigation, KissosWalletCtx, RootScreen) -> Unit
)

@Immutable
data class CustomerJourneyDeps(
    val timeProvider: TimeProvider,
)
