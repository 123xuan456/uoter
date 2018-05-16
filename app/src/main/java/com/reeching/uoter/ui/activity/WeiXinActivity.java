package com.reeching.uoter.ui.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.reeching.uoter.R;

import java.util.HashMap;
import java.util.Map;

import ren.yale.android.cachewebviewlib.CacheWebView;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class WeiXinActivity extends AppCompatActivity {

    private CacheWebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = (CacheWebView) findViewById(R.id.web);

        WebSettings settings = mWebView.getSettings();
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // 设置与Js交互的权限
        settings.setJavaScriptEnabled(true);
        // 设置允许JS弹窗
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setMinimumFontSize(settings.getMinimumFontSize() + 8);
        settings.setAllowFileAccess(false);
        settings.setTextSize(WebSettings.TextSize.NORMAL);
        mWebView.setVerticalScrollbarOverlay(true);
        settings.setDomStorageEnabled(true);//开启缓存vue的前段页面必须添加缓存，否则页面跳转失败
        //硬件加速
        mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        mWebView.loadUrl("file:///android_asset/index.html"); //加载assets文件中的H5页面
//        mWebView.loadUrl("http://192.168.3.41:8081/index.html");
//        CacheWebView.servicePreload(this,URL);//通过启动Service来预加载，不影响UI线程
        CacheWebView.cacheWebView(this).loadUrl(URL);//要放在UI线程
        mWebView.setWebViewClient(new MyWebViewClient());
    }
    private class MyWebViewClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // 如下方案可在非微信内部WebView的H5页面中调出微信支付
            if (url.startsWith("weixin://wap/pay?")) {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(WeiXinActivity.this, "请安装微信最新版！", Toast.LENGTH_SHORT).show();
                }
            } else {
                Map<String, String> extraHeaders = new HashMap<String, String>();
                extraHeaders.put("Referer", "http:www.reeching.com");
                view.loadUrl(url, extraHeaders);
            }
            return true;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.removeAllViews();
            try {
                mWebView.destroy();
            } catch (Throwable t) {
            }
            mWebView = null;
        }
    }
}
