package some.developer.reddit.client;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
import some.developer.reddit.client.helper.RedditAssembler;
import some.developer.reddit.client.model.Comment;
import some.developer.reddit.client.model.PageCategory;
import some.developer.reddit.client.model.Submission;

import java.util.List;
import java.util.stream.Collectors;

public class RedditClient {

    private static final Logger LOG = LoggerFactory.getLogger(RedditClient.class);

    private static final String REDDIT_SUBREDDIT_TOP_PAST_HOUR_URL = "/r/%s/%s";
    private static final String GET_SUBMISSION_PAGE_URL = "/r/%s/comments/%s/";

    private final CommentsParser commentsParser;
    private final RestTemplate restTemplate;
    private final RedditAssembler assembler;

    private final RedditProperties properties;

    public RedditClient(CommentsParser commentsParser,
                        RestTemplate restTemplate,
                        RedditAssembler assembler,
                        RedditProperties properties) {
        this.commentsParser = commentsParser;
        this.restTemplate = restTemplate;
        this.assembler = assembler;
        this.properties = properties;
    }

    public List<Submission> getSubmissions(String subreddit, PageCategory category) {
        String pageHtml = getSubmissionsHtmlPage(subreddit, category);
        Document document = Jsoup.parse(pageHtml);
        Elements submissionElements = document.getElementsByClass("link");

        return submissionElements.stream()
                .map(assembler::parseSubmission)
                .peek(submission -> submission.setSubreddit("r/" + subreddit))
                .collect(Collectors.toList());
    }

    public List<Comment> getComments(String subreddit, String submissionId) {
        String submissionHtmlPage = getSubmissionPage(subreddit, submissionId);
        return commentsParser.parseComments(submissionHtmlPage);
    }

    private String getSubmissionsHtmlPage(String subreddit, PageCategory category) {
        String url = String.format(REDDIT_SUBREDDIT_TOP_PAST_HOUR_URL, subreddit, category.toString().toLowerCase());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntityWithHeaders(), String.class);
        return response.getBody();
    }

    private String getSubmissionPage(String subreddit, String submissionId) {
        String url = String.format(GET_SUBMISSION_PAGE_URL, subreddit, submissionId);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntityWithHeaders(), String.class);
        return response.getBody();
    }

    private HttpEntity<Object> getHttpEntityWithHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.USER_AGENT, properties.getUserAgent());
        return new HttpEntity<>(httpHeaders);
    }
}
