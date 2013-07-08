package com.bot.main;

import java.io.IOException;
/**
 * Main class to publish a tweet at each follower of bot named in args[0]
 * @author tchap
 */
public class MainBotFollowers {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		if(args.length!=1){
			System.out.println("incorrect input");
			System.exit(0);
		}
		Bot bot = new Bot(args[0]);
		bot.getFollowersAndPublish();
	}
}
