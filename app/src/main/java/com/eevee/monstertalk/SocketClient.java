package com.eevee.monstertalk;

import android.util.Log;

import com.pusher.java_websocket.client.WebSocketClient;
import com.pusher.java_websocket.handshake.ServerHandshake;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class SocketClient extends WebSocketClient {
    private static final String TAG = "SOCKET";

    private String mUsername;
    private String mPassword;
    private ConnectionListener mConnectionListener;
    private Map<String,MessageListener> mMessageListeners;
    private boolean mConnected;

    public class AuthenticationError extends Exception {
        public AuthenticationError(String message) {
            super(message);
        }
    }

    public interface ConnectionListener {
        void onConnect(User user);
        void onDisconnect();
        void onError(Exception e);
    }

    public interface MessageListener {
        void onMessage(JSONObject data);
    }

    public SocketClient(URI host, final String username, final String password) {
        super(host);
        mUsername = username;
        mPassword = password;
        mConnectionListener = null;
        mMessageListeners = new HashMap<>();
        mConnected = false;
    }

    public void setConnectionListener(ConnectionListener listener) {
        mConnectionListener = listener;
    }

    public void setMessageListener(String action, MessageListener listener) {
        mMessageListeners.put(action, listener);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        try {
            JSONObject authData = new JSONObject();
            authData.put("action", "auth");
            authData.put("username", mUsername);
            authData.put("password", mPassword);

            this.send(authData.toString());
        }
        catch (JSONException e) {
            onError(new Exception("Failed to parse JSON"));
        }
    }

    @Override
    public void onMessage(String message) {
        try {
            JSONObject data = new JSONObject(new JSONTokener(message));
            String action  = data.getString("action");
            data.remove("action");

            switch (action) {
                case "auth ok":
                    mConnected = true;

                    User user = new User(data.getString("username"));

                    if (mConnectionListener != null) {
                        mConnectionListener.onConnect(user);
                    }

                    break;
                case "auth fail":
                    this.close();
                    onError(new AuthenticationError(data.getString("message")));

                    break;
                default:
                    if (mConnected && mMessageListeners.containsKey(action)) {
                        mMessageListeners.get(action).onMessage(data);
                    }
            }
        }
        catch (JSONException e) {
            onError(new Exception("Failed to parse JSON"));
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        mConnected = false;

        if (mConnectionListener != null) {
            mConnectionListener.onDisconnect();
        }
    }

    @Override
    public void onError(Exception e) {
        mConnected = false;

        if (mConnectionListener != null) {
            mConnectionListener.onError(e);
        }
    }

    public void send(String action, JSONObject data) throws Exception {
        JSONObject copy;

        try {
            copy = new JSONObject(new JSONTokener(data.toString()));
            copy.put("action", action);
        }
        catch (JSONException e) {
            Log.e(TAG, "Error while constructing JSON: " + e.getMessage());
            throw new Exception("Invalid action!");
        }

        this.send(copy.toString());
    }
}
