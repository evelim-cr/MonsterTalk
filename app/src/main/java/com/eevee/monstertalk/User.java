package com.eevee.monstertalk;

import java.io.Serializable;

public class User implements Serializable {
    private String mUsername;

    public User(String username) {
        mUsername = username;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }
}
