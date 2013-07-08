package com.bot.twitter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.bot.main.Bot.BotTraits;
import com.bot.main.Response;


public class TwitterClient {
	private Twitter twitInst; 
	private static String CONSUMER_KEY;
	private static String CONSUMER_SECRET;
	private static String ACCESS_TOKEN;
	private static String ACCESS_SECRET;
	static final Logger logger = Logger.getLogger(TwitterClient.class);

	public TwitterClient(BotTraits bt){
		PropertyConfigurator.configure("log4j.properties");
		readProperties(bt);
		this.twitInst = getTwitterInstance();
	}

	private void readProperties(BotTraits bt) {
		Properties props = new Properties();
		FileInputStream fis;
		try {
			fis = new FileInputStream("properties.xml");
			props.loadFromXML(fis);
		} catch (Exception e) {
			logger.error(e);
		}
		CONSUMER_KEY = props.getProperty("oauth.consumer.key");
		CONSUMER_SECRET = props.getProperty("oauth.consumer.secret");
		try{
			ACCESS_TOKEN = props.getProperty(bt.token);
			ACCESS_SECRET = props.getProperty(bt.secret);
		}catch(Exception e){
			logger.error("Invalid Bot name! " + e);
			System.exit(0);
		}
	}

	private Twitter getTwitterInstance(){
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey(CONSUMER_KEY)
		.setOAuthConsumerSecret(CONSUMER_SECRET)
		.setOAuthAccessToken(ACCESS_TOKEN)
		.setOAuthAccessTokenSecret(ACCESS_SECRET);
		TwitterFactory tf = new TwitterFactory(cb.build());
		return tf.getInstance();

	}
	public ArrayList<Response> parseMentions(){
		ArrayList<Response> comments = new ArrayList<Response>();
		try {
			ResponseList<Status> mentions = twitInst.getMentionsTimeline();
			for(Status mention : mentions){
				Response tweet = new Response(mention.getText(), mention.getUser().getScreenName(),
						mention.getCreatedAt());
				if(tweet.getDate()!=null && tweet.getSearchWord()!=null && tweet.getCommentor()!=null){
					comments.add(tweet);
				}
			}
		} catch (TwitterException e) {
			logger.error(e);
		}
		return comments;
	}

	public ArrayList<Response> getFollowers(BotTraits bt){
		ArrayList<Response> followers = new ArrayList<Response>();
		try {
			IDs ids = twitInst.getFollowersIDs(bt.name, -1);
			for(long id : ids.getIDs()){
				Response theFollower = new Response("@" + twitInst.showUser(id).getScreenName());
				followers.add(theFollower);
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return followers;
	}

	public void publishFriendTweet(ArrayList<Response> comments){
		try {
			for(Response comment : comments){
				twitInst.updateStatus(comment.getResponse());
			}
		} catch (TwitterException e) {
			logger.error(e);
		}

	}

	public Status publishResponse(String response) {
		try {
			return twitInst.updateStatus(response);
		} catch (TwitterException e) {
			logger.error(e);
		}
		return null;
	}
	public Twitter getTwitInst() {
		return twitInst;
	}

	public void setTwitInst(Twitter twitInst) {
		this.twitInst = twitInst;
	}
}
