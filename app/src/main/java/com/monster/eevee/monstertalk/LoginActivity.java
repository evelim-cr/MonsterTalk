package com.monster.eevee.monstertalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String LOGIN_USERNAME = "com.monster.eevee.monstertalk.LOGIN_USERNAME";
    private static final String TAG = "LoginActivity";
    private TextView mUsernameText;
    private TextView mPasswordText;
    private Button mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUsernameText = (TextView) findViewById(R.id.usernameText);
        mPasswordText = (TextView) findViewById(R.id.passwordText);
        mLoginButton = (Button) findViewById(R.id.loginButton);

        mLoginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == this.mLoginButton) {
            String username = this.mUsernameText.getText().toString();
            String password = this.mPasswordText.getText().toString();

            AuthManager authManager = AuthManager.getInstance();
            if (authManager.authenticate(username, password)) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else {
                // error
            }
        }
    }
}
