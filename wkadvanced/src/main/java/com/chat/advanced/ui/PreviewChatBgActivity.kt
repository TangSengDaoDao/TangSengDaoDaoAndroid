package com.chat.advanced.ui

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chat.advanced.R
import com.chat.advanced.databinding.ActPreviewChatBgLayoutBinding
import com.chat.advanced.entity.ChatBgKeys
import com.chat.base.base.WKBaseActivity
import com.chat.base.config.WKApiConfig
import com.chat.base.config.WKConfig
import com.chat.base.config.WKConstants
import com.chat.base.config.WKSharedPreferencesUtil
import com.chat.base.glide.GlideUtils
import com.chat.base.msgitem.WKChatIteMsgFromType
import com.chat.base.msgitem.WKContentType
import com.chat.base.msgitem.WKMsgBgType
import com.chat.base.ui.Theme
import com.chat.base.ui.components.CheckBox
import com.chat.base.ui.components.CubicBezierInterpolator
import com.chat.base.utils.AndroidUtilities
import com.chat.base.utils.WKFileUtils
import com.chat.base.utils.WKTimeUtils
import com.chat.base.utils.SvgHelper
import com.chat.base.utils.singleclick.SingleClickUtil
import com.xinbida.wukongim.WKIM
import java.io.File


class PreviewChatBgActivity : WKBaseActivity<ActPreviewChatBgLayoutBinding>() {
    private lateinit var channelID: String
    private var channelType: Byte = 0

    //    private var colorIndex: Int = 0
    private lateinit var url: String
    private var lightColors: String = ""
    private var darkColors: String = ""
    private var isSvg: Int = 0
    private var isLocal: Int = 0
    override fun getViewBinding(): ActPreviewChatBgLayoutBinding {
        return ActPreviewChatBgLayoutBinding.inflate(layoutInflater)
    }

    override fun setTitle(titleTv: TextView?) {
        titleTv!!.setText(R.string.chat_bg_preview)
    }

    override fun initPresenter() {
        url = intent.getStringExtra("url").toString()
        channelID = intent.getStringExtra("channelID").toString()
        channelType = intent.getByteExtra("channelType", 0)
//        colorIndex = intent.getIntExtra("index", 0)
        isSvg = intent.getIntExtra("isSvg", 0)
        if (isSvg == 1) {
            lightColors = intent.getStringExtra("lightColors").toString()
            darkColors = intent.getStringExtra("darkColors").toString()
        }
        if (intent.hasExtra("isLocal")) isLocal = intent.getIntExtra("isLocal", 0)
    }

    private var gradientAngle = 45
    private fun initCheckBox(checkBox: CheckBox) {
        checkBox.setResId(this, R.mipmap.round_check2)
        checkBox.setDrawBackground(true)
        checkBox.setHasBorder(true)
        checkBox.setStrokeWidth(AndroidUtilities.dp(2f))
        checkBox.setBorderColor(ContextCompat.getColor(this, R.color.white))
        checkBox.setSize(24)
        checkBox.setColor(
            ContextCompat.getColor(this, R.color.transparent),
            ContextCompat.getColor(this, R.color.white)
        )
        checkBox.visibility = View.VISIBLE
        checkBox.isEnabled = true
        checkBox.setChecked(true, true)
    }

    override fun initView() {
        wkVBinding.saveTV.setTextColor(Theme.colorAccount)
        if (Theme.isDark()) {
            wkVBinding.textSizeTv.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            wkVBinding.textSizeTv.setTextColor(ContextCompat.getColor(this, R.color.black))
        }
        initCheckBox(wkVBinding.patternCB)
        initCheckBox(wkVBinding.blurredCB)
        wkVBinding.blurredCB.setChecked(false, true)
        wkVBinding.patternLayout.visibility = if (isSvg == 1) View.VISIBLE else View.GONE
        wkVBinding.rotateView.visibility = if (isSvg == 1) View.VISIBLE else View.GONE
        wkVBinding.blurredLayout.visibility = if (isSvg == 1) View.GONE else View.VISIBLE
        if (TextUtils.isEmpty(url)) wkVBinding.blurredLayout.visibility = View.GONE

        if (TextUtils.isEmpty(channelID)) {
//            val userInfoEntity = WKConfig.getInstance().userInfo
            val chatBgUrl =
                WKSharedPreferencesUtil.getInstance().getSPWithUID(ChatBgKeys.chatBgUrl)
            if (!TextUtils.isEmpty(chatBgUrl) && url == chatBgUrl) {
                gradientAngle = WKSharedPreferencesUtil.getInstance()
                    .getIntWithUID(ChatBgKeys.chatBgGradientAngle)
//                colorIndex = WKSharedPreferencesUtil.getInstance()
//                    .getInt(userInfoEntity.uid + "_" + WKChannelCustomerExtras.chatBgColorIndex)
                lightColors = WKSharedPreferencesUtil.getInstance()
                    .getSPWithUID(ChatBgKeys.chatBgColorLight)
                darkColors = WKSharedPreferencesUtil.getInstance()
                    .getSPWithUID(ChatBgKeys.chatBgColorDark)
                val showPattern = WKSharedPreferencesUtil.getInstance()
                    .getIntWithUID(ChatBgKeys.chatBgShowPattern)
                val isBlurred =
                    WKSharedPreferencesUtil.getInstance().getIntWithUID(ChatBgKeys.chatBgIsBlurred)
                wkVBinding.patternCB.setChecked(showPattern == 1, true)
                if (isBlurred == 1) {
                    wkVBinding.blurView.visibility = View.VISIBLE
                }
            }
        } else {
            val channel =
                WKIM.getInstance().channelManager.getChannel(channelID, channelType)
            if (channel?.localExtra != null) {
                val urlObject = channel.localExtra[ChatBgKeys.chatBgUrl]
                if (urlObject != null && urlObject == url) {
                    val isSvgObject = channel.localExtra[ChatBgKeys.chatBgIsSvg]
                    val isBlurredObject =
                        channel.localExtra[ChatBgKeys.chatBgIsBlurred]
                    if (isSvgObject != null) {
                        val colorLightObject =
                            channel.localExtra[ChatBgKeys.chatBgColorLight]
                        if (colorLightObject != null) {
                            lightColors = colorLightObject as String
                        }
                        val colorDarkObject =
                            channel.localExtra[ChatBgKeys.chatBgColorDark]
                        if (colorDarkObject != null) {
                            darkColors = colorDarkObject as String
                        }
                        val gradientAngleObject =
                            channel.localExtra[ChatBgKeys.chatBgGradientAngle]
                        if (gradientAngleObject != null) {
                            gradientAngle = gradientAngleObject as Int
                        }
                        val showPatternObject =
                            channel.localExtra[ChatBgKeys.chatBgShowPattern]
                        if (showPatternObject != null) {
                            val showPattern = showPatternObject as Int
                            wkVBinding.patternCB.setChecked(showPattern == 1, true)
                        }
                    }
                    if (isBlurredObject != null) {
                        val isBlurred = isBlurredObject as Int
                        wkVBinding.blurredCB.setChecked(isBlurred == 1, true)
                        if (isBlurred == 1)
                            wkVBinding.blurView.visibility = View.VISIBLE
                    }

                }

            }
        }
        val showTime = WKTimeUtils.getInstance().getNewChatTime(System.currentTimeMillis())
        wkVBinding.msgTimeTv.text = showTime
        wkVBinding.msgTimeTv1.text = showTime
        Theme.setColorFilter(this, wkVBinding.statusIV, R.color.color999)
        wkVBinding.statusIV.setImageDrawable(Theme.getTicksDoubleDrawable())
        wkVBinding.sendLayout.setAll(
            WKMsgBgType.single,
            WKChatIteMsgFromType.SEND,
            WKContentType.WK_TEXT
        )
        wkVBinding.recvLayout.setAll(
            WKMsgBgType.single,
            WKChatIteMsgFromType.RECEIVED,
            WKContentType.WK_TEXT
        )

        wkVBinding.progress.setSize(50)
        wkVBinding.loading.visibility = View.VISIBLE
        val path: String = if (isLocal == 0) {
            WKConstants.chatBgCacheDir + url.replace("/", "_")
        } else {
            url
        }
        val file = File(path)
        if (file.exists()) {
            wkVBinding.saveTV.isEnabled = true
            if (isSvg == 1) {
                val isDark = Theme.isDark()
//                val color1 =
//                    if (isDark) Theme.defaultColorsDark[colorIndex][0] else Theme.defaultColorsLight[colorIndex][0]
//                val color2 =
//                    if (isDark) Theme.defaultColorsDark[colorIndex][1] else Theme.defaultColorsLight[colorIndex][1]
//                val color3 =
//                    if (isDark) Theme.defaultColorsDark[colorIndex][2] else Theme.defaultColorsLight[colorIndex][2]
//                val color4 =
//                    if (isDark) Theme.defaultColorsDark[colorIndex][3] else Theme.defaultColorsLight[colorIndex][3]
                val colors = if (isDark) darkColors.split(",") else lightColors.split(",")
                val color1 = Color.parseColor("#" + colors[0])
                val color2 = Color.parseColor("#" + colors[1])
                val color3 = Color.parseColor("#" + colors[2])
                val color4 = Color.parseColor("#" + colors[3])
                val orientation = Theme.getGradientOrientation(gradientAngle)
                val drawable = GradientDrawable(
                    orientation,
                    intArrayOf(color1, color2, color3, color4)
                )
                wkVBinding.parentView.background = drawable

                val pco = AndroidUtilities.getPatternColor(
                    color1,
                    color2,
                    color3,
                    color4
                )
                val svgDrawable = SvgHelper.getBitmap(
                    file,
                    AndroidUtilities.getScreenWidth(),
                    AndroidUtilities.getScreenHeight(),
                    pco
                )
                wkVBinding.imageView.setImageBitmap(svgDrawable)
            } else {
//                val bd = getChatBg(file.absolutePath)
//                wkVBinding.imageView.setImageDrawable(bd)
                GlideUtils.getInstance().showImg(this, file.absolutePath, wkVBinding.imageView)
            }
            wkVBinding.loading.visibility = View.GONE
        } else {
            wkVBinding.saveTV.isEnabled = false
            Glide.with(this)
                .downloadOnly()
                .load(WKApiConfig.getShowUrl(url))
                .listener(object : RequestListener<File?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<File?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: File,
                        model: Any,
                        target: Target<File?>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        if (resource.exists()) {
                            WKFileUtils.getInstance()
                                .fileCopy(resource.absolutePath, file.absolutePath)
                            initView()
                        }
                        return false
                    }
                })
                .preload()
        }
    }

    var rotation = 0f

    override fun initListener() {
        wkVBinding.sendLayout.setOnLongClickListener { true }
        wkVBinding.recvLayout.setOnLongClickListener { true }
        wkVBinding.patternLayout.setOnClickListener {
            wkVBinding.patternCB.setChecked(!wkVBinding.patternCB.isChecked, true)
            wkVBinding.imageView.visibility =
                if (wkVBinding.patternCB.isChecked) View.VISIBLE else View.GONE
        }
        wkVBinding.rotateView.setOnClickListener {
            wkVBinding.rotateIV.rotation = rotation
            rotation -= 45
            changeColor()
            wkVBinding.rotateIV.animate().rotationBy(-45f).setDuration(300)
                .setInterpolator(CubicBezierInterpolator.EASE_OUT).start()
        }
        wkVBinding.blurredLayout.setOnClickListener {
            wkVBinding.blurredCB.setChecked(!wkVBinding.blurredCB.isChecked, true)
            wkVBinding.blurView.visibility =
                if (wkVBinding.blurredCB.isChecked) View.VISIBLE else View.GONE
        }

        SingleClickUtil.onSingleClick(wkVBinding.saveTV) {
            var savePath = url
            if (isLocal == 1) {
                val uid = WKConfig.getInstance().uid
                savePath = if (!TextUtils.isEmpty(channelID)) {
                    uid + "_" + channelType + "_" + channelID + ".jpg"
                } else {
                    "$uid.jpg"
                }
                val localPath = WKConstants.chatBgCacheDir + savePath
                val file = File(localPath)
                if (file.exists()) file.delete()
                WKFileUtils.getInstance().fileCopy(url, localPath)
            }
            saveChatBG(savePath, isSvg, if (wkVBinding.blurredCB.isChecked) 1 else 0)
        }
    }

    private fun changeColor() {
        gradientAngle += 45
        while (gradientAngle >= 360) {
            gradientAngle -= 360
        }
        val colors = if (Theme.isDark()) darkColors.split(",") else lightColors.split(",")
        val color1 = Color.parseColor("#" + colors[0])
        val color2 = Color.parseColor("#" + colors[1])
        val color3 = Color.parseColor("#" + colors[2])
        val color4 = Color.parseColor("#" + colors[3])

        val orientation = Theme.getGradientOrientation(gradientAngle)
        val drawable = GradientDrawable(
            orientation,
            intArrayOf(color1, color2, color3, color4)
        )

//        val orientation = Theme.getGradientOrientation(gradientAngle)
//        val drawable = GradientDrawable(
//            orientation,
//            Theme.defaultColorsDark[colorIndex]
//        )
        wkVBinding.parentView.background = drawable
    }

    private fun saveChatBG(url: String, isSvg: Int, isBlurred: Int) {
        if (TextUtils.isEmpty(channelID)) {
//            val userInfoEntity = WKConfig.getInstance().userInfo
            WKSharedPreferencesUtil.getInstance().putSPWithUID(ChatBgKeys.chatBgUrl, url)
            WKSharedPreferencesUtil.getInstance()
                .putIntWithUID(ChatBgKeys.chatBgIsBlurred, isBlurred)
            WKSharedPreferencesUtil.getInstance().putIntWithUID(ChatBgKeys.chatBgIsSvg, isSvg)
            WKSharedPreferencesUtil.getInstance().putIntWithUID(
                ChatBgKeys.chatBgShowPattern,
                if (wkVBinding.patternCB.isChecked) 1 else 0
            )
            WKSharedPreferencesUtil.getInstance()
                .putIntWithUID(ChatBgKeys.chatBgIsDeleted, if (TextUtils.isEmpty(url)) 1 else 0)
//            userInfoEntity.chat_bg_url = url
//            userInfoEntity.chat_bg_is_blurred = isBlurred
//            userInfoEntity.chat_bg_is_svg = isSvg
//            userInfoEntity.chat_bg_show_pattern =
//                if (wkVBinding.patternCB.isChecked) 1 else 0
//            userInfoEntity.chat_bg_is_deleted = if (TextUtils.isEmpty(url)) 1 else 0
//            WKConfig.getInstance().saveUserInfo(userInfoEntity)
            WKSharedPreferencesUtil.getInstance()
                .putIntWithUID(ChatBgKeys.chatBgGradientAngle, gradientAngle)
//                    WKSharedPreferencesUtil.getInstance()
//                        .putInt(
//                            userInfoEntity.uid + "_" + WKChannelCustomerExtras.chatBgColorIndex,
//                            colorIndex
//                        )
//
            WKSharedPreferencesUtil.getInstance().putSPWithUID(
                ChatBgKeys.chatBgColorLight,
                lightColors
            )
            WKSharedPreferencesUtil.getInstance().putSPWithUID(
                ChatBgKeys.chatBgColorLight,
                darkColors
            )
            setResult(RESULT_OK)
        } else {
            saveChannelChatBG(url, isSvg, isBlurred, if (TextUtils.isEmpty(url)) 1 else 0)
        }
        showToast(R.string.save_success)
        finish()
    }

    private fun saveChannelChatBG(url: String, isSvg: Int, isBlurred: Int, isDeleted: Int) {
        val channel = WKIM.getInstance().channelManager.getChannel(
            channelID,
            channelType
        )
        if (channel != null) {
            val showPattern = if (wkVBinding.patternCB.isChecked) 1 else 0
            if (channel.localExtra == null) channel.localExtra = HashMap<String, Any>()
            channel.localExtra[ChatBgKeys.chatBgUrl] = url
            channel.localExtra[ChatBgKeys.chatBgIsSvg] = isSvg
            channel.localExtra[ChatBgKeys.chatBgIsBlurred] = isBlurred
            channel.localExtra[ChatBgKeys.chatBgGradientAngle] = gradientAngle
//            channel.localExtra[WKChannelCustomerExtras.chatBgColorIndex] = colorIndex
            channel.localExtra[ChatBgKeys.chatBgShowPattern] = showPattern
            channel.localExtra[ChatBgKeys.chatBgIsDeleted] = isDeleted
            channel.localExtra[ChatBgKeys.chatBgColorLight] = lightColors
            channel.localExtra[ChatBgKeys.chatBgColorDark] = darkColors
            WKIM.getInstance().channelManager.saveOrUpdateChannel(channel)
        }
    }

    private fun getChatBg(path: String): BitmapDrawable {
        val bitmap = BitmapFactory.decodeFile(path)
        val drawable = BitmapDrawable(this.resources, bitmap)
        drawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        drawable.setDither(true)
        return drawable
    }
}