package com.anubhav.codereverse.uiapp.fcm;

import java.io.Serializable;
import java.util.Objects;

public class NotificationData implements Serializable {
    private String senderID;
    private String senderUsername;
    private String senderImage;
    private String receiverID;
    private String title;
    private String content;
    private String extra;
    private String type;
    private String timeStamp;

    public NotificationData(String senderID, String senderUsername, String senderImage, String receiverID, String title, String content, String type) {
        this.senderID = senderID;
        this.senderUsername = senderUsername;
        this.senderImage = senderImage;
        this.receiverID = receiverID;
        this.title = title;
        this.content = content;
        this.extra = null;
        this.type = type;
        this.timeStamp = "KEY" + (int) System.currentTimeMillis();
    }

    public NotificationData(String senderID, String senderUsername, String senderImage, String receiverID, String title, String content, String extra, String type) {
        this.senderID = senderID;
        this.senderUsername = senderUsername;
        this.senderImage = senderImage;
        this.receiverID = receiverID;
        this.title = title;
        this.content = content;
        this.extra = extra;
        this.type = type;
        this.timeStamp = "KEY" + (int) System.currentTimeMillis();
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderImage() {
        return senderImage;
    }

    public void setSenderImage(String senderImage) {
        this.senderImage = senderImage;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getTimeStamp() {
        return Integer.parseInt(timeStamp.replace("KEY", ""));
    }

    public String getTimeStamps() {
        return timeStamp;
    }

    public void setTimeStamp(String mTimeStamp) {
        String timeStamp = mTimeStamp;
        if (!mTimeStamp.startsWith("KEY")) {
            timeStamp = "KEY" + mTimeStamp;
        }
        this.timeStamp = timeStamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationData that = (NotificationData) o;
        return Objects.equals(senderID, that.senderID) &&
                Objects.equals(senderUsername, that.senderUsername) &&
                Objects.equals(senderImage, that.senderImage) &&
                Objects.equals(receiverID, that.receiverID) &&
                Objects.equals(title, that.title) &&
                Objects.equals(content, that.content) &&
                Objects.equals(extra, that.extra) &&
                Objects.equals(type, that.type) &&
                Objects.equals(timeStamp, that.timeStamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderID, senderUsername, senderImage, receiverID, title, content, extra, type, timeStamp);
    }

}