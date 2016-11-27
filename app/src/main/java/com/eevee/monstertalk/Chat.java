package com.eevee.monstertalk;

import java.io.Serializable;
import java.util.ArrayList;

public class Chat implements Serializable {
    private static int CURRENT_ID = 0;
    private int mId;
    private User mRecipient;
    private ArrayList<Message> mMessages;

    public Chat(User recipient, boolean crypto) {
        mId = CURRENT_ID++;
        mRecipient = recipient;
        mMessages = new ArrayList<>();
    }

    public int getId() {
        return mId;
    }

    public User getRecipient() {
        return mRecipient;
    }

    public void pushMessage(Message message) {
        mMessages.add(message);
    }

    public ArrayList<Message> getMessages() {
        return mMessages;
    }

    public Message getLastMessage() {
        if (mMessages.size() == 0) {
            return null;
        }

        return mMessages.get(mMessages.size() - 1);
    }
}
