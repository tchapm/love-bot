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
		
	    StatusListener listener = new StatusListener(){
	    	@Override
	    	public void onStatus(Status status) {
	            System.out.println(status.getUser().getName() + " : " + status.getText());
	        }
	        @Override
	        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
	        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
	        public void onException(Exception ex) {
	            ex.printStackTrace();
	        }
			@Override
			public void onScrubGeo(long arg0, long arg1) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub
				
			}
	    };
	    TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
	    twitterStream.addListener(listener);
	    // sample() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
	    twitterStream.sample();
	}
}
