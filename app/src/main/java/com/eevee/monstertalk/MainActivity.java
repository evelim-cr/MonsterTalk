package com.eevee.monstertalk;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.eevee.monstertalk.monstertalk.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MAIN";

    private Toolbar mMainToolbarView;
    private FloatingActionButton mAddChatButton;

    private ChatAdapter mChatListAdapter;
    private ListView mChatListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SocketManager sockManager = SocketManager.getInstance();

        if (!sockManager.isConnected()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        mMainToolbarView = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(mMainToolbarView);

        mChatListView = (ListView) findViewById(R.id.chatList);
        mChatListAdapter = new ChatAdapter(this, R.layout.row_chat, sockManager.getChats());
        mChatListView.setAdapter(mChatListAdapter);

        sockManager.addOnChatUpdateListener(new SocketManager.OnChatUpdateListener() {
            @Override
            public void onChatUpdate() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChatListAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        mChatListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                Chat chat = (Chat) view.getTag();

                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("CHAT_ID", chat.getId());
                startActivity(intent);
            }
        });

        mAddChatButton = (FloatingActionButton) findViewById(R.id.addChatButton);
        mAddChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewChatDialog dialog = new NewChatDialog();

                dialog.setOnSuccessListener(new NewChatDialog.OnSuccessListener() {
                    @Override
                    public void onSuccess(DialogFragment dialog, Bundle bundle) {
                        String to = bundle.getString("to");
                        boolean crypto = bundle.getBoolean("crypto");

                        sockManager.newChat(to, crypto);
                    }
                });

                dialog.show(getFragmentManager(), "NEW_CHAT");
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


    private class ChatAdapter extends ArrayAdapter<Chat> {
        Context mContext;
        int mLayoutResourceId;
        ArrayList<Chat> mData = null;

        public ChatAdapter(Context context, int layoutResourceId, ArrayList<Chat> data) {
            super(context, layoutResourceId, data);
            this.mLayoutResourceId = layoutResourceId;
            this.mContext = context;
            this.mData = data;
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {
            Chat chat = mData.get(position);

            if (row == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                row = inflater.inflate(mLayoutResourceId, parent, false);
            }

            row.setTag(chat);

            TextView recipientView = (TextView) row.findViewById(R.id.recipientText);
            TextView statusView = (TextView) row.findViewById(R.id.statusText);
            ImageView cryptoView = (ImageView) row.findViewById(R.id.cryptoIcon);

            User recipient = chat.getRecipient();
            recipientView.setText(recipient.getUsername());

            Message lastMessage = chat.getLastMessage();
            if (lastMessage == null) {
                statusView.setText("");
            }
            else {
                statusView.setText(lastMessage.getBody());
            }

//        if (chat.isCrypto()) {
            cryptoView.setVisibility(View.VISIBLE);
//        }
//        else {
//            cryptoView.setVisibility(View.INVISIBLE);
//        }

            return row;
        }
    }
}
