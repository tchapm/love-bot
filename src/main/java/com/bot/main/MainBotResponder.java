package com.bot.main;

import java.io.IOException;
/**
 * Main class to respond to tweets that are directed at the bot named in args[0], and how long it has been
 * since the bot has responded to tweets (args[1])
 * @author tchap
 */
public class MainBotResponder {
	
	public static void main(String[] args) throws IOException {
		if(args.length!=2){
			System.out.println("Incorrect input");
			System.exit(0);
		}
		Bot bot = new Bot(args[0], Integer.parseInt(args[1]));
		bot.createAndPublish();
	}
}
