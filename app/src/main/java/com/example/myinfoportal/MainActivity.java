package com.example.myinfoportal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;


import org.jboss.aerogear.security.otp.Totp;



public class MainActivity extends AppCompatActivity {
    private Totp generator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        generator = new Totp(BuildConfig.HOTP_KEY);
        TextView txt = findViewById(R.id.txt);
        txt.setText("Login-Token:\n" + generator.now());
    }
}