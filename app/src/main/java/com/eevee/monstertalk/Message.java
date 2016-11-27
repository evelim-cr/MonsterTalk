package com.eevee.monstertalk;

import java.io.Serializable;

public class Message implements Serializable {
    private User mFrom;
    private User mTo;
    private String mBody;

    public Message(User from, User to, String body) {
        mFrom = from;
        mTo = to;
        mBody = body;
    }

    public User getFrom() {
        return mFrom;
    }

    public User getTo() {
        return mTo;
    }

    public String getBody() {
        return mBody;
    }
}
