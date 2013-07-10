package com.bot.twitter;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import com.bot.main.MainBotResponder;
import com.bot.main.Response;

/**
 * Class to connect to the twitter account of the twitter bot. Can get followers, tweets @bot, and 
 * publish responses. 
 * @author tchap
 */

public class TwitterClient {
	private Twitter twitInst; 
	private static String CONSUMER_KEY;
	private static String CONSUMER_SECRET;
	private static String ACCESS_TOKEN;
	private static String ACCESS_SECRET;

	public TwitterClient(String botName){
		readProperties(botName);
		this.twitInst = getTwitterInstance();
	}
	/**
	 * Method to read the properties.xml file and get the oath keys needed to connect to the twitter account
	 * @param bt the enum of the bot name and properties
	 */
	private void readProperties(String botName) {
		Properties props = new Properties();
		FileInputStream fis;
		try {
			fis = new FileInputStream("properties.xml");
			props.loadFromXML(fis);
		} catch (Exception e) {
			MainBotResponder.logger.error(e);
		}
		CONSUMER_KEY = props.getProperty("oauth.consumer.key");
		CONSUMER_SECRET = props.getProperty("oauth.consumer.secret");
		try{
			ACCESS_TOKEN = props.getProperty(botName + ".token");
			ACCESS_SECRET = props.getProperty(botName + ".secret");
		}catch(Exception e){
			MainBotResponder.logger.error("Invalid Bot name! " + e);
			System.exit(0);
		}
	}
	/**
	 * Method to connect to the twitter account. If authorization is correct it will be connected to the bot
	 * and be able to perform functions of the client 
	 * @return instance of the TwitterFactory associated with the specified account
	 */
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
	/**
	 * Method to iterate through the mentions of the bot, and get the relevant information so that a response
	 * can be formed.
	 * @return an array of Responses populated with necessary attributes from the comments
	 */
	public ArrayList<Response> parseMentions(){
		ArrayList<Response> comments = new ArrayList<Response>();
		try {
			ResponseList<Status> mentions = twitInst.getMentionsTimeline();
			for(Status mention : mentions){
				Response tweet = new Response(mention.getText(), mention.getUser().getScreenName(),
						mention.getCreatedAt());
				if(tweet.getDate()!=null && tweet.getSearchWord()!=null && tweet.getCommenter()!=null){
					comments.add(tweet);
				}
			}
		} catch (TwitterException e) {
			MainBotResponder.logger.error(e);
		}
		return comments;
	}
	
	public ArrayList<Response> parseMentions(Date lastDate) {
		ArrayList<Response> comments = new ArrayList<Response>();
		try {
			ResponseList<Status> mentions = twitInst.getMentionsTimeline();
			for(Status mention : mentions){
				if(mention.getCreatedAt().after(lastDate)){
					Response tweet = new Response(mention.getText(), mention.getUser().getScreenName(),
							mention.getCreatedAt());
					if(tweet.getDate()!=null && tweet.getSearchWord()!=null && tweet.getCommenter()!=null){
						comments.add(tweet);
					}
				}
			}
		} catch (TwitterException e) {
			MainBotResponder.logger.error(e);
		}
		return comments;
	}
	
	public Date getLastTweet(){
		Date theDate = new Date();
		try {
			ResponseList<Status> mentions = twitInst.getMentionsTimeline();
			theDate =  mentions.get(0).getCreatedAt();
		} catch (TwitterException e) {
			MainBotResponder.logger.error(e);
		}
		return theDate;
	}

	public ArrayList<Response> getFollowers(String botName){
		ArrayList<Response> followers = new ArrayList<Response>();
		try {
			IDs ids = twitInst.getFollowersIDs(botName, -1);
			for(long id : ids.getIDs()){
				Response theFollower = new Response(twitInst.showUser(id).getScreenName());
				followers.add(theFollower);
			}
		} catch (Exception e) {
			MainBotResponder.logger.error(e);
		}
		return followers;
	}

	public void publishFriendTweet(ArrayList<Response> comments){
		try {
			for(Response comment : comments){
				twitInst.updateStatus(comment.getResponse());
			}
		} catch (TwitterException e) {
			MainBotResponder.logger.error(e);
		}
	}

	public Status publishResponse(String response) {
		try {
			return twitInst.updateStatus(response);
		} catch (TwitterException e) {
			MainBotResponder.logger.error(e);
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
