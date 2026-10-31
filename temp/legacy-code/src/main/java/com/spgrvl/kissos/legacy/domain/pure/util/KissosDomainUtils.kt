package com.spgrvl.kissos.wallet.domain.pure.util

fun Double?.nextOrderNum(): Double = this?.plus(1) ?: 0.0
