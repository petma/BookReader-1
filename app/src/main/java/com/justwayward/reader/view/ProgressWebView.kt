/**
 * Copyright 2016 JustWayward Team
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.justwayward.reader.view

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.ProgressBar

import com.justwayward.reader.R

import butterknife.Bind
import butterknife.ButterKnife

 class ProgressWebView @JvmOverloads  constructor(private val mContext:Context, attrs:AttributeSet? = null, defStyle:Int = 0):LinearLayout(mContext, attrs, defStyle) {

@Bind(R.id.web_view)
internal var mWebView:WebView? = null

@Bind(R.id.progress_bar)
internal var mProgressBar:ProgressBar? = null
 var url:String? = null

init{
initView(mContext)
}

private fun initView(context:Context) {
View.inflate(context, R.layout.layout_web_progress, this)
ButterKnife.bind(this)
}

 fun loadUrl(url:String) {
var url = url
if (TextUtils.isEmpty(url))
{
url = "https://github.com/JustWayward/BookReader"
}
initWebview(url)
}


private fun initWebview(url:String) {

mWebView!!.addJavascriptInterface(this, "android")

val webSettings = mWebView!!.getSettings()

webSettings.setJavaScriptEnabled(true)
 // 设置可以访问文件
        webSettings.setAllowFileAccess(true)
 // 设置可以支持缩放
        webSettings.setSupportZoom(true)
 // 设置默认缩放方式尺寸是far
        webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM)
 // 设置出现缩放工具
        webSettings.setBuiltInZoomControls(false)
webSettings.setDefaultFontSize(16)

mWebView!!.loadUrl(url)

 // 设置WebViewClient
        mWebView!!.setWebViewClient(object:WebViewClient() {
 // url拦截
            public override fun shouldOverrideUrlLoading(view:WebView, url:String):Boolean {
 // 使用自己的WebView组件来响应Url加载事件，而不是使用默认浏览器器加载页面
                view.loadUrl(url)
 // 相应完成返回true
                return true
 // return super.shouldOverrideUrlLoading(view, url);
            }

 // 页面开始加载
            public override fun onPageStarted(view:WebView, url:String, favicon:Bitmap) {
mProgressBar!!.setVisibility(View.VISIBLE)
super.onPageStarted(view, url, favicon)
}

 // 页面加载完成
            public override fun onPageFinished(view:WebView, url:String) {
mProgressBar!!.setVisibility(View.GONE)
super.onPageFinished(view, url)
}

 // WebView加载的所有资源url
            public override fun onLoadResource(view:WebView, url:String) {
super.onLoadResource(view, url)
}

public override fun onReceivedError(view:WebView, errorCode:Int, description:String, failingUrl:String) {
 //				view.loadData(errorHtml, "text/html; charset=UTF-8", null);
                super.onReceivedError(view, errorCode, description, failingUrl)
}

})

 // 设置WebChromeClient
        mWebView!!.setWebChromeClient(object:WebChromeClient() {
public override// 处理javascript中的alert
 fun onJsAlert(view:WebView, url:String, message:String, result:JsResult):Boolean {
return super.onJsAlert(view, url, message, result)
}

public override// 处理javascript中的confirm
 fun onJsConfirm(view:WebView, url:String, message:String, result:JsResult):Boolean {
return super.onJsConfirm(view, url, message, result)
}

public override// 处理javascript中的prompt
 fun onJsPrompt(view:WebView, url:String, message:String, defaultValue:String, result:JsPromptResult):Boolean {
return super.onJsPrompt(view, url, message, defaultValue, result)
}

 // 设置网页加载的进度条
            public override fun onProgressChanged(view:WebView, newProgress:Int) {
mProgressBar!!.setProgress(newProgress)
super.onProgressChanged(view, newProgress)
}

 // 设置程序的Title
            public override fun onReceivedTitle(view:WebView, title:String) {
super.onReceivedTitle(view, title)
}
})
mWebView!!.setOnKeyListener(object:View.OnKeyListener {
public override fun onKey(v:View, keyCode:Int, event:KeyEvent):Boolean {
if (event.getAction() == KeyEvent.ACTION_DOWN)
{
if (keyCode == KeyEvent.KEYCODE_BACK && mWebView!!.canGoBack())
{ // 表示按返回键

mWebView!!.goBack() // 后退

 // webview.goForward();//前进
                        return true // 已处理
}
}
return false
}
})
}

 fun canBack():Boolean {
if (mWebView!!.canGoBack())
{
mWebView!!.goBack()
return false
}
return true
}
}