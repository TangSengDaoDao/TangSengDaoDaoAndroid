package com.chat.video.search.remote

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.chat.base.entity.GlobalMessage
import com.xinbida.wukongim.msgmodel.WKVideoContent

class SearchEntity(
    val type: Int,
    ) :
    MultiItemEntity {
    var date: String = ""
    var second: String = ""
    lateinit var videoModel: WKVideoContent
    lateinit var message: GlobalMessage
    override val itemType: Int
        get() = type
}