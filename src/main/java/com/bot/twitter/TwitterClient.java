package com.bot.twitter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedList;
import java.util.Properties;

import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.bot.main.Response;


public class TwitterClient {
	private Twitter twitInst; 
	private static String CONSUMER_KEY;
	private static String CONSUMER_SECRET;
	private static String ACCESS_TOKEN;
	private static String ACCESS_SECRET;
	
	public TwitterClient(){
		readProperties();
		this.twitInst = getTwitterInstance();
	}
	
	private void readProperties() {
		Properties props = new Properties();
        FileInputStream fis;
		try {
			fis = new FileInputStream("properties.xml");
			props.loadFromXML(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidPropertiesFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		CONSUMER_KEY = props.getProperty("oauth.consumer.key");
		CONSUMER_SECRET = props.getProperty("oauth.consumer.secret");
		ACCESS_TOKEN = props.getProperty("lovecraft.token");
		ACCESS_SECRET = props.getProperty("lovecraft.secret");

	}
	
	private Twitter getTwitterInstance(){
		ConfigurationBuilder hpBuilder = new ConfigurationBuilder();
		hpBuilder.setDebugEnabled(true)
		.setOAuthConsumerKey(CONSUMER_KEY)
		.setOAuthConsumerSecret(CONSUMER_SECRET)
		.setOAuthAccessToken(ACCESS_TOKEN)
		.setOAuthAccessTokenSecret(ACCESS_SECRET);
		TwitterFactory tf = new TwitterFactory(hpBuilder.build());
		return tf.getInstance();
		
	}
	public LinkedList<Response> parseMentions(){
		LinkedList<Response> comments = new LinkedList<Response>();
		try {
			ResponseList<Status> mentions = twitInst.getMentionsTimeline();
			for(Status mention : mentions){
				Response tweet = new Response();
				tweet.setSearchString(mention.getText());
				tweet.setCommentor(mention.getUser().getScreenName());
				tweet.setDate(mention.getCreatedAt());
				if(tweet.getDateStr()!=null && tweet.getSearchWord()!=null && tweet.getCommentor()!=null){
					comments.add(tweet);
				}
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return comments;
	}
	
	public LinkedList<Response> getFollowers(){
		LinkedList<Response> followers = new LinkedList<Response>();
		try {
			IDs ids = twitInst.getFollowersIDs("HPbotcraft", -1);
			for(long id : ids.getIDs()){
				Response theFollower = new Response("@" + twitInst.showUser(id).getScreenName());
				followers.add(theFollower);
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (TwitterException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return followers;
	}
	
	public void publishFriendTweet(LinkedList<Response> comments){
		try {
			for(Response comment : comments){
				twitInst.updateStatus(comment.getResponse());
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		
	}

	public void publishResponse(String response) {
		try {
			twitInst.updateStatus(response);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}
}
