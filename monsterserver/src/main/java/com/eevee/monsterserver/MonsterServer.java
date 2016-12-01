package com.eevee.monsterserver;

import com.pusher.java_websocket.WebSocket;
import com.pusher.java_websocket.handshake.ClientHandshake;
import com.pusher.java_websocket.server.WebSocketServer;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

public class MonsterServer extends WebSocketServer {
    private Map<String, User> mUsers;
    private Map<WebSocket, User> mConnectedUsers;
    private KeyPair mServerKey;

    public MonsterServer(InetSocketAddress address) throws NoSuchAlgorithmException{
        super(address);

        mServerKey = createKeyPair();

        mUsers = new HashMap<>();
        mConnectedUsers = new HashMap<>();

        mUsers.put("edu", new User("edu", "asd"));
        mUsers.put("eve", new User("eve", "asd"));
    }

    public KeyPair createKeyPair() throws NoSuchAlgorithmException{
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        return keyGen.generateKeyPair();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String ip = conn.getRemoteSocketAddress().getAddress().getHostAddress();
        System.out.println(ip + " connected.");

        JSONObject res = new JSONObject();
        res.put("action", "serverInfo");
        res.put("publicKey", Base64.encodeBase64String(mServerKey.getPublic().getEncoded()));
        System.out.println(res.toString());
        conn.send(res.toString());
    }



    @Override
    public void onMessage(WebSocket conn, String message) {
        String msgDecrypt = null;
        try {
            msgDecrypt = descryptText(message);
        } catch (CryptoError cryptoError) {
            JSONObject res = new JSONObject();
            res.put("action", "cryptoerror");
            conn.send(res.toString());
            System.out.println("Crypto Error: "+ cryptoError.getMessage());
            return;
        }

        JSONObject data = new JSONObject(new JSONTokener(msgDecrypt));

        switch (data.getString("action")) {
            case "auth":
                onAuthenticate(conn, data);
                break;
            case "message":
                onChatMessage(conn, data);
            default:
                JSONObject res = new JSONObject();
                res.put("action", "unknown");
                conn.send(res.toString());
                break;
        }
    }

    private String descryptText(String message) throws CryptoError {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, mServerKey.getPrivate());
            System.out.println(Base64.decodeBase64(message));

            byte [] cryptoBytes = cipher.doFinal(Base64.decodeBase64(message));
            System.out.println(cryptoBytes);

            return new String(cryptoBytes,"UTF-8");
        } catch (Exception e) {
            throw new CryptoError(e.getMessage());
        }
    }


    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        User user = mConnectedUsers.get(conn);

        if (user != null) {
            System.out.println(user.getUsername() + " disconnected.");
            user.setConnected(false);
            user.setSocket(null);
            mConnectedUsers.remove(conn);
        }
        else {
            String ip = conn.getRemoteSocketAddress().getAddress().getHostAddress();
            System.out.println("anonymous[" + ip + "] disconnected.");
        }
    }

    @Override
    public void onError(WebSocket conn, Exception e) {
        if (conn != null) {
            String ip = conn.getRemoteSocketAddress().getAddress().getHostAddress();
            System.err.println(ip + " entered an error state.");
        }

        System.err.println("Socket error: " + e.getMessage());
        e.printStackTrace();
    }

    private void onAuthenticate(WebSocket conn, JSONObject data) {
        String ip = conn.getRemoteSocketAddress().getAddress().getHostAddress();

        if (mConnectedUsers.containsKey(conn)) {
            System.out.println("[WARNING] " + ip + " is already authenticated!");
            mConnectedUsers.remove(conn);
        }

        if (!mUsers.containsKey(data.getString("username"))) {
            System.out.println(ip + " attempt to login as " + data.getString("username") + " but failed.");

            JSONObject res = new JSONObject();
            res.put("action", "auth fail");
            res.put("message", "invalid username");
            conn.send(res.toString());
            return;
        }

        User user = mUsers.get(data.getString("username"));
        user.setConnected(true);
        user.setSocket(conn);
        mConnectedUsers.put(conn, user);

        JSONObject res = new JSONObject();
        res.put("action", "auth ok");
        res.put("username", user.getUsername());
        conn.send(res.toString());

        System.out.println(ip + " authenticated as " + user.getUsername() + ".");
    }

    private void onChatMessage(WebSocket conn, JSONObject data) {
        String to = data.getString("to");
        User toUser = mUsers.get(to);

        if (toUser == null) {
            System.out.println("User not found: " + to);
            JSONObject res = new JSONObject();
            res.put("action", "delivery fail");
            res.put("to", to);
            res.put("message", "Destinatário não existe!");
            conn.send(res.toString());
            return;
        }

        if (!toUser.isConnected()) {
            System.out.println("User not connected: " + toUser.getUsername());
            return;
        }

        System.out.println(data.getString("from") + " is talking to " + to + ":");
        System.out.println(" > " + data.getString("body"));

        toUser.getSocket().send(data.toString());
    }

    public static void main(String[] args) throws InterruptedException, IOException, NoSuchAlgorithmException  {
        MonsterServer s = new MonsterServer(new InetSocketAddress(8080));
        s.start();

        System.out.println("Server listening on port " + s.getPort() + "...");
    }

    public class CryptoError extends Exception {
        public CryptoError(String msg) {
            super(msg);
        }
    }
}

