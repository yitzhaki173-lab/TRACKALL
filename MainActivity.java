package com.yitzhak.packtrackil;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;
    private BroadcastReceiver internalSmsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (uri != null && (uri.getScheme().equals("http") || uri.getScheme().equals("https"))) {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    return true;
                }
                return false;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, 101);
            }
        }

        webView.loadUrl("file:///android_asset/index.html");

        internalSmsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String smsBody = intent.getStringExtra("sms_body");
                if (smsBody != null && webView != null) {
                    String escaped = smsBody.replace("\\", "\\\\").replace("'", "\\'").replace("\n", " ");
                    webView.evaluateJavascript(
                        "(function(){ var ta = document.getElementById('smsRawTextarea'); var btn = document.getElementById('parseSmsNowBtn'); if(ta && btn){ ta.value = '" + escaped + "'; btn.click(); } })();",
                        null
                    );
                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(internalSmsReceiver, new IntentFilter("com.yitzhak.packtrackil.NEW_SMS"), Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(internalSmsReceiver, new IntentFilter("com.yitzhak.packtrackil.NEW_SMS"));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (internalSmsReceiver != null) {
            unregisterReceiver(internalSmsReceiver);
        }
    }
}
