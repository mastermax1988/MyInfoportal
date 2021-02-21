package com.example.myinfoportal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.TextView;


import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jboss.aerogear.security.otp.Totp;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    Button tokenBtn;
    RequestQueue requestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button).setOnClickListener((e)->{
            Intent intent = new Intent(this,WebView.class);
            startActivity(intent);
        });
        findViewById(R.id.btnBrowser).setOnClickListener((e)->
        {
            Totp generator = new Totp(BuildConfig.TOTP_KEY);
            String token = generator.now();
            ClipboardManager cp = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cp.setPrimaryClip(ClipData.newPlainText("Infoportal",token));
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://schule-infoportal.de/login/wggymuc"));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setPackage("com.android.chrome");
            startActivity(i);
        });
        tokenBtn = findViewById(R.id.btnToken);
        tokenBtn.setOnClickListener((e)-> {
            UpdateTokenBtn();
        });
        findViewById(R.id.btnRequest).setOnClickListener((e)-> {
            requestQueue = Volley.newRequestQueue(this);
            Totp generator = new Totp(BuildConfig.TOTP_KEY);
            try {
                String url ="https://schule-infoportal.de/login/wggymuc";
                //requestQueue.add(getLoginString());
                requestQueue.add(getStringRequest(url, null, new MyNetworkHandler() {
                    @Override
                    public void handle(NetworkResponse networkResponse) {
                        String cookie = networkResponse.headers.get("Set-Cookie").split(";")[0];
                        Map<String, String > params = new HashMap<String, String>();
                        params.put("user",BuildConfig.USERNAME);
                        params.put("password", BuildConfig.PASSWORD);
                        requestQueue.add(postStringRequest(url, params, cookie, new MyNetworkHandler() {
                            @Override
                            public void handle(NetworkResponse networkResponse) {
                               Map<String, String > params2 = new HashMap<String, String>();
                               params2.put("otp",generator.now());
                               requestQueue.add(postStringRequest(url, params2, cookie, new MyNetworkHandler() {
                                   @Override
                                   public void handle(NetworkResponse networkResponse) {
                                       System.out.println(networkResponse);
                                       System.out.println(networkResponse.headers);
                                   }
                               }));
                            }
                        }));
                    }
                }));
            }
            catch (Exception ex){
                System.out.println(ex);
            };
        });
    }
    private interface MyNetworkHandler
    {
        void handle(NetworkResponse networkResponse);
    }
    private StringRequest getStringRequest(String url, String cookie, MyNetworkHandler myNetworkHandler)
    {
        return new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("get-response: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String > oldheaders = super.getHeaders();
                Map<String, String> headers = new HashMap<String, String >();
                if(cookie== null || cookie.equals(""))
                    return headers;
                headers.put("Cookie", cookie);
                return headers;
            }
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                if (response != null) {
                    System.out.println("get-parse: " + response.headers);
                    myNetworkHandler.handle(response);
                    // can get more details such as response.headers
                }
                return super.parseNetworkResponse(response);
            }
        };
    }
    private StringRequest postStringRequest(String url, Map<String, String> params, String cookie, MyNetworkHandler myNetworkHandler )
    {
       try {
            JSONObject jsonBody = new JSONObject();
            Totp generator = new Totp(BuildConfig.TOTP_KEY);
            jsonBody.put("otp", generator.now());
            final String requestBody = jsonBody.toString();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    System.out.println("post-response: " + response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("error: " + error.toString());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("Cookie", cookie);
                    return headers;
                }
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    return params;
                }
                @Override
                public String getBodyContentType() {
                    return "application/x-www-form-urlencoded";
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    if (response != null) {
                        System.out.println("post-parse: " + response.headers);
                        myNetworkHandler.handle(response);
                        // can get more details such as response.headers
                    }
                    return super.parseNetworkResponse(response);
                }

            };
            return stringRequest;
        }
        catch (Exception ex)
        {
            return null;
        }
    }


    private void UpdateTokenBtn()
    {
        Totp generator = new Totp(BuildConfig.TOTP_KEY);
        String token = generator.now();
        ClipboardManager cp = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cp.setPrimaryClip(ClipData.newPlainText("Infoportal",token));
        tokenBtn.setText("Token: " + token);
        System.out.println("Button updated");
    }

    @Override
    protected void onResume() {
        super.onResume();
        UpdateTokenBtn();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


}