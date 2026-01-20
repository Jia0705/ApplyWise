package com.team.applywise.core.utils

import android.util.Patterns

object Utils {
    fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()
}