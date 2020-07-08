package com.sandoval.selfiecam.utils

import android.content.Context

object CommonUtils {
    fun dp2px(context: Context, dipValue: Float): Float {
        return dipValue * context.resources.displayMetrics.density + 0.5f
    }
}