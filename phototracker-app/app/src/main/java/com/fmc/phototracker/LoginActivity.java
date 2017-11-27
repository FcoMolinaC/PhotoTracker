package com.fmc.phototracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    boolean login;

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
        Button button_register = findViewById(R.id.button_register);
        final EditText user = findViewById(R.id.TxtInputUser);
        final EditText pass = findViewById(R.id.TxtInputPass);
        TextView textO = findViewById(R.id.textView2);
        TextView textRegister = findViewById(R.id.textView3);

        Typeface aganelight = Typeface.createFromAsset(getAssets(), "font/agane.ttf");
        Typeface aganebold = Typeface.createFromAsset(getAssets(), "font/aganebold.ttf");

        button_sesion.setTypeface(aganebold);
        button_explore.setTypeface(aganelight);
        user.setTypeface(aganelight);
        pass.setTypeface(aganelight);
        textO.setTypeface(aganelight);
        textRegister.setTypeface(aganelight);
        button_register.setTypeface(aganebold);

        button_sesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = user.getText().toString();
                String password = pass.getText().toString();

                if (username.matches("")) {
                    Toast.makeText(LoginActivity.this, "Tienes que introducir un usuario válido", Toast.LENGTH_SHORT).show();
                } else if (password.matches("")) {
                    Toast.makeText(LoginActivity.this, "Tienes que introducir la contraseña", Toast.LENGTH_SHORT).show();
                } else {
                    //ToDo: Comprobar con base de datos cuando esté implementado
                    //ToDo: Crear sentencia de control para acceder al menú personal
                    login = true;
                    Intent mainIntent = new Intent().setClass(
                            LoginActivity.this, MainActivity.class);
                    mainIntent.putExtra("login", login);
                    startActivity(mainIntent);
                    finish();
                }
            }
        });

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.register_dialog, null);
                final EditText etUsername = alertLayout.findViewById(R.id.username);
                final EditText etEmail = alertLayout.findViewById(R.id.password);

                AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
                alert.setTitle("Registro");
                alert.setView(alertLayout);
                alert.setCancelable(false);
                alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getBaseContext(), "Cancelado", Toast.LENGTH_SHORT).show();
                    }
                });

                alert.setPositiveButton("Registrar", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String user = etUsername.getText().toString();
                        String pass = etEmail.getText().toString();
                        Toast.makeText(getBaseContext(), "Usuario: " + user + " Contraseña: " + pass, Toast.LENGTH_SHORT).show();
                    }
                });
                AlertDialog dialog = alert.create();
                dialog.show();
            }
        });

        button_explore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ToDo: Comprobar con base de datos cuando esté implementado
                //ToDo: Crear sentencia de control para acceder al menú personal
                login = false;
                Intent mainIntent = new Intent().setClass(
                        LoginActivity.this, MainActivity.class);
                mainIntent.putExtra("login", login);
                startActivity(mainIntent);
                finish();
            }
        });
    }
}
