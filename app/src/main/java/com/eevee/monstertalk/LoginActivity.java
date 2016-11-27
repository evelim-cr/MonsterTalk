package com.eevee.monstertalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.eevee.monstertalk.monstertalk.R;

import java.net.URI;
import java.net.URISyntaxException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";

    private TextView mHostnameView;
    private TextView mUsernameView;
    private TextView mPasswordView;
    private Button mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mHostnameView = (TextView) findViewById(R.id.hostnameText);
        mUsernameView = (TextView) findViewById(R.id.usernameText);
        mPasswordView = (TextView) findViewById(R.id.passwordText);
        mLoginButton = (Button) findViewById(R.id.loginButton);

        mHostnameView.setText("http://192.168.25.6:8887");

        mLoginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == this.mLoginButton) {
            String hostname = this.mHostnameView.getText().toString();
            String username = this.mUsernameView.getText().toString();
            String password = this.mPasswordView.getText().toString();

            SocketManager sockManager = SocketManager.getInstance();

            sockManager.setOnConnectListener(new SocketManager.OnConnectListener() {
                @Override
                public void onConnect() {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });

            try {
                sockManager.connect(new URI(hostname), username, password);
            }
            catch (URISyntaxException e) {
                Log.e(TAG, "Hostname in bad format.");
            }
            catch (SocketManager.AlreadyConnectedException e) {
                Log.e(TAG, "Socket is already connected!");
            }
        }
    }
}
