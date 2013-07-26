package com.bot.main;

import java.io.IOException;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
/**
 * Main class to publish a tweet at each follower of bot named in args[0]
 * @author tchap
 */
public class MainBotFollowers {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		if(args.length!=1){
			MainBotResponder.logger.error("incorrect input");
			System.exit(0);
		}
		Bot bot = new Bot(args[0]);
		bot.getFollowersAndPublish();
	}
}
