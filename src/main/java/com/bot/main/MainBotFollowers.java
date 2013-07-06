package com.bot.main;

import java.io.IOException;

public class MainBotFollowers {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		if(args.length!=1){
			System.out.println("incorrect input");
			System.exit(0);
		}
		LocalBot bot = new LocalBot(args[0]);
		bot.getFollowersAndPublish();
	}
}
