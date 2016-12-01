package com.eevee.monstertalk;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.NoSuchPaddingException;

public class SocketManager  {
    private static final String TAG = "SOCKET";

    private static SocketManager ourInstance = new SocketManager();

    private boolean mConnected;
    private SocketClient mSocket;
    private User mOwnUser;
    private ArrayList<User> mUsers;
    private ArrayList<Chat> mChats;

    private OnConnectListener mOnConnectListener;
    private OnDisconnectListener mOnDisconnectListener;
    private OnConnectionErrorListener mOnConnectionErrorListener;
    private List<OnChatUpdateListener> mOnChatUpdateListeners;

    public SocketManager() {
        mConnected = false;
        mUsers = new ArrayList<>();
        mChats = new ArrayList<>();
        mOnChatUpdateListeners = new ArrayList<>();
    }

    public static SocketManager getInstance() {
        return ourInstance;
    }

    public void setOnConnectListener(OnConnectListener onConnectListener) {
        mOnConnectListener = onConnectListener;
    }

    public void setOnDisconnectListener(OnDisconnectListener onDisconnectListener) {
        mOnDisconnectListener = onDisconnectListener;
    }

    public void setOnConnectionErrorListener(OnConnectionErrorListener onConnectionErrorListener) {
        mOnConnectionErrorListener = onConnectionErrorListener;
    }

    public void addOnChatUpdateListener(OnChatUpdateListener onChatUpdateListener) {
        mOnChatUpdateListeners.add(onChatUpdateListener);
    }

    public void connect(URI host, String username, String password)
            throws AlreadyConnectedException, NoSuchAlgorithmException, NoSuchPaddingException {
        if (mConnected) {
            throw new AlreadyConnectedException("Socket already connected!");
        }

        mSocket = new SocketClient(host, username, password);

        mSocket.setConnectionListener(new SocketClient.ConnectionListener() {
            @Override
            public void onConnect(User user) {
                Log.d(TAG, "Connected.");

                mConnected = true;
                mOwnUser = user;

                if (mOnConnectListener != null) {
                    mOnConnectListener.onConnect();
                }
            }

            @Override
            public void onDisconnect() {
                Log.d(TAG, "Disconnected.");
                mConnected = false;

                if (mOnDisconnectListener != null) {
                    mOnDisconnectListener.onDisconnect();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
                mConnected = false;
            }
        });

        mSocket.setMessageListener("message", new SocketClient.MessageListener() {
            @Override
            public void onMessage(JSONObject data) {
                try {
                    String from = data.getString("from");
                    String body = data.getString("body");

                    User fromUser = findUserByName(from);

                    if (fromUser == null) {
                        Log.d(TAG, "new user: " + from);
                        fromUser = new User(from);
                        mUsers.add(fromUser);
                    }

                    Chat chat = findChatByUser(fromUser);

                    if (chat == null) {
                        Log.d(TAG, "new chat");
                        chat = new Chat(fromUser, true);
                        mChats.add(chat);
                    }

                    chat.pushMessage(new Message(fromUser, mOwnUser, body));

                    for (OnChatUpdateListener l : mOnChatUpdateListeners) {
                        l.onChatUpdate();
                    }

                    Log.d(TAG, "received " + body + " from " + fromUser.getUsername());
                }
                catch (JSONException e) {
                    Log.e(TAG, "Failed to parse JSON: " + e.getMessage());
                }
            }
        });

        mSocket.connect();
    }

    public boolean isConnected() {
        return mConnected;
    }

    public User getOwnUser() {
        return mOwnUser;
    }

    public ArrayList<User> getUsers() {
        return mUsers;
    }

    public ArrayList<Chat> getChats() {
        return mChats;
    }

    public void newChat(String to, boolean crypto) {
        User toUser = findUserByName(to);

        if (toUser == null) {
            toUser = new User(to);
            mUsers.add(toUser);
        }

        Chat chat = findChatByUser(toUser);

        if (chat == null) {
            chat = new Chat(toUser, true);
            mChats.add(chat);

            for (OnChatUpdateListener l : mOnChatUpdateListeners) {
                l.onChatUpdate();
            }
        }
    }

    public void sendMessage(Message message) {
        try {
            JSONObject data = new JSONObject();
            data.put("from", message.getFrom().getUsername());
            data.put("to", message.getTo().getUsername());
            data.put("body", message.getBody());
            mSocket.send("message", data);

            Log.d(TAG, "sending message");
        }
        catch (JSONException e) {
            Log.e(TAG, "Error while constructing JSON: " + e.getMessage());
        }
        catch (Exception e) {
            Log.e(TAG, "Error while sending mData: " + e.getMessage());
        }
    }

    public User findUserByName(String name) {
        for (User u : mUsers) {
            if (u.getUsername().equals(name)) {
                return u;
            }
        }

        return null;
    }

    public Chat findChatByUser(User user) {
        for (Chat c : mChats) {
            if (c.getRecipient() == user) {
                return c;
            }
        }

        return null;
    }

    public Chat findChatById(int id) {
        for (Chat c : mChats) {
            if (c.getId() == id) {
                return c;
            }
        }

        return null;
    }

    public class AlreadyConnectedException extends Exception {
        public AlreadyConnectedException(String message) {
            super(message);
        }
    }

    public interface OnConnectListener {
        void onConnect();
    }

    public interface OnDisconnectListener {
        void onDisconnect();
    }

    public interface OnConnectionErrorListener {
        void onConnectionError();
    }

    public interface OnChatUpdateListener {
        void onChatUpdate();
    }
}