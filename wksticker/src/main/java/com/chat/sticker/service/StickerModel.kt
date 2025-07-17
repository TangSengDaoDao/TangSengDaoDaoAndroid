package com.chat.sticker.service

import android.text.TextUtils
import com.alibaba.fastjson.JSONObject
import com.chat.base.WKBaseApplication
import com.chat.base.base.WKBaseModel
import com.chat.base.config.WKApiConfig
import com.chat.base.config.WKConfig
import com.chat.base.config.WKSharedPreferencesUtil
import com.chat.base.net.ApiService
import com.chat.base.net.HttpResponseCode
import com.chat.base.net.ICommonListener
import com.chat.base.net.IRequestResultListener
import com.chat.base.net.ud.WKProgressManager
import com.chat.base.net.ud.WKDownloader
import com.chat.base.net.entity.CommonResponse
import com.chat.base.net.entity.UploadFileUrl
import com.chat.base.utils.WKFileUtils
import com.chat.sticker.WKStickerApplication
import com.chat.sticker.db.StickerDBManager
import com.chat.sticker.entity.Sticker
import com.chat.sticker.entity.StickerCategory
import com.chat.sticker.entity.StickerDetail
import com.chat.sticker.entity.StoreEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File
import java.util.*

/**
 * 12/30/20 2:54 PM
 * 表情model
 */
class StickerModel : WKBaseModel() {

    //添加表情
    fun addSticker(
        path: String,
        width: Int,
        height: Int,
        format: String,
        placeholder: String,
        category: String,
        iCommonListener: ICommonListener
    ) {
        val json = JSONObject()
        json["path"] = path
        json["width"] = width
        json["height"] = height
        json["format"] = format
        json["category"] = category
        json["placeholder"] = placeholder
        request(
            createService(StickerService::class.java).addSticker(json),
            object : IRequestResultListener<CommonResponse> {
                override fun onSuccess(result: CommonResponse?) {
                    var maxNum = StickerDBManager.instance.getCustomStickerMaxSortNum()
                    maxNum++
                    val sticker = Sticker()
                    sticker.path = path
                    sticker.width = width
                    sticker.height = height
                    sticker.sort_num = maxNum
                    sticker.format = format
                    sticker.placeholder = placeholder
                    sticker.category = category
                    //将表情保存在本地
                    StickerDBManager.instance.insertUserCustomSticker(sticker)
                    iCommonListener.run { onResult(result!!.status, result.msg) }
                }

                override fun onFail(code: Int, msg: String) {
                    iCommonListener.onResult(code, msg)
                }
            })
    }

    //移除表情
    fun deleteSticker(list: List<String>, iCommonListener: ICommonListener) {
        val json = JSONObject()
        json["paths"] = list
        request(
            createService(StickerService::class.java).deleteSticker(json),
            object : IRequestResultListener<CommonResponse> {
                override fun onSuccess(result: CommonResponse?) {
                    //删除本地表情
                    StickerDBManager.instance.deleteCustomSticker(list)
                    iCommonListener.onResult(result!!.status, result.msg)
                }

                override fun onFail(code: Int, msg: String) {
                    iCommonListener.onResult(code, msg)
                }

            })
    }

    //将表情移到最前面
    fun moveToFront(list: List<String>, iCommonListener: ICommonListener) {
        val json = JSONObject()
        json["paths"] = list
        request(
            createService(StickerService::class.java).moveToFront(json),
            object : IRequestResultListener<CommonResponse> {
                override fun onSuccess(result: CommonResponse?) {
                    //将本地表情排序
                    StickerDBManager.instance.moveToFront(list)
                    iCommonListener.onResult(result!!.status, result.msg)
                }

                override fun onFail(code: Int, msg: String) {
                    iCommonListener.onResult(code, msg)
                }

            })
    }

    //排序分类
    fun reorderCategory(list: List<String>, iCommonListener: ICommonListener) {
        val json = JSONObject()
        json["categorys"] = list
        request(
            createService(StickerService::class.java).reorderCategory(json),
            object : IRequestResultListener<CommonResponse> {
                override fun onSuccess(result: CommonResponse?) {
                    //修改本地分类
                    StickerDBManager.instance.sortCategory(list)
                    iCommonListener.onResult(result!!.status, result.msg)
                }

                override fun onFail(code: Int, msg: String?) {
                    iCommonListener.onResult(code, msg)
                }

            })
    }

    //通过分类移除表情
    fun removeStickerWithCategory(category: String, iCommonListener: ICommonListener) {
        request(
            createService(StickerService::class.java).removeStickerWithCategory(category),
            object : IRequestResultListener<CommonResponse> {
                override fun onSuccess(result: CommonResponse?) {
                    StickerDBManager.instance.deleteCategory(category)
                    iCommonListener.onResult(result!!.status, result.msg)
                }

                override fun onFail(code: Int, msg: String?) {
                    iCommonListener.onResult(code, msg)
                }

            })
    }

    //获取用户自定义表情
    fun getUserCustomSticker(iStickersListener: IStickersListener) {
        val list = StickerDBManager.instance.getUserCustomSticker()
//        val list = getSticker(WKConfig.getInstance().uid)
        if (list.isNotEmpty()) {
            iStickersListener.onResult(HttpResponseCode.success.toInt(), "", list)
        } else {
            fetchUserCustomSticker(iStickersListener)
        }
    }

    //从网络获取用户自定义表情
    fun fetchUserCustomSticker(iStickersListener: IStickersListener) {
        request(
            createService(StickerService::class.java).getUserCustomSticker(),
            object : IRequestResultListener<List<Sticker>> {
                override fun onSuccess(result: List<Sticker>) {
                    StickerDBManager.instance.addUserCustomSticker(result)
                    iStickersListener.onResult(HttpResponseCode.success.toInt(), "", result)
                    GlobalScope.launch(Dispatchers.IO) {
                        downloadCustomerSticker(result)
                    }
                }

                override fun onFail(code: Int, msg: String) {
                    iStickersListener.onResult(code, msg, emptyList())
                }

            })
    }

    //通过分类添加表情
    fun addStickerByCategory(category: String, iCommonListener: ICommonListener) {
        request(
            createService(StickerService::class.java).addStickerByCategory(category),
            object : IRequestResultListener<CommonResponse> {
                override fun onSuccess(result: CommonResponse?) {
                    iCommonListener.onResult(result!!.status, result.msg)
                }

                override fun onFail(code: Int, msg: String?) {
                    iCommonListener.onResult(code, msg)
                }

            })
    }

    //获取用户表情分类
    fun fetchCategoryList(iStickerCategoryListener: IStickerCategoryListener) {
        request(
            createService(StickerService::class.java).getCategoryList(),
            object : IRequestResultListener<List<StickerCategory>> {
                override fun onSuccess(result: List<StickerCategory>) {
//                saveStickerCategory(result)
                    StickerDBManager.instance.addUserStickerCategory(result)
                    iStickerCategoryListener.onResult(HttpResponseCode.success.toInt(), "", result)
                    GlobalScope.launch(Dispatchers.IO) {
                        saveCategorySticker(result)
                    }

                }

                override fun onFail(code: Int, msg: String) {
                    iStickerCategoryListener.onResult(code, msg, emptyList())
                }

            })
    }

    fun saveCategorySticker(list: List<StickerCategory>) {
        for (category in list) {
            val stickers = StickerDBManager.instance.getStickerWithCategory(category.category)
            if (stickers.isEmpty()) {
                fetchStickerWithCategory(category.category, object : IStickerDetailListener {
                    override fun onResult(
                        code: Int,
                        msg: String,
                        stickerDetail: StickerDetail?
                    ) {
                        if (code == HttpResponseCode.success.toInt() && stickerDetail!!.list.isNotEmpty()) {
                            for (sticker in stickerDetail.list) {
                                val path = sticker.path.replace("/", "_")
                                val filePath = WKStickerApplication.instance.stickerDirPath + path
                                val file = File(filePath)
                                if (!file.exists()) {
                                    WKDownloader.instance.download(
                                        WKApiConfig.getShowUrl(sticker.path),
                                        filePath,
                                        object :
                                            WKProgressManager.IProgress {
                                            override fun onProgress(tag: Any?, progress: Int) {
                                            }

                                            override fun onSuccess(tag: Any?, path: String?) {
                                            }

                                            override fun onFail(tag: Any?, msg: String?) {
                                            }

                                        })
                                }
                            }
                        }
                    }
                })
            }
        }
    }

    //保存表情分类
    private fun saveStickerCategory(list: List<StickerCategory>) {
        val jsonArray = JSONArray()
        for (item: StickerCategory in list) {
            val json = org.json.JSONObject()
            json.put("category", item.category)
            json.put("cover", item.cover)
            jsonArray.put(json)
        }
        WKSharedPreferencesUtil.getInstance()
            .putSP(WKConfig.getInstance().uid + "_sticker_category_list", jsonArray.toString())
    }

    //获取分类下的表情
    fun getStickerWithCategory(category: String, iStickersListener: IStickersListener) {
        val list = StickerDBManager.instance.getStickerWithCategory(category)
        if (list.isNotEmpty()) {
            iStickersListener.onResult(HttpResponseCode.success.toInt(), "", list)
        } else {
            fetchStickerWithCategory(category, object : IStickerDetailListener {
                override fun onResult(code: Int, msg: String, stickerDetail: StickerDetail?) {
                    if (stickerDetail == null) {
                        iStickersListener.onResult(code, msg, emptyList())
                    } else iStickersListener.onResult(code, msg, stickerDetail.list)
                }

            })

        }

    }

    //从网络获取某个分类下的表情
    fun fetchStickerWithCategory(
        category: String,
        iStickersListener: IStickerDetailListener
    ) {
        request(
            createService(StickerService::class.java).getStickerWithCategory(category),
            object : IRequestResultListener<StickerDetail> {
                override fun onSuccess(result: StickerDetail) {
                    StickerDBManager.instance.addSticker(result.list)
//                saveSticker(result)
                    iStickersListener.onResult(HttpResponseCode.success.toInt(), "", result)
                }

                override fun onFail(code: Int, msg: String) {
                    iStickersListener.onResult(code, msg, null)
                }

            })
    }

    //获取商店表情
    fun storeList(pageIndex: Int, iStoreListener: IStoreListener) {
        request(
            createService(StickerService::class.java).storeList(pageIndex, 20),
            object : IRequestResultListener<List<StoreEntity>> {
                override fun onSuccess(result: List<StoreEntity>) {
                    iStoreListener.onResult(HttpResponseCode.success.toInt(), "", result)
                }

                override fun onFail(code: Int, msg: String) {
                    iStoreListener.onResult(code, msg, emptyList())
                }

            })
    }

    //搜索表情
    fun search(keyword: String, pageIndex: Int, iSearchListener: ISearchListener) {
        request(
            createService(StickerService::class.java).search(keyword, pageIndex, 20),
            object : IRequestResultListener<List<Sticker>> {
                override fun onSuccess(result: List<Sticker>) {
                    iSearchListener.onResult(HttpResponseCode.success.toInt(), "", result)
                }

                override fun onFail(code: Int, msg: String) {
                    iSearchListener.onResult(code, msg, emptyList())
                }

            })
    }

    interface IStickerDetailListener {
        fun onResult(code: Int, msg: String, stickerDetail: StickerDetail?)
    }

    interface IStickersListener {
        fun onResult(code: Int, msg: String, list: List<Sticker>)
    }

    interface IStickerCategoryListener {
        fun onResult(code: Int, msg: String, list: List<StickerCategory>)
    }

    interface IStoreListener {
        fun onResult(code: Int, msg: String, list: List<StoreEntity>)
    }

    interface ISearchListener {
        fun onResult(code: Int, msg: String, list: List<Sticker>)
    }

//    fun saveSticker(list: List<Sticker>) {
//        var category = ""
//        val jsonArr = JSONArray()
//        if (list.isNotEmpty()) {
//            for (i in list.indices) {
//                val sticker: Sticker = list[i]
//                val json = org.json.JSONObject()
//                category = sticker.category
//                json.put("category", sticker.category)
//                json.put("path", sticker.path)
//                json.put("width", sticker.width)
//                json.put("height", sticker.height)
//                json.put("title", sticker.title)
//                jsonArr.put(json)
//            }
//        }
//        val key = String.format("%s_%s_stickers", WKConfig.getInstance().uid, category)
//        SharedPreferencesUtil.getInstance().putSP(key, jsonArr.toString())
//    }

//    private fun getSticker(category: String): List<Sticker> {
//        val list = ArrayList<Sticker>()
//        val key = String.format("%s_%s_stickers", WKConfig.getInstance().uid, category)
//        val arrStr = SharedPreferencesUtil.getInstance().getSP(key)
//        if (!TextUtils.isEmpty(arrStr)) {
//            val jsonArr = JSONArray(arrStr)
//            for (i in 0 until jsonArr.length()) {
//                val json: org.json.JSONObject = jsonArr[i] as org.json.JSONObject
//                val sticker = Sticker()
//                sticker.path = json.optString("path")
//                sticker.width = json.optInt("width")
//                sticker.height = json.optInt("height")
//                sticker.category = json.optString("category")
//                sticker.title = json.optString("title")
//                list.add(sticker)
//            }
//        }
//        list.sort()
//        return list
//    }


    // 获取表情上传地址
    fun getStickerUploadURL(iStickerUploadURLListener: IStickerUploadURLListener) {
        request(
            createService(ApiService::class.java).getUploadFileUrl(WKApiConfig.baseUrl + "file/upload?type=sticker"),
            object : IRequestResultListener<UploadFileUrl> {
                override fun onSuccess(result: UploadFileUrl) {
                    iStickerUploadURLListener.onResult(
                        HttpResponseCode.success.toInt(),
                        "",
                        result.url
                    )
                }

                override fun onFail(code: Int, msg: String) {
                    iStickerUploadURLListener.onResult(code, msg, "")
                }

            })
    }

    interface IStickerUploadURLListener {
        fun onResult(code: Int, msg: String, url: String)
    }

    fun downloadCustomerSticker(list: List<Sticker>) {
        for (sticker in list) {
            if (TextUtils.isEmpty(sticker.format)) {
                val savePath = getLocalPath(sticker.path)
                val file = File(savePath)
                if (file.exists()) {
                    continue
                }
                val url = WKApiConfig.getShowUrl(sticker.path)
                download(url, savePath)
            }
        }
    }

    fun download(url: String, savePath: String) {
        if (TextUtils.isEmpty(url)) return
        WKDownloader.instance.download(url, savePath, object : WKProgressManager.IProgress {
            override fun onProgress(tag: Any?, progress: Int) {
            }

            override fun onSuccess(tag: Any?, path: String?) {
            }

            override fun onFail(tag: Any?, msg: String?) {
            }

        })
    }

    fun getLocalPath(path: String): String {
        val fileDir =
            Objects.requireNonNull<File>(
                WKBaseApplication.getInstance().context.getExternalFilesDir(
                    "customerSticker"
                )
            ).absolutePath + "/"
        WKFileUtils.getInstance().createFileDir(fileDir)
        return fileDir + "${path.hashCode()}"
    }
}