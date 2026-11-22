package com.spgrvl.kissos.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import timber.log.Timber

/** Where unsupported receipts and bank notifications are reported, so that support for them can be added. */
const val SupportEmail = "contact.sgsoftware@gmail.com"

/** This Kissos's version name, for reports. */
val Context.kissosVersion: String
    get() = packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()

val androidVersion: String
    get() = Build.VERSION.RELEASE

/**
 * Opens an email to [SupportEmail] with [subject] and [body] filled in.
 *
 * @return false when there's no email app.
 */
fun Context.emailSupport(subject: String, body: String): Boolean {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(SupportEmail))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    return try {
        startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        Timber.w(e, "No email app to write to support")
        false
    }
}
