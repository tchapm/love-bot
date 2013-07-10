package com.bot.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import junit.framework.Assert;
import org.junit.Test;
import twitter4j.*;

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
		Corpus cp = new Corpus("HPbotcraft");
		HashMap<String, Integer> testMap = cp.wordMap.get("death");
		Assert.assertEquals(2,(int)testMap.get("which"));
		String tweet = lb.getTweet(cp.wordProbablityMap, "Steve", "which!");
		Assert.assertEquals(true, tweet.length()>0);
		Assert.assertEquals(true, tweet.startsWith("@Steve which"));
		tweet = lb.getTweet(cp.wordProbablityMap, "Steve", "sfsdfsdfds");
		Assert.assertEquals(false, tweet.startsWith("@Steve sfsdfsdfds"));
	}
	
}
