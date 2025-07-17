package com.chat.sticker.service

import com.alibaba.fastjson.JSONObject
import com.chat.base.net.entity.CommonResponse
import com.chat.sticker.entity.Sticker
import com.chat.sticker.entity.StickerCategory
import com.chat.sticker.entity.StickerDetail
import com.chat.sticker.entity.StoreEntity
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.*

/**
 * 12/30/20 2:56 PM
 *
 */
interface StickerService {
    @POST("sticker/user")
    fun addSticker(@Body jsonObject: JSONObject): Observable<CommonResponse>

    @HTTP(method = "DELETE", path = "sticker/user", hasBody = true)
    fun deleteSticker(@Body jsonObject: JSONObject): Observable<CommonResponse>

    @DELETE("sticker/remove")
    fun removeStickerWithCategory(@Query("category") category: String): Observable<CommonResponse>

    @GET("sticker/user")
    fun getUserCustomSticker(): Observable<List<Sticker>>

    @POST("sticker/user/{category}")
    fun addStickerByCategory(@Path("category") category: String): Observable<CommonResponse>

    @GET("sticker/user/category")
    fun getCategoryList(): Observable<List<StickerCategory>>

    @GET("sticker/user/sticker")
    fun getStickerWithCategory(@Query("category") category: String): Observable<StickerDetail>

    @GET("sticker/store")
    fun storeList(@Query("page_index") page_index: Int, @Query("page_size") page_size: Int): Observable<List<StoreEntity>>

    @GET("sticker")
    fun search(@Query("keyword") keyword: String, @Query("page") page: Int, @Query("page_size") page_size: Int): Observable<List<Sticker>>

    @PUT("sticker/user/front")
    fun moveToFront(@Body jsonObject: JSONObject): Observable<CommonResponse>

    @PUT("sticker/user/category/reorder")
    fun reorderCategory(@Body jsonObject: JSONObject): Observable<CommonResponse>
}