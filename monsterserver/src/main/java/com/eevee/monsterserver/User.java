package com.eevee.monsterserver;

import com.pusher.java_websocket.WebSocket;

public class User {
    private String mUsername;
    private String mPassword;
    private boolean mConnected;
    private WebSocket mSocket;

    public User(String username, String password) {
        mUsername = username;
        mPassword = password;
        mConnected = false;
        mSocket = null;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setConnected(boolean connected) {
        mConnected = connected;
    }

    public boolean isConnected() {
        return mConnected;
    }

    public WebSocket getSocket() {
        return mSocket;
    }

    public void setSocket(WebSocket socket) {
        mSocket = socket;
    }
}
