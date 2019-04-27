package imta.soundclouddownloader.core;

public class TrackInfo {
    private String username;
    private String title;
    private String url;
    private long id;

    public TrackInfo() {
    }

    public String getUsername() {
        return username;
    }

    public TrackInfo setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public TrackInfo setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public TrackInfo setUrl(String url) {
        this.url = url;
        return this;
    }

    public long getId() {
        return id;
    }

    public TrackInfo setId(long id) {
        this.id = id;
        return this;
    }

    @Override
    public String toString() {
        return "TrackInfo{" +
                "username='" + username + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", id=" + id +
                '}';
    }
}
