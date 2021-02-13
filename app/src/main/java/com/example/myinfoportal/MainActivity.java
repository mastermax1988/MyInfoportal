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


import org.jboss.aerogear.security.otp.Totp;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    Button tokenBtn;

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