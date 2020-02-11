package com.tablayout.bean

import androidx.annotation.DrawableRes

data class CustomTabEntity(
    val tabTitle: String,
    @DrawableRes
    val tabSelectedIcon: Int,
    @DrawableRes
    val tabUnselectedIcon: Int
)