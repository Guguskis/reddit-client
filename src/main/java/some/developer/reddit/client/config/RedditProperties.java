package some.developer.reddit.client.config;

public class RedditProperties {

    private String host;
    private String userAgent;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
