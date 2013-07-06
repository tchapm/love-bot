package com.bot.main;

import java.io.IOException;

public class MainBotResponder {

	public static void main(String[] args) throws IOException {
		if(args.length!=1){
			System.out.println("Incorrect input");
			System.exit(0);
		}
		LocalBot bot = new LocalBot(args[0]);
		bot.createAndPublish();
	}
}
