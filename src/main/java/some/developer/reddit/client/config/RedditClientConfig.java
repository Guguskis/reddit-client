package some.developer.reddit.client.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import some.developer.reddit.client.client.RedditClient;
import some.developer.reddit.client.helper.CommentsParser;
import some.developer.reddit.client.helper.RedditAssembler;
import some.developer.reddit.client.helper.SubmissionParser;

@Configuration
public class RedditClientConfig {

    @Bean
    @Qualifier("reddit")
    public RestTemplate getRestTemplate(RedditProperties properties) {
        return new RestTemplateBuilder()
                .rootUri(properties.getHost())
                .build();
    }

    @Bean
    @ConfigurationProperties(prefix = "reddit")
    public RedditProperties getRedditProperties() {
        return new RedditProperties();
    }

    @Bean
    public RedditClient getRedditClient(@Qualifier("reddit") RestTemplate restTemplate, RedditProperties properties) {
        return new RedditClient(
                new CommentsParser(),
                restTemplate,
                new RedditAssembler(new SubmissionParser()),
                properties
        );
    }

}
