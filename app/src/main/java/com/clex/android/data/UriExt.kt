package com.clex.android.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

fun Uri.displayName(context: Context): String? {
    if (scheme != "content") return path?.substringAfterLast('/')

    val cursor = context.contentResolver.query(this, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) return it.getString(index)
        }
    }
    return path?.substringAfterLast('/')
}
