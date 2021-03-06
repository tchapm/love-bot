package com.bot.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import junit.framework.Assert;
import org.junit.Test;
import twitter4j.TwitterException;

public class TestBot {
	private static final String botName = System.getProperty("botName");
	
	@Test
	public void testPublishResponseDate() throws InterruptedException{
		Response testRes = new Response("Test");
		testRes.setDate(new Date());
		testRes.setResponse("Where?");
		Bot lb = new Bot(botName);
		ArrayList<Response> lbList = new ArrayList<Response>();
		lbList.add(testRes);
		long tweetId = lb.publishResponseTweet(lbList);
		Assert.assertEquals(true, tweetId>0);
		try {
			if(tweetId>0){
				lb.getTwitClient().getTwitInst().destroyStatus(tweetId);
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testInputCorpus() throws IOException{
		Bot lb = new Bot(botName);
		Corpus cp = new Corpus(botName);
		HashMap<String, Integer> testMap = cp.wordMap.get("when");
		Assert.assertEquals(true,(int)testMap.get("you")>0);
		String tweet = lb.getTweet(cp.wordProbablityMap, "Steve", "which!");
		Assert.assertEquals(true, tweet.length()>0);
		Assert.assertEquals(true, tweet.startsWith("@Steve which"));
		tweet = lb.getTweet(cp.wordProbablityMap, "Steve", "sfsdfsdfds");
		Assert.assertEquals(false, tweet.startsWith("@Steve sfsdfsdfds"));
	}
	
}
