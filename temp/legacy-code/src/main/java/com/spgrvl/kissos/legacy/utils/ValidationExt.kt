package com.spgrvl.kissos.legacy.utils

fun String?.isNotNullOrBlank(): Boolean {
    return this != null && this.isNotBlank()
}
