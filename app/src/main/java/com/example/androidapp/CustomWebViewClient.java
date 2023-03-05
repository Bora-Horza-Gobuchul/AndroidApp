package com.example.androidapp;

import android.app.Activity;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CustomWebViewClient extends WebViewClient {
    private Activity activity;

    public CustomWebViewClient(Activity activity){
        this.activity = activity;
    }

    //for API<24
    @Override
    public boolean shouldOverrideUrlLoading(WebView webView, String url){
        return false;
    }

    //for API 24+
    @Override
    public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request){
        return false;
    }
}
