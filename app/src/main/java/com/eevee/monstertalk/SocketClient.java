package com.eevee.monstertalk;

import android.util.Base64;
import android.util.Log;

import com.pusher.java_websocket.client.WebSocketClient;
import com.pusher.java_websocket.handshake.ServerHandshake;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URI;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static android.R.attr.key;

public class SocketClient extends WebSocketClient {
    private static final String TAG = "SOCKET";

    private String mUsername;
    private String mPassword;
    private ConnectionListener mConnectionListener;
    private Map<String,MessageListener> mMessageListeners;
    private boolean mConnected;
    private PublicKey mServerPublicKey;

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

    public SocketClient(URI host, final String username, final String password)
            throws NoSuchAlgorithmException, NoSuchPaddingException {
        super(host);
        mUsername = username;
        mPassword = password;
        mConnectionListener = null;
        mMessageListeners = new HashMap<>();
        mConnected = false;

        Security.addProvider(new BouncyCastleProvider());
    }

    public void setConnectionListener(ConnectionListener listener) {
        mConnectionListener = listener;
    }

    public void setMessageListener(String action, MessageListener listener) {
        mMessageListeners.put(action, listener);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
    }

    @Override
    public void onMessage(String message) {
        try {
            JSONObject data = new JSONObject(new JSONTokener(message));
            String action  = data.getString("action");
            data.remove("action");

            switch (action) {
                case "serverInfo":
                    try {
                        mServerPublicKey = getPublicKeyFromString(data.getString("publicKey"));

                        JSONObject authData = new JSONObject();
                        authData.put("username", mUsername);
                        authData.put("password", mPassword);
                        this.send("auth", authData, mServerPublicKey);
                    }
                    catch (JSONException e) {
                        onError(new Exception("Failed to parse JSON: " + e.getMessage()));
                    }
                    catch (CryptoError e) {
                        onError(new Exception("Failed to parse server public key: " + e.getMessage()));
                    }
                    break;
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

    public void send(String action, JSONObject data) throws JSONException {
        JSONObject dataCopy = new JSONObject(new JSONTokener(data.toString()));
        dataCopy.put("action", action);

        this.send(dataCopy.toString());
    }

    public void send(String action, JSONObject data, Key key) throws JSONException, CryptoError {
        JSONObject dataCopy = new JSONObject(new JSONTokener(data.toString()));
        dataCopy.put("action", action);

        this.send(this.cryptText(dataCopy.toString(), key));
    }

    private PublicKey getPublicKeyFromString(String key) throws CryptoError {
        try {
            X509EncodedKeySpec pk = new X509EncodedKeySpec(Base64.decode(key, Base64.DEFAULT));
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(pk);
        }
        catch (NoSuchAlgorithmException e) {
            throw new CryptoError(e.getMessage());
        }
        catch (InvalidKeySpecException e) {
            throw new CryptoError(e.getMessage());
        }
    }

    private String cryptText(String data, Key key) throws CryptoError {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte [] cryptoBytes = cipher.doFinal(data.getBytes(Charset.forName("UTF-8")));

            return Base64.encodeToString(cryptoBytes, Base64.DEFAULT);
        } catch (Exception e) {
            throw new CryptoError(e.getMessage());
        }
    }


    public class CryptoError extends Exception {
        public CryptoError(String msg) {
            super(msg);
        }
    }
}
