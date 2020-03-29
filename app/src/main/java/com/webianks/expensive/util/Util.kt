package com.webianks.expensive.util

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

object Util {

    const val TAG: String = "Expensive"
    const val EXPENSE_COLLECTION: String = "expenses"
    private const val PRIVACY_URL: String = "https://expensive.flycricket.io/privacy.html"

    fun openPrivacyTab(context: Context) {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(PRIVACY_URL))
    }
}