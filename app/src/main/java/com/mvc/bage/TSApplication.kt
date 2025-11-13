package com.mvc.bage

import android.app.Activity
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.text.TextUtils
import android.util.Log
import androidx.multidex.MultiDexApplication
import com.chat.advanced.WKAdvancedApplication
import com.chat.base.WKBaseApplication
import com.chat.base.config.WKApiConfig
import com.chat.base.config.WKConfig
import com.chat.base.config.WKConstants
import com.chat.base.config.WKSharedPreferencesUtil
import com.chat.base.endpoint.EndpointCategory
import com.chat.base.endpoint.EndpointManager
import com.chat.base.ui.Theme
import com.chat.base.utils.ActManagerUtils
import com.chat.base.utils.WKPlaySound
import com.chat.base.utils.WKTimeUtils
import com.chat.base.utils.language.WKMultiLanguageUtil
import com.chat.file.WKFileApplication
import com.chat.groupmanage.WKGroupManageApplication
import com.chat.imgeditor.WKImageEditorApplication
import com.chat.login.WKLoginApplication
import com.chat.moments.WKMomentsApplication
import com.chat.push.WKPushApplication
import com.chat.scan.WKScanApplication
import com.chat.sticker.WKStickerApplication
import com.chat.uikit.TabActivity
import com.chat.uikit.WKUIKitApplication
import com.chat.uikit.chat.manager.WKIMUtils
import com.chat.uikit.user.service.UserModel
import com.chat.video.WKVideoApplication
import com.tencent.bugly.crashreport.CrashReport
import kotlin.system.exitProcess

class TSApplication : MultiDexApplication() {
    val KEY_API_URL = "api_url"
    val DEFAULT_API_URL = "http://api.newhxchat.top/api"
    val BUGLY_ID = "d383347352"

    // 使用单例模式保存实例引用
    companion object {
        private lateinit var instance: TSApplication

        fun getInstance(): TSApplication {
            return instance
        }
    }
    // 标记API组件是否已初始化
    private var isApiInitialized = false

    override fun onCreate() {
        super.onCreate()

        instance = this  // 保存实例引用

        // 检查是否是进程重启，如果是则清理可能残留的状态
        if (isProcessRestarted()) {
            Log.d("TSApplication", "检测到进程重启，清理残留状态")
            // 清理EndpointManager中的重复菜单项
            EndpointManager.getInstance().clearAll()
            isApiInitialized = false
        }

        // 只初始化基础组件

        // 检查本地是否已缓存API地址
//        val cachedApiUrl = WKSharedPreferencesUtil.getInstance().getSP(KEY_API_URL)
//        if (!TextUtils.isEmpty(cachedApiUrl)) {
//            // 如果有缓存的API地址，可以立即初始化API依赖组件
//            initApiDependentComponents(cachedApiUrl)
//        }
        // 否则，等待SplashActivity获取API地址后再初始化

        // 优先初始化Bugly，确保异常处理器在CrashHandler之前设置
        CrashReport.initCrashReport(applicationContext, BUGLY_ID, true)
        
        val processName = getProcessName(this, Process.myPid())
        if (processName != null) {
            val defaultProcess = processName == getAppPackageName()
            if (defaultProcess) {
                initBasicComponents()
                val cachedApiUrl = WKSharedPreferencesUtil.getInstance().getSP(KEY_API_URL)
                initApi(cachedApiUrl);
            }
        }
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
            }

            override fun onActivityStarted(p0: Activity) {
            }

            override fun onActivityResumed(p0: Activity) {
                ActManagerUtils.getInstance().currentActivity = p0
            }

            override fun onActivityPaused(p0: Activity) {
            }

            override fun onActivityStopped(p0: Activity) {
            }

            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
            }

            override fun onActivityDestroyed(p0: Activity) {
            }
        })
    }


    // 检测是否是进程被杀死后重启
    private fun isProcessRestarted(): Boolean {
        // 可以通过SharedPreferences存储一个标记和时间戳
        val sp = getSharedPreferences("app_state", Context.MODE_PRIVATE)
        val lastTimestamp = sp.getLong("last_running_timestamp", 0)
        val currentTime = System.currentTimeMillis()

        // 更新时间戳
        sp.edit().putLong("last_running_timestamp", currentTime).apply()

        // 如果上次记录时间与当前时间差距过大，说明应用被杀死过
        return lastTimestamp > 0 && (currentTime - lastTimestamp > 30000) // 30秒是示例阈值
    }

    fun ensureInitialized(context: Context) {
        if (!isApiInitialized) {
            // 重新初始化必要组件
            initBasicComponents()
            // 获取保存的API URL
            val apiUrl = WKSharedPreferencesUtil.getInstance().getSP(KEY_API_URL) ?: DEFAULT_API_URL
            initApiDependentComponents(apiUrl)
        }
    }



    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (applicationContext != null && applicationContext.resources != null && applicationContext.resources.configuration != null && applicationContext.resources.configuration.uiMode != newConfig.uiMode) {
            WKMultiLanguageUtil.getInstance().setConfiguration()
            Theme.applyTheme()
            killAppProcess()
        }
    }

    private fun killAppProcess() {
        ActManagerUtils.getInstance().clearAllActivity()
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(WKMultiLanguageUtil.getInstance().attachBaseContext(base))
    }

//    private fun initAll() {
//
//        WKMultiLanguageUtil.getInstance().init(this)
//        WKBaseApplication.getInstance().init(getAppPackageName(), this)
//        Theme.applyTheme()
//        initApi()
//        WKLoginApplication.getInstance().init(this)
//        WKScanApplication.getInstance().init(this)
//        WKUIKitApplication.getInstance().init(this)
//        WKPushApplication.getInstance().init(getAppPackageName(), this)
//        addAppFrontBack()
//        addListener()
//    }

    fun initBasicComponents(){
        WKMultiLanguageUtil.getInstance().init(this)
        WKBaseApplication.getInstance().init(getAppPackageName(), this)
        Theme.applyTheme()
        // 其他不依赖API的基础初始化...
    }

    fun initApiDependentComponents(apiUrl: String) {
        // 避免重复初始化
        if (isApiInitialized) return

        // 清理可能存在的重复菜单项
        EndpointManager.getInstance().clearCategory(EndpointCategory.personalCenter)
        EndpointManager.getInstance().clearCategory(EndpointCategory.mailList)
        EndpointManager.getInstance().clearCategory(EndpointCategory.chatFunction)
        EndpointManager.getInstance().clearCategory(EndpointCategory.tabMenus)

        // 保存API地址到本地缓存，下次启动可以直接使用
//        WKSharedPreferencesUtil.getInstance().putSP(KEY_API_URL, apiUrl)

        // 初始化API
        initApi(apiUrl)

        // 初始化其他依赖API的组件，确保使用正确的上下文
        WKLoginApplication.getInstance().init(this) // 这里的this始终指向TSApplication实例
        WKScanApplication.getInstance().init(this)
        WKUIKitApplication.getInstance().init(this)
        WKPushApplication.getInstance().init(getAppPackageName(), this)
        WKGroupManageApplication.getInstance().init()
        WKFileApplication.getInstance().init(this)
        WKVideoApplication.getInstance().init(this)
        WKMomentsApplication.getInstance().init(this)
        WKAdvancedApplication.instance.init()
        WKImageEditorApplication.getInstance().init()
        UserModel.getInstance().getOnlineUsers()
        WKStickerApplication.instance.init()
        // 添加其他监听器
        addAppFrontBack()
        addListener()

        isApiInitialized = true
    }


    private fun initApi() {
        Log.d("TSApplication", "初始化了 ")
        var apiURL = WKSharedPreferencesUtil.getInstance().getSP("api_base_url")
        if (TextUtils.isEmpty(apiURL)) {
            apiURL = "http://api.newhxchat.top/api"
            WKApiConfig.initBaseURL(apiURL)
        } else {
            WKApiConfig.initBaseURLIncludeIP(apiURL)
        }
    }


    private fun initApi(apiUrl: String) {
        Log.d("TSApplication 初始化", "当前初始化API地址  $apiUrl")
        WKApiConfig.initBaseURL(apiUrl)
        // 其他API相关设置...
    }

    // 检查API组件是否已初始化
    fun isApiInitialized(): Boolean {
        return isApiInitialized
    }

    // 重置API初始化状态（用于重试场景）
    fun resetApiInitialized() {
        isApiInitialized = false
    }


    private fun getAppPackageName(): String {
        return "com.mvc.bage"
    }

    private fun getProcessName(cxt: Context, pid: Int): String? {
        val am = cxt.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val runningApps = am.runningAppProcesses ?: return null
        for (app in runningApps) {
            if (app.pid == pid) {
                return app.processName
            }
        }
        return null
    }

    private fun addAppFrontBack() {
        val helper = AppFrontBackHelper()
        helper.register(this, object : AppFrontBackHelper.OnAppStatusListener {
            override fun onFront() {
                if (!TextUtils.isEmpty(WKConfig.getInstance().token)) {
                    if (WKBaseApplication.getInstance().disconnect) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            EndpointManager.getInstance()
                                .invoke("chow_check_lock_screen_pwd", null)
                        }, 1000)
                    }
                    WKIMUtils.getInstance().initIMListener()
                    WKUIKitApplication.getInstance().startChat()
                    UserModel.getInstance().getOnlineUsers()

                }
            }

            override fun onBack() {
                val result = EndpointManager.getInstance().invoke("rtc_is_calling", null)
                var isCalling = false
                if (result != null) {
                    isCalling = result as Boolean
                }
                if (WKBaseApplication.getInstance().disconnect && !isCalling) {
//                    WKUIKitApplication.getInstance().stopConn()
                }
//                WKIMUtils.getInstance().removeListener()
                WKSharedPreferencesUtil.getInstance()
                    .putLong("lock_start_time", WKTimeUtils.getInstance().currentSeconds)

            }
        })
    }

    private fun addListener() {
        createNotificationChannel()
        EndpointManager.getInstance().setMethod("main_show_home_view") { `object` ->
            if (`object` != null) {
                val from = `object` as Int
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("from", from)
                startActivity(intent)
            }
            null
        }
        EndpointManager.getInstance().setMethod("show_tab_home") {
            val intent = Intent(applicationContext, TabActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            null
        }

        EndpointManager.getInstance().setMethod("play_new_msg_Media") {
            WKPlaySound.getInstance().playRecordMsg(R.raw.newmsg)
            null
        }
    }


    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = applicationContext.getString(R.string.new_msg_notification)
            val description = applicationContext.getString(R.string.new_msg_notification_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(WKConstants.newMsgChannelID, name, importance)
            channel.description = description
            channel.enableVibration(true) //是否有震动
            channel.setSound(
                Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.newmsg),
                Notification.AUDIO_ATTRIBUTES_DEFAULT
            )
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = applicationContext.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
        createNotificationRTCChannel()
    }

    private fun createNotificationRTCChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = applicationContext.getString(R.string.new_rtc_notification)
            val description = applicationContext.getString(R.string.new_rtc_notification_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(WKConstants.newRTCChannelID, name, importance)
            channel.description = description
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(0, 100, 100, 100, 100, 100)
            channel.setSound(
                Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.newrtc),
                Notification.AUDIO_ATTRIBUTES_DEFAULT
            )
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = applicationContext.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

}