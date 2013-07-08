package com.bot.main;

import java.io.IOException;

public class MainBotResponder {

	public static void main(String[] args) throws IOException {
		if(args.length!=1){
			System.out.println("Incorrect input");
			System.exit(0);
		}
		Bot bot = new Bot(args[0]);
		bot.createAndPublish();
	}
}
