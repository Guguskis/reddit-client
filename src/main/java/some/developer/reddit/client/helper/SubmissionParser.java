package some.developer.reddit.client.helper;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubmissionParser {

    public LocalDateTime parseCreationDate(String creationDateText) {
        return LocalDateTime.parse(creationDateText, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public Integer parseScore(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Integer parseCommentCount(String commentCountText) {
        if (commentCountText.equals("comment")) {
            return 0;
        }

        Pattern pattern = Pattern.compile("(\\d*) comment");
        Matcher matcher = pattern.matcher(commentCountText);

        if (!matcher.find()) {
            throw new IllegalArgumentException(String.format("String does not contain commentCount number '%s'", commentCountText));
        }

        String commentCountString = matcher.group(1);
        return Integer.parseInt(commentCountString);
    }
}
