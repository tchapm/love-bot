package com.bot.twitter;

import java.util.LinkedList;

import twitter4j.IDs;
import twitter4j.PagableResponseList;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

import com.bot.main.Response;


public class TwitterClient {
	private Twitter twitInst; 
	
	public TwitterClient(){
		this.twitInst = TwitterClient.getTwitterInstance();
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
	private static Twitter getTwitterInstance(){
		ConfigurationBuilder hpBuilder = new ConfigurationBuilder();
		TwitterFactory tf = new TwitterFactory(hpBuilder.build());
		return tf.getInstance();
		
	}

	public void publishResponse(String response) {
		try {
			twitInst.updateStatus(response);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}
}
