package com.spgrvl.kissos.domain.banknotification

import android.content.Context

/**
 * The bank apps whose payment notifications Kissos can read. Here rather than with the parsers, so that
 * other screens (e.g. Home) can suggest the feature to people who have one of them.
 */
object SupportedBankApps {
    const val REVOLUT = "com.revolut.revolut"
    const val MAGENTA_PAY = "gr.payzy"
    const val EUROBANK = "com.EurobankEFG"

    val packages: Set<String> = setOf(REVOLUT, MAGENTA_PAY, EUROBANK)

    /** The supported apps installed on this phone; visible to Kissos through its launcher `<queries>`. */
    fun installed(context: Context): List<String> =
        packages.filter { context.packageManager.getLaunchIntentForPackage(it) != null }
}
