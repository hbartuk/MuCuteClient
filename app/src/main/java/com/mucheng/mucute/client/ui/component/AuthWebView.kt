package com.mucheng.mucute.client.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.JsonObject
import com.mucheng.mucute.client.game.AccountManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession
import java.net.URL
import java.util.UUID
import javax.net.ssl.HttpsURLConnection

/**
 * WebView, который обрабатывает авторизацию Xbox и Bedrock через Microsoft.
 */
@SuppressLint("SetJavaScriptEnabled")
class AuthWebView(context: Context) : WebView(context) {

    /** Callback вызывается по завершении авторизации (Throwable? = null если успех) */
    var callback: ((Throwable?) -> Unit)? = null

    init {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        webViewClient = AuthClient()
    }

    /** Запуск входа в Microsoft/Xbox */
    fun addAccount() {
        loadUrl(SISU_START_URL)
    }

    /**
     * Внутренний WebViewClient для перехвата редиректов
     */
    inner class AuthClient : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (url != null && url.startsWith(REDIRECT_URL)) {
                handleAuthRedirect(url)
            }
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url = request?.url.toString()
            if (url.startsWith(REDIRECT_URL)) {
                handleAuthRedirect(url)
                return true
            }
            return false
        }
    }

    /**
     * Обрабатывает редирект и выполняет токен-обмен
     */
    private fun handleAuthRedirect(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val authCode = url.substringAfter("code=").substringBefore("&")
                val (refreshToken, accessToken) = fetchIdentityToken(authCode)
                val rawChain = fetchRawChain(accessToken)
                val session = StepFullBedrockSession.FullBedrockSession(rawChain, refreshToken)

                AccountManager.addAccount(session)
                callback?.invoke(null)
            } catch (t: Throwable) {
                Log.e("AuthWebView", "Auth failed", t)
                callback?.invoke(t)
            }
        }
    }

    // -------------------------------
    //   Xbox/Microsoft Auth Helpers
    // -------------------------------

    private fun fetchIdentityToken(code: String): Pair<String, String> {
        val connection =
            URL("https://login.live.com/oauth20_token.srf").openConnection() as HttpsURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

        val data =
            "client_id=$APP_ID&code=$code&grant_type=authorization_code&redirect_uri=$REDIRECT_URL"

        connection.outputStream.use {
            it.write(data.toByteArray())
        }

        val response = connection.inputStream.bufferedReader().readText()
        val json = MinecraftAuth.GSON.fromJson(response, JsonObject::class.java)

        val refreshToken = json["refresh_token"].asString
        val accessToken = json["access_token"].asString

        return refreshToken to accessToken
    }

    private fun fetchRawChain(accessToken: String): JsonObject {
        val connection =
            URL("https://user.auth.xboxlive.com/user/authenticate").openConnection() as HttpsURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")

        val payload = """
            {
              "Properties": {
                "AuthMethod": "RPS",
                "SiteName": "user.auth.xboxlive.com",
                "RpsTicket": "d=$accessToken"
              },
              "RelyingParty": "http://auth.xboxlive.com",
              "TokenType": "JWT"
            }
        """.trimIndent()

        connection.outputStream.use {
            it.write(payload.toByteArray())
        }

        val response = connection.inputStream.bufferedReader().readText()
        val json = MinecraftAuth.GSON.fromJson(response, JsonObject::class.java)
        return json
    }

    // -------------------------------
    //   Constants + Helpers
    // -------------------------------

    companion object {
        private const val APP_ID = "00000000402b5328" // Xbox Live App ID
        private const val REDIRECT_URL = "https://login.live.com/oauth20_desktop.srf"
        private const val SISU_START_URL =
            "https://login.live.com/oauth20_authorize.srf?client_id=$APP_ID&response_type=code&redirect_uri=$REDIRECT_URL&scope=service::user.auth.xboxlive.com::MBI_SSL"
    }
}
