package com.example.euclidscompass

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.ViewGroup
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * The page itself (assets/euclids-compass.html) is a pristine copy of
 * web/euclids-compass.html, bundled by the `copyWeb` Gradle task before every
 * build. All Android-specific integration lives here instead of being
 * hand-edited into that file, so the web app never needs re-patching:
 *  - PNG export is caught via [WebView.setDownloadListener], which fires for
 *    the page's plain `<a download href="data:...">.click()` export button.
 *  - The "Clear all" confirm() dialog is handled via [WebChromeClient].
 *  - The mobile tool-ribbon layout/safe-area fix is injected as a <style>
 *    block (see [MOBILE_LAYOUT_PATCH_CSS]) into the HTML text before it's
 *    loaded, rather than baked into the file, since Android draws the
 *    WebView under the system bars on this device. This is done up front
 *    with loadDataWithBaseURL rather than via shouldInterceptRequest,
 *    because WebView never invokes that callback for the top-level
 *    file:///android_asset/ navigation — only for its sub-resources.
 */
class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private var pendingPngBase64: String? = null

    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val base64 = pendingPngBase64
            pendingPngBase64 = null
            if (granted && base64 != null) {
                savePngToGallery(base64)
            } else if (!granted) {
                Toast.makeText(this, "Storage permission is needed to save the PNG.", Toast.LENGTH_LONG).show()
            }
        }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#241509"))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            webChromeClient = object : WebChromeClient() {
                override fun onJsAlert(
                    view: WebView?, url: String?, message: String?, result: JsResult?
                ): Boolean {
                    AlertDialog.Builder(this@MainActivity)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm() }
                        .setOnCancelListener { result?.cancel() }
                        .show()
                    return true
                }

                override fun onJsConfirm(
                    view: WebView?, url: String?, message: String?, result: JsResult?
                ): Boolean {
                    AlertDialog.Builder(this@MainActivity)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm() }
                        .setNegativeButton(android.R.string.cancel) { _, _ -> result?.cancel() }
                        .setOnCancelListener { result?.cancel() }
                        .show()
                    return true
                }
            }

            setDownloadListener { url, _, _, mimeType, _ ->
                val commaIdx = url.indexOf(',')
                if (url.startsWith("data:") && mimeType?.startsWith("image/") == true && commaIdx != -1) {
                    handlePngDownload(url.substring(commaIdx + 1))
                } else {
                    Toast.makeText(this@MainActivity, "Unsupported download.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        setContentView(webView)

        val html = assets.open("euclids-compass.html").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val patchedHtml = html.replace("</head>", MOBILE_LAYOUT_PATCH_CSS + "</head>")
        webView.loadDataWithBaseURL(
            "file:///android_asset/", patchedHtml, "text/html", "UTF-8", null
        )
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    private fun handlePngDownload(base64: String) {
        val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            pendingPngBase64 = base64
            requestStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            savePngToGallery(base64)
        }
    }

    private fun savePngToGallery(base64: String) {
        try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val filename = "euclids-compass-${System.currentTimeMillis()}.png"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Euclid's Compass")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val resolver = contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri == null) {
                Toast.makeText(this, "Could not save PNG.", Toast.LENGTH_LONG).show()
                return
            }
            resolver.openOutputStream(uri)?.use { it.write(bytes) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            Toast.makeText(this, "Saved to Pictures/Euclid's Compass", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save PNG: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val MOBILE_LAYOUT_PATCH_CSS = """
<style id="android-mobile-layout-patch">
/* Android WebView wrapper patch: the tool ribbon moves above the canvas
   (instead of docking to the bottom) and safe-area padding is added, since
   Android draws the WebView under the status/nav bars. Depends on the
   upstream page's .app/.toolbar/.canvas-outer/.mobile-instruction/.sidebar
   selectors under the max-width:860px breakpoint; if upstream restructures
   the mobile layout, revisit this block. */
@media (max-width: 860px){
  .app{
    grid-template-rows: auto 74px 1fr !important;
    grid-template-areas: "header" "toolbar" "canvas" !important;
  }
  header{ padding-top: calc(10px + env(safe-area-inset-top)) !important; }
  .toolbar{ border-top:none !important; border-bottom:1px solid var(--walnut-seam) !important; }
  .canvas-outer{ padding-bottom: calc(10px + env(safe-area-inset-bottom)) !important; }
  .mobile-instruction{ bottom: calc(10px + env(safe-area-inset-bottom)) !important; }
  .sidebar{ padding-top:env(safe-area-inset-top) !important; padding-bottom:env(safe-area-inset-bottom) !important; }
}
</style>
"""
    }
}
