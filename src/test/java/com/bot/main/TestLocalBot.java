package com.bot.main;

import java.util.ArrayList;
import java.util.Date;

import junit.framework.Assert;
import org.junit.Test;
import twitter4j.TwitterException;

public class TestLocalBot {

	@Test
	public void testPublishResponseDate() throws InterruptedException{
		Response testRes = new Response("Test");
		testRes.setDate(new Date());
		testRes.setResponse("Eldrich horrors!");
		LocalBot lb = new LocalBot("HPbotcraft");
		ArrayList<Response> lbList = new ArrayList<Response>();
		lbList.add(testRes);
		long tweetId = lb.publishResponseTweetNew(lbList);
		Assert.assertEquals(true, tweetId>0);
		try {
			if(tweetId>0){
				lb.getTwitClient().getTwitInst().destroyStatus(tweetId);
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		
	}
}
