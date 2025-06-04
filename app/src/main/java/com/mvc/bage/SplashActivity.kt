package com.mvc.bage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chat.base.WKBaseApplication
import com.chat.base.config.WKSharedPreferencesUtil
import com.chat.base.ui.components.AlertDialog
import com.chat.base.utils.IpSearch
import com.chat.uikit.TabActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection


@SuppressLint("CustomSplashScreen")
public final class SplashActivity : AppCompatActivity() {
    val KEY_API_URL = "api_url"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 确保全屏
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            val window: Window = getWindow()
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            window.setStatusBarColor(Color.TRANSPARENT)
//
//
//            // 设置内容延伸到状态栏
//            window.getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
//                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//            )
//        }


        setContentView(R.layout.splash_activity_with_animation)

        // 使用单例方法获取TSApplication实例，而不是使用application属性转换
        val tsApp = TSApplication.getInstance()

        // 如果API组件已初始化，直接进入主界面
        if (tsApp.isApiInitialized()) {
            startMainActivity()
            return
        }

        // 否则，获取API地址
        if (isNetworkAvailable(this)) {
            getConfigAsync()
        } else {
            // 无网络情况下，尝试使用默认地址初始化
            tsApp.initApiDependentComponents(tsApp.DEFAULT_API_URL)
            showErrorAndRetryOption(Exception("网络连接不可用，使用默认API地址"))
        }
    }
    fun getConfigAsync() {
        lifecycleScope.launch {
            try {
                val configJson = withContext(Dispatchers.IO) {
                    val ossUrl = "https://liuxing-shangwu.oss-accelerate.aliyuncs.com/config.json"
                    getConfig(ossUrl)
                }

                // 从配置中解析API地址
                val apiUrl = configJson

                // 使用单例方法获取TSApplication实例
                TSApplication.getInstance().initApiDependentComponents(apiUrl)

                WKSharedPreferencesUtil.getInstance().putSP(KEY_API_URL,apiUrl)

                // 进入主界面
                startMainActivity()
            } catch (e: Exception) {
                // 处理错误，尝试使用默认地址
                TSApplication.getInstance().initApiDependentComponents(
                    TSApplication.getInstance().DEFAULT_API_URL
                )
                showErrorAndRetryOption(e)
            }
        }
    }


    private fun startMainActivity() {
        // 创建进入主页的Intent
        val intent = Intent(this, TabActivity::class.java)

        // 添加标志以清除任务栈，确保SplashActivity完全退出
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // 启动主页Activity
        startActivity(intent)

        // 结束SplashActivity
        finish()
    }

     private fun getConfig(getUrl: String): String {
        var jsValue = ""
        if (isNetworkAvailable(WKBaseApplication.getInstance().getContext())) {
            try {
                val url = URL(getUrl)
                val connection: HttpURLConnection = if (getUrl.startsWith("https")) {
                    url.openConnection() as HttpsURLConnection
                } else {
                    url.openConnection() as HttpURLConnection
                }

                connection.connectTimeout = 3000
                connection.readTimeout = 3000 // 设置读取超时为 3000 毫秒
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val inputStream = connection.inputStream
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                    val line = bufferedReader.readLine()

                    bufferedReader.close()
                    Log.i("LINE", line)
                    jsValue = line
                }
                connection.disconnect()
                val jsonObject = JSONObject(jsValue)
                val configUrl = jsonObject.optString("config", "") // 第二个参数是默认值
                val configJwUrl = jsonObject.optString("configJw", "")
                val ip = getDeviceIp()
                val instance = IpSearch.getInstance(WKBaseApplication.getInstance().getContext())
                val area = instance.getArea(ip)
                if (area != "CN") {
                    return configJwUrl;
                } else {
                    return configUrl;
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return jsValue;
            }
        } else {
            //fixme 网络异常
        }
        return jsValue
    }

    @SuppressLint("MissingPermission")
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // 使用NetworkCapabilities API (推荐用于API 23及以上)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
        // 兼容老版本Android (API 22及以下)
        else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }


    private fun getDeviceIp(): String {
        val ipApis = listOf(
            "https://api.ip.sb/ip",
            "https://icanhazip.com/",
            "https://ifconfig.me/ip"
        )

        for (api in ipApis) {
            try {
                val url = URL(api)
                val connection = url.openConnection() as HttpsURLConnection
                connection.connectTimeout = 3000 // 设置连接超时为 3000 毫秒
                connection.readTimeout = 3000 // 设置读取超时为 3000 毫秒
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val ip = reader.readLine()?.trim() ?: ""
                    reader.close()

                    if (ip.isNotEmpty()) {
                        Log.d("TSApplication", "成功从 $api 获取到 IP: $ip")
                        return ip
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e("TSApplication", "从 $api 获取 IP 失败: ${e.message}")
                // 继续尝试下一个 API
            }
        }

        Log.e("TSApplication", "无法从任何 API 获取 IP 地址")
        return "" // 如果所有 API 都失败，返回空字符串
    }

    private fun showErrorAndRetryOption(exception: Exception) {
        // 显示错误对话框，提供重试按钮
        AlertDialog.Builder(this)
            .setTitle("网络错误")
            .setMessage("无法获取API配置，请检查网络连接后重试")
            .setPositiveButton("重试") { _, _ -> recreate() }
            .setNegativeButton("退出") { _, _ -> finish() }
            .show()
    }
}
