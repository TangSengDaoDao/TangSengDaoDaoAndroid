package com.chat.sticker.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import android.text.TextUtils
import com.chat.base.WKBaseApplication
import com.chat.base.db.WKCursor
import com.chat.sticker.entity.Sticker
import com.chat.sticker.entity.StickerCategory

/**
 * 1/6/21 2:43 PM
 * 表情数据库管理
 */
class StickerDBManager private constructor() {
    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = StickerDBManager()
    }

    private val customSticker = "custom_sticker"
    private val userStickerCategory = "user_sticker_category"
    private val sticker = "sticker"

    //获取用户自定义表情
    fun getUserCustomSticker(): ArrayList<Sticker> {
        val list = ArrayList<Sticker>()
        val sql = "select * from $customSticker order by sort_num desc"
        val cursor: Cursor = WKBaseApplication.getInstance().dbHelper.rawQuery(sql, null)
            ?: return list
        run {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                list.add(serializeCustomSticker(cursor))
                cursor.moveToNext()
            }
        }
        cursor.close()
        return list
    }

    fun getCustomerWithPath(path: String): Sticker? {
        val sql = "select * from $customSticker where path='$path'"
        val cursor: Cursor = WKBaseApplication.getInstance().dbHelper.rawQuery(sql) ?: return null
        cursor.moveToFirst()
        var sticker = Sticker()
        if (!cursor.isAfterLast) {
            sticker = serializeSticker(cursor)
            cursor.moveToNext()
        }
        cursor.close()
        return sticker
    }

    //获取用户表情分类列表
    fun getUserStickerCategory(): ArrayList<StickerCategory> {
        val list = ArrayList<StickerCategory>()
        val sql = "select * from $userStickerCategory order by sort_num desc"
        val cursor: Cursor = WKBaseApplication.getInstance().dbHelper.rawQuery(sql, null)
            ?: return list
        run {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                list.add(serializeStickerCategory(cursor))
                cursor.moveToNext()
            }
        }
        cursor.close()
        return list
    }

    //通过path查询某个自定义表情
    private fun getCustomStickerWidthPath(path: String): Sticker {
        val sql = "select * from $customSticker where path ='$path'"
        val cursor: Cursor = WKBaseApplication.getInstance().dbHelper.rawQuery(sql, null)
        cursor.moveToFirst()
        var sticker = Sticker()
        if (!cursor.isAfterLast) {
            sticker = serializeCustomSticker(cursor)
            cursor.moveToNext()
        }
        cursor.close()
        return sticker
    }

    //通过category查询某个表情分类
    private fun getCategoryWidthCategory(category: String): StickerCategory {
        val sql = "select * from $userStickerCategory where category ='$category'"
        val cursor: Cursor = WKBaseApplication.getInstance().dbHelper.rawQuery(sql, null)
        cursor.moveToFirst()
        var stickerCategory = StickerCategory()
        if (!cursor.isAfterLast) {
            stickerCategory = serializeStickerCategory(cursor)
            cursor.moveToNext()
        }
        cursor.close()
        return stickerCategory
    }

    //清空所有分类
    private fun clearCategory() {
        WKBaseApplication.getInstance().dbHelper
            .delete(userStickerCategory, null, null)
    }

    //添加分类
    fun addUserStickerCategory(list: List<StickerCategory>?) {
        if (list!!.isNotEmpty()) {
            try {
                clearCategory()
                WKBaseApplication.getInstance().dbHelper.db.beginTransaction()
                for (category in list) {
                    insertCategory(category)
                }
                WKBaseApplication.getInstance().dbHelper.db.setTransactionSuccessful()
            } catch (e: Exception) {
            } finally {
                if (WKBaseApplication.getInstance().dbHelper.db.inTransaction()) {
                    WKBaseApplication.getInstance().dbHelper.db.endTransaction()
                }
            }
        }
    }

    fun addSticker(list: List<Sticker>?) {
        if (list!!.isNotEmpty()) {
            try {
                val cvList = ArrayList<ContentValues>()
                for (sticker in list) {
                    val tempSticker = getStickerWithPath(sticker.path)
                    if (TextUtils.isEmpty(tempSticker.path))
                        cvList.add(getStickerCV(sticker))
                }
                WKBaseApplication.getInstance().dbHelper.db.beginTransaction()
                for (cv in cvList) {
                    WKBaseApplication.getInstance().dbHelper.insert("sticker", cv)
                }
                WKBaseApplication.getInstance().dbHelper.db.setTransactionSuccessful()
            } catch (_: Exception) {
            } finally {
                if (WKBaseApplication.getInstance().dbHelper.db.inTransaction()) {
                    WKBaseApplication.getInstance().dbHelper.db.endTransaction()
                }
            }
        }
    }

    private fun getStickerWithPath(path: String): Sticker {
        val sql = "select * from $sticker where path = '$path'"
        val cursor: Cursor = WKBaseApplication.getInstance().dbHelper.rawQuery(sql, null)
        cursor.moveToFirst()
        var sticker = Sticker()
        if (!cursor.isAfterLast) {
            sticker = serializeSticker(cursor)
            cursor.moveToNext()
        }
        cursor.close()
        return sticker
    }

    private fun insertSticker(sticker: Sticker?) {
        if (sticker == null) return
        val cv = getStickerCV(sticker)
        WKBaseApplication.getInstance().dbHelper.insert("sticker", cv)
    }

    //移除分类
    fun deleteStickerCategory(category: String): Boolean {
        val where = "category=?"
        val whereValue = arrayOfNulls<String>(1)
        whereValue[0] = category
        return WKBaseApplication.getInstance().dbHelper
            .delete(userStickerCategory, where, whereValue)
    }

    //清除所有自定义表情
    private fun clearCustomSticker() {
        WKBaseApplication.getInstance().dbHelper
            .delete(customSticker, null, null)
    }

    // 添加自定义表情
    fun addUserCustomSticker(list: List<Sticker>?) {
        if (list!!.isNotEmpty()) {
            try {
                clearCustomSticker()
                WKBaseApplication.getInstance().dbHelper.db.beginTransaction()
                for (sticker in list) {
                    insertUserCustomSticker(sticker)
                }
                WKBaseApplication.getInstance().dbHelper.db.setTransactionSuccessful()
            } catch (e: Exception) {
            } finally {
                if (WKBaseApplication.getInstance().dbHelper.db.inTransaction()) {
                    WKBaseApplication.getInstance().dbHelper.db.endTransaction()
                }
            }
        }
    }

    private fun deleteCustomSticker(path: String) {
        val where = "path=?"
        val whereValue = arrayOfNulls<String>(1)
        whereValue[0] = path
        WKBaseApplication.getInstance().dbHelper
            .delete(customSticker, where, whereValue)
    }

    fun deleteCategory(category: String) {
        val where = "category=?"
        val whereValue = arrayOfNulls<String>(1)
        whereValue[0] = category
        WKBaseApplication.getInstance().dbHelper
            .delete(userStickerCategory, where, whereValue)
    }


    fun deleteCustomSticker(paths: List<String>?) {
        if (paths!!.isNotEmpty()) {
            try {
                WKBaseApplication.getInstance().dbHelper.db.beginTransaction()
                for (path in paths) {
                    deleteCustomSticker(path)
                }
                WKBaseApplication.getInstance().dbHelper.db.setTransactionSuccessful()
            } catch (e: Exception) {
            } finally {
                if (WKBaseApplication.getInstance().dbHelper.db.inTransaction()) {
                    WKBaseApplication.getInstance().dbHelper.db.endTransaction()
                }
            }
        }
    }

    //将部分表情移到最前面
    fun moveToFront(paths: List<String>?) {
        if (paths!!.isNotEmpty()) {
            val num = getCustomStickerMaxSortNum()
            try {
                var tempSortNum = paths.size
                WKBaseApplication.getInstance().dbHelper.db.beginTransaction()
                for (i in paths.indices) {
                    updateCustomStickerSortNum(num + tempSortNum, paths[i])
                    tempSortNum--
                }
                WKBaseApplication.getInstance().dbHelper.db.setTransactionSuccessful()
            } catch (e: Exception) {
            } finally {
                if (WKBaseApplication.getInstance().dbHelper.db.inTransaction()) {
                    WKBaseApplication.getInstance().dbHelper.db.endTransaction()
                }
            }
        }
    }

    //排序分类
    fun sortCategory(list: List<String>?) {
        if (list!!.isNotEmpty()) {
//            val num = getCustomStickerMaxSortNum()
            try {
                var tempSortNum = list.size
                WKBaseApplication.getInstance().dbHelper.db.beginTransaction()
                for (i in list.indices) {
                    updateCategorySortNum(tempSortNum, list[i])
                    tempSortNum--
                }
                WKBaseApplication.getInstance().dbHelper.db.setTransactionSuccessful()
            } catch (e: Exception) {
            } finally {
                if (WKBaseApplication.getInstance().dbHelper.db.inTransaction()) {
                    WKBaseApplication.getInstance().dbHelper.db.endTransaction()
                }
            }
        }
    }


    //修改分类序号
    private fun updateCategorySortNum(sortNum: Int, category: String) {
        val updateKey = arrayOfNulls<String>(1)
        val updateValue = arrayOfNulls<String>(1)
        updateKey[0] = "sort_num"
        updateValue[0] = sortNum.toString()
        val where = "category=?"
        val whereValue = arrayOfNulls<String>(1)
        whereValue[0] = category
        val cv = ContentValues()
        cv.put("sort_num", sortNum)
        WKBaseApplication.getInstance().dbHelper
            .update(userStickerCategory, cv, where, whereValue)
    }

    //修改表情序号
    private fun updateCustomStickerSortNum(sortNum: Int, path: String) {
        val updateKey = arrayOfNulls<String>(1)
        val updateValue = arrayOfNulls<String>(1)
        updateKey[0] = "sort_num"
        updateValue[0] = sortNum.toString()
        val where = "path=?"
        val whereValue = arrayOfNulls<String>(1)
        whereValue[0] = path
        val cv = ContentValues()
        cv.put("sort_num", sortNum)
        WKBaseApplication.getInstance().dbHelper
            .update(customSticker, cv, where, whereValue)
    }

    //获取自定义表情的最大序号
    fun getCustomStickerMaxSortNum(): Int {
        val sql = "select * from $customSticker order by sort_num desc limit 1"
        val cursor: Cursor = WKBaseApplication.getInstance().dbHelper.rawQuery(sql, null)
        cursor.moveToFirst()
        var num = 0
        if (!cursor.isAfterLast) {
            val sticker = serializeCustomSticker(cursor)
            num = sticker.sort_num
            cursor.moveToNext()
        }
        cursor.close()
        return num
    }

    // 添加表情
    fun insertUserCustomSticker(sticker: Sticker?) {
        if (sticker == null) return
        val cv = getCustomStickerCV(sticker)
        WKBaseApplication.getInstance().dbHelper.insert(customSticker, cv)
    }

    //添加分类
    private fun insertCategory(category: StickerCategory?) {
        if (category == null) return
        val cv = getCategoryCV(category)
        WKBaseApplication.getInstance().dbHelper.insert(userStickerCategory, cv)
    }

    //获取某个分类下的表情
    fun getStickerWithCategory(category: String): ArrayList<Sticker> {
        val list = ArrayList<Sticker>()
        val sql = "select * from $sticker where category='$category'"
        val cursor: Cursor = WKBaseApplication.getInstance().dbHelper.rawQuery(sql, null)
            ?: return list
        run {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                list.add(serializeSticker(cursor))
                cursor.moveToNext()
            }
        }
        cursor.close()
        return list
    }

    @SuppressLint("Range")
    private fun serializeCustomSticker(cursor: Cursor): Sticker {
        val sticker = Sticker()
        sticker.path = cursor.getString(cursor.getColumnIndex("path"))
        sticker.width = cursor.getInt(cursor.getColumnIndex("width"))
        sticker.height = cursor.getInt(cursor.getColumnIndex("height"))
        sticker.sort_num = cursor.getInt(cursor.getColumnIndex("sort_num"))
        sticker.placeholder = cursor.getString(cursor.getColumnIndex("placeholder"))
        sticker.format = cursor.getString(cursor.getColumnIndex("format"))
        sticker.category = cursor.getString(cursor.getColumnIndex("category"))
        return sticker
    }

    @SuppressLint("Range")
    private fun serializeStickerCategory(cursor: Cursor): StickerCategory {
        val stickerCategory = StickerCategory()
        stickerCategory.cover = cursor.getString(cursor.getColumnIndex("cover"))
        stickerCategory.cover_lim = cursor.getString(cursor.getColumnIndex("cover_lim"))
        stickerCategory.category = cursor.getString(cursor.getColumnIndex("category"))
        stickerCategory.sort_num = cursor.getInt(cursor.getColumnIndex("sort_num"))
        stickerCategory.title = cursor.getString(cursor.getColumnIndex("title"))
        return stickerCategory
    }

    @SuppressLint("Range")
    private fun serializeSticker(cursor: Cursor): Sticker {
        val sticker = Sticker()
        sticker.path = WKCursor.readString(cursor, "path")
        sticker.category = WKCursor.readString(cursor, "category")
        sticker.width = WKCursor.readInt(cursor, "width")
        sticker.height = WKCursor.readInt(cursor, "height")
        sticker.title = WKCursor.readString(cursor, "title")
        sticker.placeholder = WKCursor.readString(cursor, "placeholder")
        sticker.format = WKCursor.readString(cursor, "format")
        sticker.searchable_word = WKCursor.readString(cursor, "searchable_word")
        return sticker
    }

    private fun getCustomStickerCV(sticker: Sticker): ContentValues {
        val cv = ContentValues()
        cv.put("path", sticker.path)
        cv.put("width", sticker.width)
        cv.put("height", sticker.height)
        cv.put("sort_num", sticker.sort_num)
        cv.put("format", sticker.format)
        cv.put("placeholder", sticker.placeholder)
        cv.put("category", sticker.category)
        return cv
    }

    private fun getStickerCV(sticker: Sticker): ContentValues {
        val cv = ContentValues()
        cv.put("path", sticker.path)
        cv.put("width", sticker.width)
        cv.put("height", sticker.height)
        cv.put("searchable_word", sticker.searchable_word)
        cv.put("category", sticker.category)
        cv.put("title", sticker.title)
        cv.put("placeholder", sticker.placeholder)
        cv.put("format", sticker.format)
        return cv
    }

    private fun getCategoryCV(category: StickerCategory): ContentValues {
        val cv = ContentValues()
        cv.put("cover", category.cover)
        cv.put("cover_lim", category.cover_lim)
        cv.put("title", category.title)
        cv.put("category", category.category)
        cv.put("sort_num", category.sort_num)
        return cv
    }


    private fun queryEmojiSticker(category: String, searchableWord: String): Sticker {
        val sql =
            "select * from $sticker where category='$category' and searchable_word='$searchableWord'"
        val cursor: Cursor = WKBaseApplication.getInstance().dbHelper.rawQuery(sql, null)
        cursor.moveToFirst()
        var sticker = Sticker()
        if (!cursor.isAfterLast) {
            sticker = serializeSticker(cursor)
            cursor.moveToNext()
        }
        cursor.close()
        return sticker
    }

    var emojiStickerList = ArrayList<Sticker>()
    fun getEmojiSticker(searchableWord: String): Sticker {
        var sticker: Sticker? = null
        if (emojiStickerList.isNotEmpty()) {
            for (e in emojiStickerList) {
                if (e.searchable_word == searchableWord) {
                    sticker = e
                    break
                }
            }
        }
        if (sticker != null) {
            return sticker
        }
        sticker = queryEmojiSticker("emoji", searchableWord)
        if (!TextUtils.isEmpty(sticker.path))
            emojiStickerList.add(sticker)
        return sticker
    }
}