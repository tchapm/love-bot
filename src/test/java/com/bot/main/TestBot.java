package com.bot.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import twitter4j.TwitterException;

public class TestBot {

	@Test
	public void testPublishResponseDate() throws InterruptedException{
		Response testRes = new Response("Test");
		testRes.setDate(new Date());
		testRes.setResponse("Eldrich horrors!");
		Bot lb = new Bot("HPbotcraft");
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
		Bot lb = new Bot("HPbotcraft");
		lb.inputCorpus();
		HashMap<String, Integer> testMap = lb.wordMap.get("death");
		Assert.assertEquals(1,(int)testMap.get("struggle"));
		lb.setProbability();
		String tweet = lb.getTweet("Steve", "struggle!");
		Assert.assertEquals(true, tweet.length()>0);
		Assert.assertEquals(true, tweet.startsWith("@Steve struggle"));
		tweet = lb.getTweet("Steve", "sfsdfsdfds");
		Assert.assertEquals(true, tweet.startsWith("@Steve What"));
	}
}
