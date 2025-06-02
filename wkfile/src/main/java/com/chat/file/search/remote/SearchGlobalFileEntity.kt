package com.chat.file.search.remote

import com.chat.base.entity.GlobalMessage
import com.chat.file.msgitem.FileContent

class SearchGlobalFileEntity(
    val fileModel: FileContent,
    val msg: GlobalMessage,
    val fileSize: String,
    val fileType: String,
    val time: String,
    val date: String,
    val fromUID: String,
    val fromName: String,
    val fromAvatarCache: String
)