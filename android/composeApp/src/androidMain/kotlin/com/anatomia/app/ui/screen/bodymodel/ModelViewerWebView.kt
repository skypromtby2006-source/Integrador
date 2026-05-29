package com.anatomia.app.ui.screen.bodymodel

import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import com.anatomia.app.network.BASE_URL

// ── Bridge JavaScript → Kotlin ────────────────────────────────────────────────
class OrganBridge(
    private val onModelLoaded: () -> Unit,
    private val onModelTapped: () -> Unit
) {
    @JavascriptInterface
    fun onModelLoaded() = onModelLoaded.invoke()

    @JavascriptInterface
    fun onModelTapped() = onModelTapped.invoke()
}

// ── Composable del WebView ────────────────────────────────────────────────────
@Composable
fun ModelViewerWebView(
    organId: String,
    organName: String,
    modifier: Modifier = Modifier,
    onModelLoaded: () -> Unit = {},
    onModelTapped: () -> Unit = {}
) {
    val modelUrl = when (organId) {
        "heart"   -> "$BASE_URL/models/organ_heart.glb"
        "lungs"   -> "$BASE_URL/models/organ_lungs.glb"
        "kidneys" -> "$BASE_URL/models/organ_kidneys.glb"
        else      -> "$BASE_URL/models/organ_heart.glb"
    }

    AndroidView(
        modifier = modifier,
        factory  = { context ->
            val assetLoader = WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
                .build()

            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    allowFileAccess   = true
                    mixedContentMode  = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    cacheMode         = WebSettings.LOAD_DEFAULT
                    domStorageEnabled = true
                }

                addJavascriptInterface(
                    OrganBridge(
                        onModelLoaded = onModelLoaded,
                        onModelTapped = onModelTapped
                    ),
                    "AndroidBridge"
                )

                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest
                    ): WebResourceResponse? {
                        return assetLoader.shouldInterceptRequest(request.url)
                    }

                    override fun onPageFinished(view: WebView, url: String) {
                        android.util.Log.d("ANATOMIA", "Página cargada: $url — modelo: $modelUrl")
                    }
                }

                loadUrl("$BASE_URL/viewer/$organId")
            }
        },
        update = { webView ->
            android.util.Log.d("ANATOMIA", "Tab cambiado → recargando viewer para: $organId")
            webView.loadUrl("$BASE_URL/viewer/$organId")
        }
    )
}
