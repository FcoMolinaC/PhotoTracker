package com.fmc.phototracker;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        Button button_sesion = findViewById(R.id.button_sesion);
        Button button_explore = findViewById(R.id.button_explore);
        Typeface aganelight = Typeface.createFromAsset(getAssets(), "font/agane.ttf");
        Typeface aganebold = Typeface.createFromAsset(getAssets(), "font/aganebold.ttf");

        button_sesion.setTypeface(aganebold);
        button_explore.setTypeface(aganelight);
    }
}
