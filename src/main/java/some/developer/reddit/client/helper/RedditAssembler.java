package some.developer.reddit.client.helper;

import org.jsoup.nodes.Element;
import some.developer.reddit.client.model.Submission;

import java.net.MalformedURLException;
import java.net.URL;

public class RedditAssembler {

    private final SubmissionParser submissionParser;

    public RedditAssembler(SubmissionParser submissionParser) {
        this.submissionParser = submissionParser;
    }

    public Submission parseSubmission(Element submissionElement) {
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
}
