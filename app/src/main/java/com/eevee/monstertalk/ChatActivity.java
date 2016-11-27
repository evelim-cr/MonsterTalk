package com.eevee.monstertalk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.eevee.monstertalk.monstertalk.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "CHAT";

    private Toolbar mMainToolbarView;

    private Chat mChat;
    private ArrayList<Message> mMessageList;
    private ArrayAdapter mMessageListAdapter;

    private ListView mMessageListView;
    private TextView mNewMessageView;
    private ImageButton mSendMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        final SocketManager sockManager = SocketManager.getInstance();
        if (!sockManager.isConnected()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        mMainToolbarView = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(mMainToolbarView);

        int chatId = getIntent().getIntExtra("CHAT_ID", -1);
        mChat = sockManager.findChatById(chatId);

        mMessageListView = (ListView) findViewById(R.id.messageList);
        mMessageListAdapter = new MessageAdapter(this, R.layout.row_message, mChat.getMessages());
        mMessageListView.setAdapter(mMessageListAdapter);

        mNewMessageView = (TextView) findViewById(R.id.newMessage);
        mSendMessageView = (ImageButton) findViewById(R.id.sendMessage);
        mSendMessageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String body = mNewMessageView.getText().toString();
                mNewMessageView.setText("");

                Message newMessage = new Message(
                        sockManager.getOwnUser(), mChat.getRecipient(), body);

                sockManager.sendMessage(newMessage);
                mChat.pushMessage(newMessage);
                mMessageListAdapter.notifyDataSetChanged();
            }
        });

        sockManager.addOnChatUpdateListener(new SocketManager.OnChatUpdateListener() {
            @Override
            public void onChatUpdate() {
                ChatActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMessageListAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public class MessageAdapter extends ArrayAdapter<Message> {
        Context context;
        int layoutResourceId;
        ArrayList<Message> data = null;

        public MessageAdapter(Context context, int layoutResourceId, ArrayList<Message> data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {
            Message message = data.get(position);

            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
            }

            row.setTag(message);

            TextView recipientView = (TextView) row.findViewById(R.id.messageRecipient);
            TextView contentView = (TextView) row.findViewById(R.id.messageContent);

            recipientView.setText(message.getFrom().getUsername());
            contentView.setText(message.getBody());

            return row;
        }
    }
}
