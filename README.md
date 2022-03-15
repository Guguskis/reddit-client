Add properties to `application.yml` and RedditClient should get autowired for spring boot application automatically

[//]: # (language yml)

``` yaml
reddit:
    host: https://old.reddit.com
    # Reddit restricts API access for default agents
    user-agent: "Reddit-Bot (by /u/someDeveloper)"
```
