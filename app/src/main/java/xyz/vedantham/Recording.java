package xyz.vedantham;

public class Recording {
    private String title;
    private long timestamp;
    private int duration;
    private String channel;
    private String uuid;
    public Recording(String title, long timestamp, int duration, String channel, String uuid) {
        this.title = title;
        this.timestamp = timestamp;
        this.duration = duration;
        this.channel = channel;
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
