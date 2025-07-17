package com.chat.sticker.ui

import android.graphics.BitmapFactory
import android.widget.Button
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chat.base.base.WKBaseActivity
import com.chat.base.endpoint.EndpointManager
import com.chat.base.glide.ChooseMimeType
import com.chat.base.glide.ChooseResult
import com.chat.base.glide.GlideRequestOptions
import com.chat.base.glide.GlideUtils
import com.chat.base.net.HttpResponseCode
import com.chat.base.net.ud.WKUploader
import com.chat.base.utils.ImageUtils
import com.chat.base.utils.WKFileUtils
import com.chat.sticker.R
import com.chat.sticker.databinding.ActAddCustomStickerLayoutBinding
import com.chat.sticker.gifmaker.AnimatedGifEncoder
import com.chat.sticker.service.StickerModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


/**
 * 1/3/21 7:40 PM
 * 添加自定义表情
 */
class AddCustomStickerActivity : WKBaseActivity<ActAddCustomStickerLayoutBinding>() {

    private var tempImgPath: String = ""
    private var path: String = ""
    override fun setTitle(titleTv: TextView?) {
        titleTv!!.text = ""
    }

    override fun initPresenter() {
    }

    override fun getRightBtnText(titleRightBtn: Button?): String {
        return getString(R.string.str_save)
    }

    override fun rightButtonClick() {
        super.rightButtonClick()
        showOrHideRightBtn(false)
        showTitleRightLoading()
        val localPath = tempImgPath
        if (localPath.uppercase().endsWith("GIF")) {
            path = tempImgPath
            upload()
        } else {
            createGif(UUID.randomUUID().toString(), 200)
        }
    }

    private fun upload() {
        StickerModel().getStickerUploadURL(object : StickerModel.IStickerUploadURLListener {
            override fun onResult(code: Int, msg: String, url: String) {
                if (code == HttpResponseCode.success.toInt()) {
                    WKUploader.getInstance().upload(url, path, object : WKUploader.IUploadBack {
                        override fun onSuccess(url: String?) {

                            val bitmap = BitmapFactory.decodeFile(path)
                            StickerModel().addSticker(
                                url!!,
                                bitmap.width,
                                bitmap.height,
                                "",
                                "",
                                ""
                            ) { code, msg ->
                                hideTitleRightLoading()
                                if (code == HttpResponseCode.success.toInt()) {
                                    //重置表情
                                    EndpointManager.getInstance()
                                        .invoke("refresh_custom_sticker", null)
                                    setResult(RESULT_OK)
                                    finish()
                                } else {
                                    showOrHideRightBtn(true)
                                    showToast(msg)
                                }
                            }

                        }

                        override fun onError() {
                            showToast(R.string.upload_sticker_err)
                        }

                    })
                } else {
                    showOrHideRightBtn(true)
                    hideTitleRightLoading()
                    showToast(msg)
                }
            }

        })
    }

    override fun initView() {
        chooseIMG()
    }

    override fun initListener() {
    }

    /**
     * 生成gif图
     *
     * @param delay 图片之间间隔的时间
     */
    private fun createGif(file_name: String, delay: Int) {
        val baos = ByteArrayOutputStream()
        val localAnimatedGifEncoder = AnimatedGifEncoder()
        localAnimatedGifEncoder.start(baos)//start
        localAnimatedGifEncoder.setRepeat(0)//设置生成gif的开始播放时间。0为立即开始播放
        localAnimatedGifEncoder.setDelay(delay)

        //【注意1】开始生成gif的时候，是以第一张图片的尺寸生成gif图的大小，后面几张图片会基于第一张图片的尺寸进行裁切
        //所以要生成尺寸完全匹配的gif图的话，应先调整传入图片的尺寸，让其尺寸相同
        //【注意2】如果传入的单张图片太大的话会造成OOM，可在不损失图片清晰度先对图片进行质量压缩
        localAnimatedGifEncoder.addFrame(BitmapFactory.decodeFile(tempImgPath))
        localAnimatedGifEncoder.finish()
        val fileDir = getExternalFilesDir("tempGIF")!!.absoluteFile.absolutePath
        WKFileUtils.getInstance().createFileDir(fileDir)
        path = "$fileDir/$file_name.gif"
        WKFileUtils.getInstance().createFile(path)
        try {
            val fos = FileOutputStream(path)
            baos.writeTo(fos)
            baos.flush()
            fos.flush()
            baos.close()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        upload()
    }

    override fun getViewBinding(): ActAddCustomStickerLayoutBinding {
        return ActAddCustomStickerLayoutBinding.inflate(layoutInflater)
    }

    fun chooseIMG() {
        GlideUtils.getInstance()
            .chooseIMG(this, 1, false, ChooseMimeType.img, false, object : GlideUtils.ISelectBack {
                override fun onBack(paths: MutableList<ChooseResult>?) {
                    if (paths!!.isNotEmpty()) {
                        val file = File(paths[0].path)
                        if (file.length() > 1024 * 1024 * 2) {
                            showToast(R.string.img_size_is_too_large)
                            finish()
                            return
                        }
                        tempImgPath = paths[0].path
                        val bitmap = BitmapFactory.decodeFile(paths[0].path)
                        val wH = ImageUtils.getInstance()
                            .getImageWidthAndHeightToTalk(bitmap.width, bitmap.height)
                        wkVBinding.imageView.layoutParams.width = wH[0]
                        wkVBinding.imageView.layoutParams.height = wH[1]

                        if (tempImgPath.endsWith("GIF") || tempImgPath.endsWith("gif")) {
                            Glide.with(this@AddCustomStickerActivity).asGif().load(tempImgPath)
                                .apply(GlideRequestOptions.getInstance().normalRequestOption())
                                .into(wkVBinding.imageView)
                        } else {
                            wkVBinding.imageView.setImageBitmap(bitmap)
                        }
                    } else {
                        finish()
                    }
                }

                override fun onCancel() {
                    finish()
                }

            })
    }
}