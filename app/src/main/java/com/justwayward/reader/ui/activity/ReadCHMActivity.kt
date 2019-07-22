package com.justwayward.reader.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import androidx.core.view.MenuItemCompat
import androidx.appcompat.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseActivity
import com.justwayward.reader.base.Constant
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.view.chmview.Utils

import java.io.File
import java.io.IOException
import java.lang.reflect.Method
import java.util.ArrayList

import butterknife.Bind

/**
 * @author yuyh.
 * @date 2016/12/19.
 */
class ReadCHMActivity : BaseActivity() {

    @Bind(R.id.progressBar)
    internal var mProgressBar: ProgressBar? = null
    @Bind(R.id.webview)
    internal var mWebView: WebView? = null

    private var chmFileName: String? = null
    var chmFilePath = ""
    var extractPath: String
    var md5File: String

    private var listSite: ArrayList<String>? = null
    private var listBookmark: ArrayList<String>? = null

    override val layoutId: Int
        get() = R.layout.activity_read_chm

    override fun setupActivityComponent(appComponent: AppComponent?) {

    }

    override fun initToolBar() {
        chmFilePath = Uri.decode(intent.dataString!!.replace("file://", ""))
        chmFileName = chmFilePath.substring(chmFilePath.lastIndexOf("/") + 1, chmFilePath.lastIndexOf("."))
        mCommonToolbar!!.title = chmFileName
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
    }

    override fun initDatas() {
        Utils.chm = null
        listSite = ArrayList()
    }

    override fun configViews() {
        initVweView()

        initFile()
    }

    private fun initVweView() {
        mProgressBar!!.max = 100

        mWebView!!.settings.javaScriptEnabled = true
        mWebView!!.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                visible(mProgressBar)
                mProgressBar!!.progress = newProgress
            }
        }
        mWebView!!.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
                var url = url
                if (!url.startsWith("http") && !url.endsWith(md5File)) {
                    val temp = url.substring("file://".length)
                    if (!temp.startsWith(extractPath)) {
                        url = "file://$extractPath$temp"
                    }
                }

                super.onPageStarted(view, url, favicon)
                mProgressBar!!.progress = 50
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                mProgressBar!!.progress = 100
                gone(mProgressBar)
            }

            override fun onLoadResource(view: WebView, url: String) {
                var url = url
                if (!url.startsWith("http") && !url.endsWith(md5File)) {
                    val temp = url.substring("file://".length)
                    if (!temp.startsWith(extractPath)) {
                        url = "file://$extractPath$temp"
                    }
                }
                super.onLoadResource(view, url)
            }


            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                var url = request.url.toString()
                if (!url.startsWith("http") && !url.endsWith(md5File)) {
                    val temp = url.substring("file://".length)
                    var insideFileName: String
                    if (!temp.startsWith(extractPath)) {
                        url = "file://$extractPath$temp"
                        insideFileName = temp
                    } else {
                        insideFileName = temp.substring(extractPath.length)
                    }
                    if (insideFileName.contains("#")) {
                        insideFileName = insideFileName.substring(0, insideFileName.indexOf("#"))
                    }
                    if (insideFileName.contains("?")) {
                        insideFileName = insideFileName.substring(0, insideFileName.indexOf("?"))
                    }
                    if (insideFileName.contains("%20")) {
                        insideFileName = insideFileName.replace("%20".toRegex(), " ")
                    }
                    if (url.endsWith(".gif") || url.endsWith(".jpg") || url.endsWith(".png")) {
                        try {
                            return WebResourceResponse("image/*", "", Utils.chm!!.getResourceAsStream(insideFileName))
                        } catch (e: IOException) {
                            e.printStackTrace()
                            return super.shouldInterceptRequest(view, request)
                        }

                    } else if (url.endsWith(".css") || url.endsWith(".js")) {
                        try {
                            return WebResourceResponse("", "", Utils.chm!!.getResourceAsStream(insideFileName))
                        } catch (e: IOException) {
                            e.printStackTrace()
                            return super.shouldInterceptRequest(view, request)
                        }

                    } else {
                        Utils.extractSpecificFile(chmFilePath, extractPath + insideFileName, insideFileName)
                    }
                }
                Log.e("2, webviewrequest", url)
                return super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                var url = url
                if (!url.startsWith("http") && !url.endsWith(md5File)) {
                    val temp = url.substring("file://".length)
                    if (!temp.startsWith(extractPath)) {
                        url = "file://$extractPath$temp"
                        view.loadUrl(url)
                        return true
                    }
                }
                return false
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return shouldOverrideUrlLoading(view, request.url.toString())
            }
        }
        mWebView!!.settings.builtInZoomControls = true
        mWebView!!.settings.displayZoomControls = false
        mWebView!!.settings.useWideViewPort = true
        mWebView!!.settings.loadWithOverviewMode = true
        mWebView!!.settings.loadsImagesAutomatically = true
    }

    private fun initFile() {
        object : AsyncTask<Void, Void, Void>() {
            internal var historyIndex = 1

            override fun onPreExecute() {
                super.onPreExecute()
                showDialog()
            }

            override fun doInBackground(vararg voids: Void): Void? {
                md5File = Utils.checkSum(chmFilePath)
                extractPath = Constant.PATH_CHM + "/" + md5File
                if (!File(extractPath).exists()) {
                    if (Utils.extract(chmFilePath, extractPath)) {
                        try {
                            listSite = Utils.domparse(chmFilePath, extractPath, md5File)
                            listBookmark = Utils.getBookmark(extractPath, md5File)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    } else {
                        File(extractPath).delete()
                    }
                } else {
                    listSite = Utils.getListSite(extractPath, md5File)
                    listBookmark = Utils.getBookmark(extractPath, md5File)
                    historyIndex = Utils.getHistory(extractPath, md5File)
                }
                return null
            }

            override fun onPostExecute(aVoid: Void) {
                super.onPostExecute(aVoid)
                mWebView!!.loadUrl("file://" + extractPath + "/" + listSite!![historyIndex])
                hideDialog()
            }
        }.execute()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_chm_reader, menu)

        val searchMenuItem = menu.findItem(R.id.menu_search)//在菜单中找到对应控件的item
        val searchView = MenuItemCompat.getActionView(searchMenuItem) as SearchView
        searchView.setOnCloseListener {
            mWebView!!.clearMatches()
            false
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                mWebView!!.findAllAsync(newText)
                try {
                    for (m in WebView::class.java.declaredMethods) {
                        if (m.name == "setFindIsUp") {
                            m.isAccessible = true
                            m.invoke(mWebView, true)
                            break
                        }
                    }
                } catch (ignored: Exception) {
                }

                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    companion object {

        fun start(context: Context, filePath: String) {
            val intent = Intent(context, ReadCHMActivity::class.java)
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.fromFile(File(filePath))
            context.startActivity(intent)
        }
    }
}
