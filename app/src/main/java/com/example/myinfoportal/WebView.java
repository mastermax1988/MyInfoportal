package com.example.myinfoportal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;

import org.jboss.aerogear.security.otp.Totp;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class WebView extends AppCompatActivity {

    private int iMaxTries;
    private boolean parsingDone;
    private String postkorb;
    private android.webkit.WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_web_view);
        webView = findViewById(R.id.wv);
        Totp generator = new Totp(BuildConfig.TOTP_KEY);
        webView.getSettings().setJavaScriptEnabled(true);
        iMaxTries = savedInstanceState == null? 2: savedInstanceState.getInt("iMaxTries");
        parsingDone = savedInstanceState != null && savedInstanceState.getBoolean("parsingDone");
        postkorb = savedInstanceState == null? "" : savedInstanceState.getString("postkorb");
        webView.setWebContentsDebuggingEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(android.webkit.WebView view, String url) {
                if(parsingDone)
                    return;
                if(!url.contains("login"))
                {
                    System.out.println("user seems to be logged in -> grabbing postkorb and logging out");
                    webView.evaluateJavascript("(function() { return ('<html><table>'+document.getElementById('postkorb-box').innerHTML+'</table></html>'); })();", new CB());
                    String myCustomJS = "document.getElementsByClassName(\"logout_button\")[0].click()";
                    webView.loadUrl("javascript:(function(){" + myCustomJS + "})()");
                    iMaxTries = 0;
                    parsingDone = true;
                    loadPostkorb();
                }
                if(iMaxTries <=0)
                    return;
                iMaxTries--;
                System.out.println(url);
                super.onPageFinished(view, url);
                String myCustomJS = "try{document.getElementById('user').value='"+BuildConfig.USERNAME+"'; " +
                        "document.getElementById('password').value='"+BuildConfig.PASSWORD+"'; }catch(e){} " +
                        "try{document.getElementById('otp').value='"+generator.now()+"';}catch(e){} " +
                        "document.getElementById('submit_button').click();";
                System.out.println(myCustomJS);
                webView.loadUrl("javascript:(function(){" + myCustomJS + "})()");
            }
        });
        if(savedInstanceState == null)
            webView.loadUrl("https://schule-infoportal.de/login/wggymuc");
        else
            loadPostkorb();
    }


    private synchronized void loadPostkorb()
    {
        webView.loadDataWithBaseURL(null,postkorb,"text/html","utf-8",null);
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("iMaxTries",iMaxTries);
        outState.putBoolean("parsingDone", parsingDone);
        outState.putString("postkorb",postkorb);
    }
    private class CB implements ValueCallback<String>
    {
        @Override
        public void onReceiveValue(String value) {
            Properties p = new Properties();
            parsingDone = true;
            try {
                p.load(new StringReader("key="+value));
                value = p.getProperty("key").replaceAll("\n","");
                value = value.substring(1,value.length()-2);
                postkorb = value;
                loadPostkorb();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}