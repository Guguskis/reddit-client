package some.developer.reddit.client.client;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import some.developer.reddit.client.config.RedditProperties;
import some.developer.reddit.client.helper.CommentsParser;
import some.developer.reddit.client.helper.SubmissionParser;
import some.developer.reddit.client.model.Comment;
import some.developer.reddit.client.model.PageCategory;
import some.developer.reddit.client.model.Submission;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class RedditClient {

    private static final Logger LOG = LoggerFactory.getLogger(RedditClient.class);

    private static final String REDDIT_SUBREDDIT_TOP_PAST_HOUR_URL = "/r/%s/%s";
    private static final String GET_SUBMISSION_PAGE_URL = "/r/%s/comments/%s/";
    private final SubmissionParser submissionParser;
    private final CommentsParser commentsParser;
    private final RestTemplate restTemplate;

    private final RedditProperties properties;

    public RedditClient(SubmissionParser submissionParser,
                        CommentsParser commentsParser,
                        RestTemplate restTemplate, RedditProperties properties) {
        this.submissionParser = submissionParser;
        this.commentsParser = commentsParser;
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public List<Submission> getSubmissions(String subreddit, PageCategory category) {
        String pageHtml = getSubmissionsHtmlPage(subreddit, category);
        Document document = Jsoup.parse(pageHtml);
        Elements submissionElements = document.getElementsByClass("link");

        return submissionElements.stream()
                .map(this::parseSubmission)
                .peek(submission -> submission.setSubreddit("r/" + subreddit))
                .collect(Collectors.toList());
    }

    public List<Comment> getCommentsForSubmission(String subreddit, String submissionId) {
        String submissionHtmlPage = getSubmissionPage(subreddit, submissionId);
        return commentsParser.parseComments(submissionHtmlPage);
    }

    public String getSubmissionsHtmlPage(String subreddit, PageCategory category) {
        String url = String.format(REDDIT_SUBREDDIT_TOP_PAST_HOUR_URL, subreddit, category.toString().toLowerCase());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntityWithHeaders(), String.class);
        return response.getBody();
    }

    private String getSubmissionPage(String subreddit, String submissionId) {
        String url = String.format(GET_SUBMISSION_PAGE_URL, subreddit, submissionId);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntityWithHeaders(), String.class);
        return response.getBody();
    }

    private Submission parseSubmission(Element submissionElement) {
        String title = submissionElement.select("a.title").text();
        String scoreText = submissionElement.getElementsByClass("score unvoted").get(0).attr("title");
        String commentCountText = submissionElement.getElementsByClass("comments").text();
        String creationDateText = submissionElement.getElementsByClass("live-timestamp").get(0).attr("datetime");
        String url = submissionElement.getElementsByClass("comments").get(0).attr("href");

        Submission submission = new Submission();
        submission.setTitle(title);
        submission.setScore(submissionParser.parseScore(scoreText));
        submission.setCommentCount(submissionParser.parseCommentCount(commentCountText));
        submission.setCreated(submissionParser.parseCreationDate(creationDateText));

        try {
            submission.setUrl(new URL(url));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not parse URL", e);
        }

        return submission;
    }

    private HttpEntity<Object> getHttpEntityWithHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.USER_AGENT, properties.getUserAgent());
        return new HttpEntity<>(httpHeaders);
    }
}
