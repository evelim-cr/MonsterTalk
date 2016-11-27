package com.eevee.monsterserver;

import com.pusher.java_websocket.WebSocket;
import com.pusher.java_websocket.handshake.ClientHandshake;
import com.pusher.java_websocket.server.WebSocketServer;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class MonsterServer extends WebSocketServer {
    private Map<String, User> mUsers;
    private Map<WebSocket, User> mConnectedUsers;

    public MonsterServer(InetSocketAddress address) {
        super(address);

        mUsers = new HashMap<>();
        mConnectedUsers = new HashMap<>();

        mUsers.put("edu", new User("edu", "asd"));
        mUsers.put("eve", new User("eve", "asd"));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String ip = conn.getRemoteSocketAddress().getAddress().getHostAddress();
        System.out.println(ip + " connected.");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        JSONObject data = new JSONObject(new JSONTokener(message));

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
    }

    private void onChatMessage(WebSocket conn, JSONObject data) {
        String to = data.getString("to");
        User toUser = mUsers.get(to);

        if (toUser == null) {
            JSONObject res = new JSONObject();
            res.put("action", "delivery fail");
            res.put("to", to);
            res.put("message", "Destinatário não existe!");
            conn.send(res.toString());
            return;
        }

        if (!toUser.isConnected()) {
            return;
        }

        System.out.println(data.getString("from") + " is talking to " + to + ":");
        System.out.println(" > " + data.getString("body"));

        toUser.getSocket().send(data.toString());
    }

    public static void main(String[] args) throws InterruptedException , IOException {
        MonsterServer s = new MonsterServer(new InetSocketAddress(8887));
        s.start();

        System.out.println("Server listening on port " + s.getPort() + "...");
    }
}
