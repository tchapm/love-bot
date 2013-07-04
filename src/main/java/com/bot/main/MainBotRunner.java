package com.bot.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import com.bot.twitter.TwitterClient;

public class MainBotRunner {


	public static void main(String[] args) throws IOException, InterruptedException {
		TwitterClient twc = new TwitterClient();
		twc.publishFriendTweet(null);
		String path = args[0];
		//		HPbotcraft
		//		EdgarAllenBot
		String botName = args[1];
		Boolean isFollowers = Boolean.parseBoolean(args[2]);
		LocalBot bot = new LocalBot(botName, path);
		LinkedList<Response> comments = new LinkedList<Response>();
		if(isFollowers){
			String inputFile = path + "tweets/followers" + botName + ".txt";
			BufferedReader input = new BufferedReader(new FileReader(inputFile));
			comments = bot.getFollowers(input);
		}else{
			String inputFile = path + "tweets/inTweets" + botName + ".txt";
			BufferedReader input = new BufferedReader(new FileReader(inputFile));
			comments = bot.parseInTweets(input);
		}

		//String firstWord = new String("can");
		//String nextWord = null;
		//int sentenceNum = 10;
		//int currSentence = 0;

		//pull in the corpus to analize
		bot.inputCorpus();
		System.out.println("\nWords filed: " + bot.getNumWords());
		//System.out.println("The number of I is = " + lovecraft.wordMap.get("I"));
		bot.setProbablity();
		bot.makeTweets(comments);
		if(isFollowers){
			bot.publishFriendTweet(comments);
		}else{
			bot.publishResponseTweet(comments);
		}


	}
}
