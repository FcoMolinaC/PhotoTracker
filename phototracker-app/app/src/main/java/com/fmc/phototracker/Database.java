package com.fmc.phototracker;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private HttpClient httpclient;
    private List<NameValuePair> param_POST;
    private HttpPost httppost;

    private String resultText;
    private int position = 0;
    private List<User> UserList = null;
    private boolean result = false;

    private final static String URL_SERVIDOR = "192.168.0.12";
    private final static String URL_PHP = "http://" + URL_SERVIDOR + "/phototrack/";

    public boolean insert(String username, String password) {
        httpclient = new DefaultHttpClient();
        httppost = new HttpPost(URL_PHP + "insert.php");

        param_POST = new ArrayList<NameValuePair>(4);
        param_POST.add(new BasicNameValuePair("username", username.trim()));
        param_POST.add(new BasicNameValuePair("password", password.trim()));

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    httppost.setEntity(new UrlEncodedFormEntity(param_POST));
                    httpclient.execute(httppost);
                    result = true;
                } catch (UnsupportedEncodingException e) {
                    result = false;
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    result = false;
                    e.printStackTrace();
                } catch (IOException e) {
                    result = false;
                    e.printStackTrace();
                }
            }
        });
        return result;
    }

    public boolean delete(String username) {
        httpclient = new DefaultHttpClient();
        httppost = new HttpPost(URL_PHP + "delete.php");

        param_POST = new ArrayList<NameValuePair>();

        User user = UserList.get(position);
        String id = String.valueOf(user.getId());
        param_POST.add(new BasicNameValuePair("id", id));

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    httppost.setEntity(new UrlEncodedFormEntity(param_POST));
                    httpclient.execute(httppost);
                    result = true;
                } catch (UnsupportedEncodingException e) {
                    result = false;
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    result = false;
                    e.printStackTrace();
                } catch (IOException e) {
                    result = false;
                    e.printStackTrace();
                }
            }
        });
        return result;
    }

    public boolean update(String username, String password) {
        httpclient = new DefaultHttpClient();
        httppost = new HttpPost(URL_PHP + "update.php");

        param_POST = new ArrayList<NameValuePair>(5);
        param_POST.add(new BasicNameValuePair("username", username.trim()));
        param_POST.add(new BasicNameValuePair("password", password.trim()));

        User user = UserList.get(position);
        String id = String.valueOf(user.getId());
        param_POST.add(new BasicNameValuePair("id", id));

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    httppost.setEntity(new UrlEncodedFormEntity(param_POST));
                    httpclient.execute(httppost);
                    result = true;
                } catch (UnsupportedEncodingException e) {
                    result = false;
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    result = false;
                    e.printStackTrace();
                } catch (IOException e) {
                    result = false;
                    e.printStackTrace();
                }
            }
        });
        return result;

    }

    public boolean login(String username, String password) {
        httpclient = new DefaultHttpClient();
        httppost = new HttpPost(URL_PHP + "login.php");

        param_POST = new ArrayList<NameValuePair>(4);
        param_POST.add(new BasicNameValuePair("username", username.trim()));
        param_POST.add(new BasicNameValuePair("password", password.trim()));

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    httppost.setEntity(new UrlEncodedFormEntity(param_POST));
                    httpclient.execute(httppost);
                    result = true;
                } catch (UnsupportedEncodingException e) {
                    result = false;
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    result = false;
                    e.printStackTrace();
                } catch (IOException e) {
                    result = false;
                    e.printStackTrace();
                }
            }
        });
        return result;
    }

    private String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;

            while ((line = reader.readLine()) != null)
                sb.append(line + "\n");

            resultText = sb.toString();

            Log.e("getpostresponse", " resultText= " + sb.toString());

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("log_tag", "Error E/S al convertir el resultText " + e.toString());

        } finally {
            try {
                if (is != null)
                    is.close();
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("log_tag", "Error E/S al cerrar los flujos de entrada " + e.toString());
            }

        }
        return resultText;
    }
}
