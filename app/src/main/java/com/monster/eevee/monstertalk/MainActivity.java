package com.monster.eevee.monstertalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AuthManager authManager = AuthManager.getInstance();

        if (!authManager.isAuthenticated()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        this.mUser = authManager.getAuthenticatedUser();

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText("Bem-vindo " + this.mUser.getUsername() + ".");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
