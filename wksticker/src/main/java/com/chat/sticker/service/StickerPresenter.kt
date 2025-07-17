package com.chat.sticker.service

import com.chat.base.net.HttpResponseCode
import com.chat.sticker.entity.Sticker
import com.chat.sticker.entity.StoreEntity
import java.lang.ref.WeakReference

/**
 * 12/30/20 3:41 PM
 *
 */

class StickerPresenter constructor(view: StickerContact.StickerView) :
    StickerContact.StickerPresenter {
    override fun getUserCustomSticker() {
        StickerModel().getUserCustomSticker(object : StickerModel.IStickersListener {
            override fun onResult(code: Int, msg: String, list: List<Sticker>) {
                if (code == HttpResponseCode.success.toInt()) {
                    stickerView.get()!!.setUserCustomSticker(list)
                } else {
                    stickerView.get()!!.showError(msg)
                }
            }

        })
    }

    override fun getStickerByCategory(category: String) {
        StickerModel().getStickerWithCategory(category, object : StickerModel.IStickersListener {
            override fun onResult(code: Int, msg: String, list: List<Sticker>) {
                if (code == HttpResponseCode.success.toInt()) {
                    stickerView.get()!!.setCategorySticker(list)
                } else {
                    stickerView.get()!!.showError(msg)
                }
            }

        })
    }

    override fun storeList(pageIndex: Int) {
        StickerModel().storeList(pageIndex, object : StickerModel.IStoreListener {
            override fun onResult(code: Int, msg: String, list: List<StoreEntity>) {
                if (code == HttpResponseCode.success.toInt()) {
                    stickerView.get()!!.setStoreList(list)
                } else {
                    stickerView.get()!!.hideLoading()
                    stickerView.get()!!.showError(msg)
                }
            }

        })
    }

    override fun showLoading() {
    }

    private var stickerView: WeakReference<StickerContact.StickerView> = WeakReference(view)
}