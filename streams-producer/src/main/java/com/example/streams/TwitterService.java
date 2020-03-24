package com.example.streams;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

@Service
public class TwitterService {

    @Autowired
    private ProducerService producer;

    private TwitterStream twitterStream;
    private String[] keys = new String[]{"#java", "#kafka", "#grpc", "#docker", "#k8s", "#Covid19"};

    @PostConstruct
    private void init() throws IOException {
        InputStream twitterPropertiesFile = new ClassPathResource("twitter.properties").getInputStream();
        Properties twitterProperties = new Properties();
        twitterProperties.load(twitterPropertiesFile);

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(twitterProperties.getProperty("OAuthConsumerKey"))
                .setOAuthConsumerSecret(twitterProperties.getProperty("OAuthConsumerSecret"))
                .setOAuthAccessToken(twitterProperties.getProperty("OAuthAccessToken"))
                .setOAuthAccessTokenSecret(twitterProperties.getProperty("OAuthAccessTokenSecret"));

        TwitterStreamFactory tf = new TwitterStreamFactory(cb.build());
        twitterStream = tf.getInstance();

        twitterStream.addListener(
                new StatusListener() {
                    @Override
                    public void onException(Exception ex) {
                    }

                    public void onStatus(Status status) {
                        for (String key : keys) {
                            if (status.getText().contains(key)) {
//                                System.out.println(key + ": " + status.getText());
                                producer.send(key, status.getText());
                            }
                        }
                    }

                    @Override
                    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                    }

                    @Override
                    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                    }

                    @Override
                    public void onScrubGeo(long userId, long upToStatusId) {
                    }

                    @Override
                    public void onStallWarning(StallWarning warning) {
                    }
                }
        );

        FilterQuery tweetFilterQuery = new FilterQuery();
        tweetFilterQuery.track(keys);

        twitterStream.filter(tweetFilterQuery);
    }

    @PreDestroy
    private void close() {
        twitterStream.shutdown();
    }

}
