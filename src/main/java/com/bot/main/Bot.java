package com.bot.main;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import twitter4j.Status;

import com.bot.twitter.TwitterClient;
import com.bot.utils.StringUtils;


public class Bot {
	private TwitterClient twitClient;
	private BotTraits botType;
	private static Date searchTime = new Date();
	static final Logger logger = Logger.getLogger(Bot.class);

	private static final String GENERIC_START_WORD = "What";

	public enum BotTraits {
		BOT1("HPbotcraft", "lovecraft.token", "lovecraft.secret", 4),
		BOT2("EdgarAllanBot", "poe.token", "poe.secret", 5);

		private BotTraits(String name, String token, String secret, int checkTime){
			this.name = name;
			this.token = token;
			this.secret = secret;
			this.checkTime = checkTime;
		}
		public final String name;
		public final String token;
		public final String secret;
		public final int checkTime;
	}

	public Bot(String botName) {
		PropertyConfigurator.configure("log4j.properties");
		this.botType = (botName.equals(BotTraits.BOT1.name)) ? BotTraits.BOT1 : botName.equals(BotTraits.BOT2.name) ? BotTraits.BOT2 : null;
		this.twitClient = new TwitterClient(botType);
	}

	public void createAndPublish() throws IOException {
		ArrayList<Response> comments = parseMentions();
		comments = getMentionsInTime(comments, botType.checkTime);
		//pull in the corpus to analize
		if(comments.size()>0){
			Corpus botCorp = new Corpus(botType.name);
			makeTweets(botCorp.getwordProbablityMap(), comments);
			publishResponseTweet(comments, botType.checkTime);
		}else{
			logger.info("No valid comments in time period");
			System.exit(0);
		}

	}


	public void getFollowersAndPublish() throws IOException {
		ArrayList<Response> comments = getFollowers();
		//pull in the corpus to analize
		Corpus botCorp = new Corpus(botType.name);
		makeTweets(botCorp.getwordProbablityMap(), comments);
		twitClient.publishFriendTweet(comments);

	}

	public ArrayList<Response> getFollowers() {
		return twitClient.getFollowers(botType);
	}

	public ArrayList<Response> parseMentions() {
		return twitClient.parseMentions();
	}

	public void makeTweets(HashMap<String, HashMap<String, Float>> wordProbablityMap, ArrayList<Response> comments) {
		for(int i=0; i<comments.size(); i++){
			String tweet = "";
			String commentator = comments.get(i).getCommentor();
			String searchWord = comments.get(i).getSearchWord();
			while(!StringUtils.hasPunctuation(tweet)){
				tweet = getTweet(wordProbablityMap, commentator, searchWord);
			}
			comments.get(i).setResponse(tweet);
			logger.info(i+1 + ".   " + comments.get(i).getResponse());
		}
	}

	public String getTweet(HashMap<String, HashMap<String, Float>> wordProbablityMap, String commentor, String srchWrd){
		StringBuilder sb = new StringBuilder("@" + commentor);
		String nextWord = srchWrd;
		if(wordProbablityMap.containsKey(nextWord)){
			sb.append(" " + nextWord);
			nextWord = getNext(wordProbablityMap, nextWord);
		} else if(wordProbablityMap.containsKey(nextWord.toLowerCase())){
			sb.append(" " + nextWord);
			nextWord = getNext(wordProbablityMap, nextWord.toLowerCase());
		} else if(wordProbablityMap.containsKey(StringUtils.stripPunctuation(nextWord))){
			sb.append(" " + nextWord);
			nextWord = getNext(wordProbablityMap, nextWord.substring(0, (nextWord.length()-1)));
		} else {
			nextWord = GENERIC_START_WORD;
		}
		try{
			while((sb.length() + nextWord.length()) < 140){
				sb.append(" " + nextWord);
				if(StringUtils.hasPunctuation(nextWord) && sb.length()>70){
					break;
				}
				nextWord = getNext(wordProbablityMap, nextWord);
			}
		} catch(Exception e){
			logger.error("NULL getTweet():   " + sb.toString());
		}
		return sb.toString();
	}


	private ArrayList<Response> getMentionsInTime(ArrayList<Response> comments, int checkTime) {
		ArrayList<Response> validComments = new ArrayList<Response>();
		Date cutoffTime = getCutoffTime(checkTime);
		for(Response comment : comments){
			Date twtDate = comment.getDate();
			if(twtDate.after(cutoffTime)){
				validComments.add(comment);
			}
		}
		return validComments;
	}

	public long publishResponseTweet(ArrayList<Response> comments, int checkTime) {
		long tweetId = 0;
		try{
			Date cutoffTime = getCutoffTime(checkTime);
			for(Response comment : comments){
				Date twtDate = comment.getDate();
				if(twtDate.after(cutoffTime)){
					logger.info("Tweet is within the last search");
					Status st = twitClient.publishResponse(comment.getResponse());
					tweetId = st.getId();
				}
				logger.info("Current time: " + cutoffTime.toString() + " --- Tweet time: " + twtDate.toString());
			}
		}catch (Exception e){
			logger.error(e);
		}
		return tweetId;

	}

	private Date getCutoffTime(int checkTime) {
		Calendar cd = new GregorianCalendar();
		cd.setTime(searchTime);
		cd.add(Calendar.MINUTE,	-checkTime);
		return new Date(cd.getTimeInMillis());
	}

	public String getNext(HashMap<String, HashMap<String, Float>> wordProbablityMap, String firstWord) {
		String closestWord = null;
		Random generator = new Random();
		float randomIndex = generator.nextFloat()*100;
		if (!wordProbablityMap.containsKey(firstWord)){
			return null;
		}
		float nearDist = Float.MAX_VALUE;
		HashMap<String, Float> wordProb = wordProbablityMap.get(firstWord);
		for(String keyWord : wordProb.keySet()){
			float wordDist = wordProb.get(keyWord);
			if(wordDist>randomIndex && (wordDist-randomIndex<nearDist)){
				nearDist = wordDist-randomIndex;
				closestWord = keyWord;
			}
		}
		return closestWord;
	}



	public TwitterClient getTwitClient() {
		return twitClient;
	}

	public void setTwitClient(TwitterClient twitClient) {
		this.twitClient = twitClient;
	}

}