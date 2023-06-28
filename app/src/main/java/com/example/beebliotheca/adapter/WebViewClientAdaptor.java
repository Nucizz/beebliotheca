package com.example.beebliotheca.adapter;

import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewClientAdaptor extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

        view.loadUrl(String.valueOf(request.getUrl()));
        return false;
    }
}
