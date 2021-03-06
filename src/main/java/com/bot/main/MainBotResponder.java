package com.bot.main;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Main class to respond to tweets that are directed at the bot named in args[0], and how long the bot waits
 * to respond to tweets (args[1])
 * @author tchap
 */

public class MainBotResponder {
	public static final Logger logger = Logger.getLogger(MainBotResponder.class);
	static {
		PropertyConfigurator.configure("log4j.properties");
	}
	public static void main(String[] args) throws IOException, InterruptedException {
		if(args.length!=2){
			logger.error("Incorrect input");
			System.exit(0);
		}
		Bot bot = new Bot(args[0]);
//		bot.createAndPublish(Integer.parseInt(args[1]));
		bot.createAndPublishCont(Integer.parseInt(args[1]));
	}
}
