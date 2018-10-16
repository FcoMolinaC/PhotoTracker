package com.fmc.phototracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    EditText usertext;
    EditText passtext;
    EditText etUsername;
    EditText etPassword;
    EditText etEmail;
    boolean login;

    private FirebaseAuth auth;

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
        usertext = findViewById(R.id.TxtInputUser);
        passtext = findViewById(R.id.TxtInputPass);
        TextView textO = findViewById(R.id.textView2);
        TextView textRegister = findViewById(R.id.textView3);

        Typeface aganelight = Typeface.createFromAsset(getAssets(), "font/agane.ttf");
        Typeface aganebold = Typeface.createFromAsset(getAssets(), "font/aganebold.ttf");

        button_sesion.setTypeface(aganebold);
        button_explore.setTypeface(aganelight);
        usertext.setTypeface(aganelight);
        passtext.setTypeface(aganelight);
        textO.setTypeface(aganelight);
        textRegister.setTypeface(aganelight);
        button_register.setTypeface(aganebold);

        auth = FirebaseAuth.getInstance();

        button_sesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = usertext.getText().toString();
                final String password = passtext.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Tienes que introducir un email válido", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Tienes que introducir una contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }

                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    if (password.length() < 6) {
                                        Toast.makeText(LoginActivity.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "La contraseña no es válida", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    login = true;
                                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                    mainIntent.putExtra("login", login);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            }
                        });
            }
        });

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.register_dialog, null);
                etUsername = alertLayout.findViewById(R.id.username);
                etPassword = alertLayout.findViewById(R.id.password);
                etEmail = alertLayout.findViewById(R.id.email);

                AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
                alert.setTitle("Registro");
                alert.setView(alertLayout);
                alert.setCancelable(false);
                alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(LoginActivity.this, "Registro cancelado", Toast.LENGTH_SHORT).show();
                    }
                });

                alert.setPositiveButton("Registrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = etPassword.getText().toString();
                        String email = etEmail.getText().toString();

                        if (TextUtils.isEmpty(email)) {
                            Toast.makeText(getApplicationContext(), "Tienes que introducir un email válido", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (TextUtils.isEmpty(password)) {
                            Toast.makeText(getApplicationContext(), "Tienes que introducir una contraseña", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (password.length() < 6) {
                            Toast.makeText(getApplicationContext(), "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        Toast.makeText(LoginActivity.this, "Registrado con éxito" + task.isSuccessful(), Toast.LENGTH_SHORT).show();

                                        if (!task.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "Registro fallido" + task.getException(),
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            login = true;
                                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                            mainIntent.putExtra("login", login);
                                            startActivity(mainIntent);
                                            finish();
                                        }
                                    }
                                });
                    }
                });
                AlertDialog dialog = alert.create();
                dialog.show();
            }
        });

        button_explore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
