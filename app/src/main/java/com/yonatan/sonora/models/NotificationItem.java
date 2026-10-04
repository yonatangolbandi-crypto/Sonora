package com.yonatan.sonora.models;

import java.io.Serializable;
import java.util.Objects;

/**
 * מחלקה המייצגת הודעת התראה במערכת (לייק, מעקב חדש, תגובה, המלצת AI שבועית).
 * In-app notification entity.
 */
public class NotificationItem implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TYPE_LIKE = "like";
    public static final String TYPE_FOLLOW = "follow";
    public static final String TYPE_COMMENT = "comment";
    public static final String TYPE_AI_REC = "ai_rec";

    private String id;
    private String recipientUserId;
    private String actorUsername;
    private String actorAvatar;
    private String type;
    private String title;
    private String message;
    private String targetId;
    private long timestamp;
    private boolean isRead;

    public NotificationItem() {
        this.id = "";
        this.recipientUserId = "";
        this.actorUsername = "";
        this.actorAvatar = "";
        this.type = TYPE_LIKE;
        this.title = "";
        this.message = "";
        this.targetId = "";
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }

    public NotificationItem(String id, String recipientUserId, String actorUsername,
                            String actorAvatar, String type, String title,
                            String message, String targetId, long timestamp) {
        this.id = id;
        this.recipientUserId = recipientUserId;
        this.actorUsername = actorUsername;
        this.actorAvatar = actorAvatar;
        this.type = type;
        this.title = title;
        this.message = message;
        this.targetId = targetId;
        this.timestamp = timestamp;
        this.isRead = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(String recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public String getActorAvatar() {
        return actorAvatar;
    }

    public void setActorAvatar(String actorAvatar) {
        this.actorAvatar = actorAvatar;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationItem)) return false;
        NotificationItem that = (NotificationItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
