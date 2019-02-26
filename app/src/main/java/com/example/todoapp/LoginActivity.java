package com.example.todoapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Handler handler;
    private EditText edit_address;
    private EditText edit_password;
    private Button button_login;
    private TextView text_signup;

    private String address = null;
    private String password = null;

    private String Uid = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edit_address = findViewById(R.id.edit_email);
        edit_password = findViewById(R.id.edit_password);
        button_login = findViewById(R.id.button_login);
        text_signup = findViewById(R.id.text_signup);

        button_login.setOnClickListener(this);
        text_signup.setOnClickListener(this);

    }

    public void onClick(View view) {

        handler = new Handler();

        address = edit_address.getText().toString();
        password = edit_password.getText().toString();

        switch(view.getId()) {
            case R.id.button_login:
                if (!address.equals("") && !password.equals("")) {
                    login(address, password);
                } else {
                    showInformationError(address, password);
                }
                break;
            case R.id.text_signup:
                loadSignupActivity();
                break;
            default:
                break;
        }
    }

    private void login(String address, String password) {

        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uid", address).put("email", address).put("password", password);
            RequestBody body = RequestBody.create(JSON, jsonObject.toString());

            Request request = new Request.Builder()
                    .url("https://life-server-staging.herokuapp.com/api/auth/sign_in")
                    .post(body)
                    .build();

            final OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) {
                    System.out.println(response.code());
                    Headers responses = response.headers();
                    if (response.code() == 401) {
                        // 別スレッドを実行
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // Handlerを使用してメイン(UI)スレッドに処理を依頼する
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showLoginError();
                                    }
                                });
                            }
                        }).start();
                    } else {
                        Uid = responses.get("uid").toString();

                        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("Uid", Uid);
                        editor.commit();

                        loadMainActivity();
                    }
                }
            });


        } catch(JSONException je) {
            je.printStackTrace();
        }
    }

    private void loadMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void loadSignupActivity() {
        Intent intent = new Intent(this, SignupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // エラーメッセージ表示
    private void showInformationError(String userAddress, String userPassword) {
        if (userAddress.equals("")) {
            Toast toast = Toast.makeText(this, "メールアドレスを入力してください", Toast.LENGTH_SHORT);
            toast.show();
        } else if (userPassword.equals("")) {
            Toast toast = Toast.makeText(this, "パスワードを入力してください", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    // エラーメッセージ表示
    private void showLoginError() {
        Toast toast = Toast.makeText(getApplicationContext(), "ユーザー情報が正しくありません", Toast.LENGTH_SHORT);
        toast.show();
    }
}
