package com.chat.sticker.service

import com.chat.base.base.WKBasePresenter
import com.chat.base.base.WKBaseView
import com.chat.sticker.entity.Sticker
import com.chat.sticker.entity.StoreEntity

/**
 * 12/30/20 3:37 PM
 *
 */
interface StickerContact {
    interface StickerPresenter : WKBasePresenter {
        fun storeList(pageIndex: Int)
        fun getStickerByCategory(category: String)
        fun getUserCustomSticker()
    }

    interface StickerView : WKBaseView {
        fun setStoreList(list: List<StoreEntity>)
        fun setCategorySticker(list: List<Sticker>)
        fun setUserCustomSticker(list: List<Sticker>)
    }
}