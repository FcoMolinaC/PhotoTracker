package com.fmc.phototracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
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

import com.fmc.phototracker.model.User;

public class LoginActivity extends AppCompatActivity {
    EditText usertext;
    EditText passtext;
    EditText etUsername;
    EditText etEmail;
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

        button_sesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = usertext.getText().toString();
                String password = passtext.getText().toString();

                if (username.matches("")) {
                    Toast.makeText(LoginActivity.this, "Tienes que introducir un usuario válido", Toast.LENGTH_SHORT).show();
                } else if (password.matches("")) {
                    Toast.makeText(LoginActivity.this, "Tienes que introducir la contraseña", Toast.LENGTH_SHORT).show();
                } else {
                    new WebService_login(LoginActivity.this).execute();
                }
            }
        });

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.register_dialog, null);
                etUsername = alertLayout.findViewById(R.id.username);
                etEmail = alertLayout.findViewById(R.id.password);

                AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
                alert.setTitle("Registro");
                alert.setView(alertLayout);
                alert.setCancelable(false);
                alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                alert.setPositiveButton("Registrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new WebService_insert(LoginActivity.this).execute();
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

    private boolean login() {
        return false;
    }

    public boolean insert() {
        return false;
    }

    class WebService_login extends AsyncTask<String, String, String> {
        private Activity context;

        WebService_login(Activity context) {
            this.context = context;
        }

        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            String result;

            if (login())
                result = "OK";
            else
                result = "ERROR";
            return result;
        }

        protected void onPostExecute(String result) {
            if (result.equals("OK")) {
                login = true;
                Intent mainIntent = new Intent().setClass(
                        LoginActivity.this, MainActivity.class);
                mainIntent.putExtra("login", login);
                startActivity(mainIntent);
                finish();
            } else
                Toast.makeText(context, "Error, no se ha podido encontrar al usuario", Toast.LENGTH_SHORT).show();
        }
    }

    class WebService_insert extends AsyncTask<String, String, String> {
        private Activity context;

        WebService_insert(Activity context) {
            this.context = context;
        }

        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            String result;

            if (insert())
                result = "OK";
            else
                result = "ERROR";
            return result;
        }

        protected void onPostExecute(String result) {
            if (result.equals("OK")) {
                Toast.makeText(context, "Registro completado", Toast.LENGTH_SHORT).show();
                login = true;
                Intent mainIntent = new Intent().setClass(
                        LoginActivity.this, MainActivity.class);
                mainIntent.putExtra("login", login);
                startActivity(mainIntent);
                finish();
            } else
                Toast.makeText(context, "Error, no se ha podido completar el registro", Toast.LENGTH_SHORT).show();
        }
    }
}
