package com.example.myinfoportal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;

import org.jboss.aerogear.security.otp.Totp;

public class WebView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_web_view);
        android.webkit.WebView webView = findViewById(R.id.wv);
        Totp generator = new Totp(BuildConfig.TOTP_KEY);
        webView.getSettings().setJavaScriptEnabled(true);
        final int[] iMaxTries = {2}; //necessary for access in inner class
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(android.webkit.WebView view, String url) {
                if(iMaxTries[0] <=0)
                    return;
                iMaxTries[0]--;
                System.out.println(url);
                if(!url.contains("login"))
                {
                    System.out.println("no login page! aborting");
                    return;
                }
                super.onPageFinished(view, url);
                String myCustomJS = "try{document.getElementById('user').value='"+BuildConfig.USERNAME+"'; " +
                        "document.getElementById('password').value='"+BuildConfig.PASSWORD+"'; }catch(e){} " +
                        "try{document.getElementById('otp').value='"+generator.now()+"';}catch(e){} " +
                        "document.getElementById('submit_button').click();";
                System.out.println(myCustomJS);
                webView.loadUrl("javascript:(function(){" + myCustomJS + "})()");
            }
        });

        webView.loadUrl("https://schule-infoportal.de/login/wggymuc");
    }
}