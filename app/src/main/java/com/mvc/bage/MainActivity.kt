package com.mvc.bage

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.util.Log
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import com.chat.base.WKBaseApplication
import com.chat.base.base.WKBaseActivity
import com.chat.base.config.WKApiConfig
import com.chat.base.config.WKConfig
import com.chat.base.config.WKSharedPreferencesUtil
import com.chat.base.ui.components.NormalClickableContent
import com.chat.base.ui.components.NormalClickableSpan
import com.chat.base.utils.WKDialogUtils
import com.chat.login.ui.PerfectUserInfoActivity
import com.chat.login.ui.WKLoginActivity
import com.chat.uikit.TabActivity
import com.mvc.bage.databinding.ActivityMainBinding
import com.xinbida.wukongim.WKIM

class MainActivity : WKBaseActivity<ActivityMainBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AppFlow", "[MainActivity] onCreate called")

        // 检查应用是否已正确初始化
        if (!TSApplication.getInstance().isApiInitialized()) {
            Log.d("AppFlow", "[MainActivity] API not initialized, redirecting to SplashActivity")
            // 如果未初始化，重定向到SplashActivity
            val intent = Intent(this, SplashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
            return
        }
    }


    override fun getViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        Log.d("AppFlow", "[MainActivity] initView called")
        val isShowDialog: Boolean =
            WKSharedPreferencesUtil.getInstance().getBoolean("show_agreement_dialog")
        Log.d("AppFlow", "[MainActivity] show_agreement_dialog: $isShowDialog")
        if (isShowDialog) {
            Log.d("AppFlow", "[MainActivity] Redirecting to SplashActivity for agreement")
            // 将隐私协议放到 SplashActivity 统一处理，避免跳过 getConfig
            val intent = Intent(this, SplashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
            return
        } else {
            Log.d("AppFlow", "[MainActivity] Calling gotoApp")
            gotoApp()
        }
    }

    private fun gotoApp() {
        Log.d("AppFlow", "[MainActivity] gotoApp called")
        val token = WKConfig.getInstance().token
        Log.d("AppFlow", "[MainActivity] Token: ${if (token.isNullOrEmpty()) "empty" else "exists"}")
        
        if (!TextUtils.isEmpty(token)) {
            val userInfo = WKConfig.getInstance().userInfo
            Log.d("AppFlow", "[MainActivity] User name: ${if (userInfo.name.isNullOrEmpty()) "empty" else "exists"}")
            
            if (TextUtils.isEmpty(userInfo.name)) {
                Log.d("AppFlow", "[MainActivity] Starting PerfectUserInfoActivity")
                startActivity(Intent(this@MainActivity, PerfectUserInfoActivity::class.java))
            } else {
                val publicRSAKey: String = WKIM.getInstance().cmdManager.rsaPublicKey
                Log.d("AppFlow", "[MainActivity] RSA Key: ${if (publicRSAKey.isNullOrEmpty()) "empty" else "exists"}")
                
                if (TextUtils.isEmpty(publicRSAKey)) {
                    Log.d("AppFlow", "[MainActivity] Starting WKLoginActivity (no RSA key)")
                    val intent = Intent(this@MainActivity, WKLoginActivity::class.java)
                    intent.putExtra("from", getIntent().getIntExtra("from", 0))
                    startActivity(intent)
                } else {
                    Log.d("AppFlow", "[MainActivity] Starting TabActivity")
                    startActivity(Intent(this@MainActivity, TabActivity::class.java))
                }
            }
        } else {
            Log.d("AppFlow", "[MainActivity] Starting WKLoginActivity (no token)")
            val intent = Intent(this@MainActivity, WKLoginActivity::class.java)
            intent.putExtra("from", getIntent().getIntExtra("from", 0))
            startActivity(intent)
        }
        Log.d("AppFlow", "[MainActivity] Finishing MainActivity")
        finish()
    }

    private fun showDialog() {
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
                // 协议在 MainActivity 不再处理，交给 SplashActivity 继续后续流程
                val intent = Intent(this, SplashActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            } else {
                finish()
            }
        }
    }
}
