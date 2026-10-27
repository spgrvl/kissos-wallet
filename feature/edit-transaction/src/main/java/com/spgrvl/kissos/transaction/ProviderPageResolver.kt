package com.spgrvl.kissos.transaction

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Message
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.spgrvl.kissos.base.threading.DispatchersProvider
import com.spgrvl.kissos.data.receipt.ReceiptCode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Opens an e-invoicing provider's receipt page in a hidden WebView and finds its link to the document
 * on myDATA. Providers build these pages with JavaScript behind bot checks, so plain HTTP can't read them.
 * Some only open the link from a button, e.g. Epsilon Digital's "Go to AADE", so it's pressed once ready.
 */
class ProviderPageResolver @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val dispatchers: DispatchersProvider,
) {
    companion object {
        private const val TIMEOUT_MS = 30_000L
        private const val POLL_INTERVAL_MS = 500L

        private const val CLICK_INTERVAL_MS = 3_000

        /**
         * Returns a myDATA link in the page, or presses the page's AADE button so that it opens one.
         * The button only works once the page has passed its bot check, so it's pressed again every few seconds.
         */
        private const val FIND_LINK = """
            (function () {
                var link = document.querySelector('a[href*="TimologioQR"]');
                if (link) return link.href;
                var match = document.documentElement.outerHTML
                    .match(/https:\/\/mydatapi\.aade\.gr\/[^"'<>\s]+/);
                if (match) return match[0].replace(/&amp;/g, '&');
                var button = document.querySelector('button.aade-button');
                var now = Date.now();
                if (button && now - (window.kissosClickedAt || 0) > $CLICK_INTERVAL_MS) {
                    window.kissosClickedAt = now;
                    button.click();
                }
                return null;
            })()
        """
    }

    /**
     * @param onPageLoad runs once the provider's page has loaded and the link is being looked for.
     * @return the myDATA link, or null if the page didn't show one in time.
     */
    @SuppressLint("SetJavaScriptEnabled")
    suspend fun findMyDataLink(
        provider: ReceiptCode.Provider,
        onPageLoad: () -> Unit,
    ): ReceiptCode.MyData? = withContext(dispatchers.main) {
        val webView = WebView(context)
        val popups = mutableListOf<WebView>()
        var openedLink: String? = null
        var pageLoaded = false
        try {
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            // The AADE button may open the link in a new window
            webView.settings.setSupportMultipleWindows(true)
            webView.settings.javaScriptCanOpenWindowsAutomatically = true
            val catchMyDataLink = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val url = request.url.toString()
                    val isMyData = ReceiptCode.parse(url) is ReceiptCode.MyData
                    if (isMyData) openedLink = url
                    return isMyData
                }

                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    if (ReceiptCode.parse(url) is ReceiptCode.MyData) {
                        openedLink = url
                        view.stopLoading()
                    }
                }

                override fun onPageFinished(view: WebView, url: String) {
                    if (view == webView && !pageLoaded) {
                        pageLoaded = true
                        onPageLoad()
                    }
                }
            }
            webView.webViewClient = catchMyDataLink
            webView.webChromeClient = object : WebChromeClient() {
                override fun onCreateWindow(
                    view: WebView,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message,
                ): Boolean {
                    // A throwaway window that only reports where it's sent
                    val window = WebView(context).apply { webViewClient = catchMyDataLink }
                    popups += window
                    (resultMsg.obj as WebView.WebViewTransport).webView = window
                    resultMsg.sendToTarget()
                    return true
                }
            }
            webView.loadUrl(provider.url)
            withTimeoutOrNull(TIMEOUT_MS) {
                var link: ReceiptCode.MyData? = null
                while (link == null) {
                    delay(POLL_INTERVAL_MS)
                    link = (openedLink ?: webView.evaluate(FIND_LINK))
                        ?.let { ReceiptCode.parse(it) as? ReceiptCode.MyData }
                }
                link
            }.also {
                if (it == null) Timber.w("No myDATA link found on %s", provider.url)
            }
        } finally {
            (popups + webView).forEach {
                it.stopLoading()
                it.destroy()
            }
        }
    }

    private suspend fun WebView.evaluate(script: String): String? = suspendCancellableCoroutine { continuation ->
        evaluateJavascript(script) { json ->
            // The result is JSON: a quoted string or null
            continuation.resume(json?.let { JSONArray("[$it]").opt(0) as? String })
        }
    }
}
