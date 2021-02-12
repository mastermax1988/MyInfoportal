package com.example.myinfoportal;

import androidx.annotation.NonNull;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;


import org.jboss.aerogear.security.otp.Totp;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Totp generator = new Totp(BuildConfig.TOTP_KEY);
        TextView txt = findViewById(R.id.txt);
        txt.setText("Login-Token:\n" + generator.now());
        ClipboardManager cp = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cp.setPrimaryClip(ClipData.newPlainText("",generator.now()));
        findViewById(R.id.button).setOnClickListener((e)->{
            Intent intent = new Intent(this,WebView.class);
            startActivity(intent);
        });
    }
}