package com.project.rms.Webview

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.project.rms.App
import com.project.rms.MainActivity
import com.project.rms.R
import java.net.URLEncoder

class ssy_Webview : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ssy_webview)

        //액션바 숨기기
        val actionBar = getSupportActionBar()
        if (actionBar != null) {
            actionBar.hide()
        }
        // 추가
        var myWebView: WebView = findViewById(R.id.webview)
        myWebView.webViewClient = WebViewClient()
        myWebView.loadUrl(App.prefs.NewsSite.toString())

        /*
        var str_encode = URLEncoder.encode("하이", "UTF-8") //검색어 인코딩해서 넣기 셰어드
        myWebView.loadUrl("https://www.youtube.com/results?search_query="+str_encode)
        유튜브 : "https://www.youtube.com/results?search_query="+str_encode
        유튭브 : "https://www.youtube.com"
        구글  : "https://www.google.co.kr/search?q="+str_encode
        구글  : "https://www.google.co.kr"
        쉐어드로 주소값 받기기*/
    }
}