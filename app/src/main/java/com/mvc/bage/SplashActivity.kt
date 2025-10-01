package com.mvc.bage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.chat.base.WKBaseApplication
import com.chat.base.config.WKApiConfig
import com.chat.base.config.WKSharedPreferencesUtil
import com.chat.base.endpoint.EndpointCategory
import com.chat.base.endpoint.EndpointManager
import com.chat.base.ui.components.AlertDialog
import com.chat.base.ui.components.NormalClickableContent
import com.chat.base.ui.components.NormalClickableSpan
import com.chat.base.utils.IpSearch
import com.chat.base.utils.JiamiUtil
import com.chat.base.utils.WKDialogUtils
import com.chat.base.act.WKWebViewActivity
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

        // 先检查隐私协议是否同意，未同意则先拦截，避免进入主页或跳过 getConfig
        val needShowAgreement = WKSharedPreferencesUtil.getInstance().getBoolean("show_agreement_dialog")
        if (needShowAgreement) {
            showAgreementAndThenProceed()
        } else {
            proceedInit()
        }
    }

    private fun proceedInit() {
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

    private fun showAgreementAndThenProceed() {
        val content = getString(R.string.dialog_content)
        val linkSpan = SpannableStringBuilder()
        linkSpan.append(content)
        val userAgreementIndex = content.indexOf(getString(R.string.main_user_agreement))
        linkSpan.setSpan(
            NormalClickableSpan(
                true,
                ContextCompat.getColor(this, R.color.blue),
                NormalClickableContent(NormalClickableContent.NormalClickableTypes.Other, ""),
                object : NormalClickableSpan.IClick {
                    override fun onClick(view: View) {
                        showWebView(
                            WKApiConfig.baseWebUrl + "user_agreement.html"
                        )
                    }
                }), userAgreementIndex, userAgreementIndex + 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val privacyPolicyIndex = content.indexOf(getString(R.string.main_privacy_policy))
        linkSpan.setSpan(
            NormalClickableSpan(true,
                ContextCompat.getColor(this, R.color.blue),
                NormalClickableContent(NormalClickableContent.NormalClickableTypes.Other, ""),
                object : NormalClickableSpan.IClick {
                    override fun onClick(view: View) {
                        showWebView(
                            WKApiConfig.baseWebUrl + "privacy_policy.html"
                        )
                    }
                }), privacyPolicyIndex, privacyPolicyIndex + 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        WKDialogUtils.getInstance().showDialog(
            this,
            getString(R.string.dialog_title),
            linkSpan,
            false,
            getString(R.string.disagree),
            getString(R.string.agree),
            0,
            0
        ) { index ->
            if (index == 1) {
                WKSharedPreferencesUtil.getInstance()
                    .putBoolean("show_agreement_dialog", false)
                WKBaseApplication.getInstance().init(
                    WKBaseApplication.getInstance().packageName,
                    WKBaseApplication.getInstance().application
                )
                proceedInit()
            } else {
                finish()
            }
        }
    }

    private fun showWebView(url: String) {
        val intent = Intent(this, WKWebViewActivity::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
    }

    fun getConfigAsync() {
        lifecycleScope.launch {
            try {
                val configJson = withContext(Dispatchers.IO) {
                    val ossUrl = "https://clean-nengyuan.oss-accelerate.aliyuncs.com/config.json"
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
        Log.d("AppFlow", "[SplashActivity] startMainActivity called")
        // 创建进入MainActivity的Intent，让MainActivity处理隐私协议
        val intent = Intent(this, MainActivity::class.java)

        // 添加标志以清除任务栈，确保SplashActivity完全退出
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // 启动MainActivity
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

                connection.connectTimeout = 20000
                connection.readTimeout = 20000 // 设置读取超时为 3000 毫秒
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
                var configUrl = jsonObject.optString("config", "") // 第二个参数是默认值
                var configJwUrl = jsonObject.optString("configJw", "")
                configUrl= JiamiUtil.decrypt(configUrl)
                configJwUrl=JiamiUtil.decrypt(configJwUrl)


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
            // 中国境内可用的API（优先）
            "https://whois.pconline.com.cn/ipJson.jsp?json=true",  // 太平洋IP查询
            
            // 国际稳定API（备用）
            "https://ifconfig.me/ip",  // ifconfig.me
            "https://icanhazip.com/",  // icanhazip
            "https://api.ipify.org?format=text",  // ipify
            "https://ipinfo.io/ip",  // ipinfo
            "https://checkip.amazonaws.com",  // AWS IP查询
            "https://api.ip.sb/ip",  // IP.SB
            "https://myip.dnsomatic.com",  // DNS-O-Matic
            "https://ipecho.net/plain"  // ipecho.net
        )

        for (api in ipApis) {
            try {
                val url = URL(api)
                val connection = if (api.startsWith("https")) {
                    url.openConnection() as HttpsURLConnection
                } else {
                    url.openConnection() as HttpURLConnection
                }
                connection.connectTimeout = 3000 // 设置连接超时为 3000 毫秒
                connection.readTimeout = 3000 // 设置读取超时为 3000 毫秒
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText().trim()
                    reader.close()

                    val ip = parseIpFromResponse(api, response)
                    if (isValidIpAddress(ip)) {
                        Log.d("TSApplication", "成功从 $api 获取到 IP: $ip")
                        return ip
                    } else {
                        Log.w("TSApplication", "从 $api 获取的响应不是有效IP: $response")
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

    /**
     * 解析不同API的响应格式，提取IP地址
     */
    private fun parseIpFromResponse(api: String, response: String): String {
        return try {
            Log.d("TSApplication", "解析API $api 的响应: $response")
            
            // 首先尝试直接提取IP地址（适用于大多数API）
            val ipPattern = "\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b".toRegex()
            val match = ipPattern.find(response)
            
            if (match != null) {
                val ip = match.value
                if (isValidIpAddress(ip)) {
                    return ip
                }
            }
            
            // 针对特定API的解析
            when {
                // 太平洋API - JSON格式
                api.contains("pconline.com.cn") -> {
                    try {
                        val jsonObject = JSONObject(response)
                        val ip = jsonObject.getString("ip")
                        Log.d("TSApplication", "太平洋API解析结果: $ip")
                        ip
                    } catch (e: Exception) {
                        Log.w("TSApplication", "太平洋API JSON解析失败: ${e.message}")
                        ""
                    }
                }
                
                // 检查是否包含HTML标签（说明返回了HTML页面）
                response.contains("<html", ignoreCase = true) || 
                response.contains("<!DOCTYPE", ignoreCase = true) -> {
                    Log.w("TSApplication", "API $api 返回了HTML页面而不是IP地址")
                    ""
                }
                
                // 检查是否是JSON格式
                response.startsWith("{") && response.endsWith("}") -> {
                    try {
                        val jsonObject = JSONObject(response)
                        // 尝试常见的JSON字段
                        val possibleFields = listOf("ip", "query", "origin", "client_ip")
                        for (field in possibleFields) {
                            if (jsonObject.has(field)) {
                                val ip = jsonObject.getString(field)
                                if (isValidIpAddress(ip)) {
                                    Log.d("TSApplication", "JSON API解析结果: $ip")
                                    return ip
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("TSApplication", "解析JSON失败: ${e.message}")
                    }
                    ""
                }
                
                // 默认情况：直接返回响应内容
                else -> {
                    val result = response.trim()
                    Log.d("TSApplication", "默认解析结果: $result")
                    result
                }
            }
        } catch (e: Exception) {
            Log.e("TSApplication", "解析API响应失败: ${e.message}")
            ""
        }
    }

    /**
     * 验证IP地址格式是否正确
     */
    private fun isValidIpAddress(ip: String): Boolean {
        if (ip.isBlank()) return false
        
        val parts = ip.split(".")
        if (parts.size != 4) return false
        
        for (part in parts) {
            try {
                val num = part.toInt()
                if (num < 0 || num > 255) return false
            } catch (e: NumberFormatException) {
                return false
            }
        }
        
        // 过滤掉无效的IP地址
        val invalidIps = listOf(
            "127.0.0.1",      // 本地回环地址
            "0.0.0.0",        // 无效地址
            "255.255.255.255", // 广播地址
            "169.254.0.0",    // 链路本地地址
            "192.168.0.0",    // 私有地址
            "10.0.0.0",       // 私有地址
            "172.16.0.0"      // 私有地址
        )
        
        // 检查是否是无效IP
        if (invalidIps.contains(ip)) {
            Log.w("TSApplication", "检测到无效IP地址: $ip")
            return false
        }
        
        // 检查是否是私有地址段
        val firstOctet = parts[0].toInt()
        val secondOctet = parts[1].toInt()
        
        when {
            // 10.0.0.0/8
            firstOctet == 10 -> {
                Log.w("TSApplication", "检测到私有地址段 10.x.x.x: $ip")
                return false
            }
            // 172.16.0.0/12
            firstOctet == 172 && secondOctet in 16..31 -> {
                Log.w("TSApplication", "检测到私有地址段 172.16-31.x.x: $ip")
                return false
            }
            // 192.168.0.0/16
            firstOctet == 192 && secondOctet == 168 -> {
                Log.w("TSApplication", "检测到私有地址段 192.168.x.x: $ip")
                return false
            }
        }
        
        return true
    }

    private fun showErrorAndRetryOption(exception: Exception) {
        // 显示错误对话框，提供重试按钮
        AlertDialog.Builder(this)
            .setTitle("网络错误")
            .setMessage("无法获取API配置，请检查网络连接后重试")
            .setPositiveButton("重试") { _, _ -> 
                // 清理可能存在的重复菜单项，然后重新获取配置
                EndpointManager.getInstance().clearCategory(EndpointCategory.personalCenter)
                EndpointManager.getInstance().clearCategory(EndpointCategory.mailList)
                EndpointManager.getInstance().clearCategory(EndpointCategory.chatFunction)
                EndpointManager.getInstance().clearCategory(EndpointCategory.tabMenus)
                
                // 重置API初始化状态
                TSApplication.getInstance().resetApiInitialized()
                
                // 重新获取配置
                getConfigAsync()
            }
            .setNegativeButton("退出") { _, _ -> finish() }
            .show()
    }
}
