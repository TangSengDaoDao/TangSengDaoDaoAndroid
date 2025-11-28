package com.chat.advanced.utils

import com.chat.base.config.WKConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun checkEditTime(createdTime: String): Boolean {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val createdDate: Date = sdf.parse(createdTime)!!
    val now = Date()

    val diffInMillis = now.time - createdDate.time
    val diffInSeconds = diffInMillis / 1000
//    val createdAt = LocalDateTime.parse(createdTime,Const.dateTimeFormatter)
//    val now = LocalDateTime.now()
//    val diffInSeconds = Duration.between(createdAt, now).seconds
    val appConfig = WKConfig.getInstance().appConfig
    return (diffInSeconds >= appConfig.revoke_second)
}