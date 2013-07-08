package com.bot.main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import twitter4j.Status;

import com.bot.twitter.TwitterClient;


public class Bot {
	private static final String GENERIC_START_WORD = "What";
	private TwitterClient twitClient;
	public HashMap<String, HashMap<String, Integer>> wordMap = new HashMap<String, HashMap<String, Integer>>();
	public HashMap<String, HashMap<String, Float>> wordProbablityMap = new HashMap<String, HashMap<String, Float>>();
	private int numWords=0;
	private BotTraits botType;
	static final Logger logger = Logger.getLogger(Bot.class);

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
		comments = getMentionsInTime(comments);

		//pull in the corpus to analize
		if(comments.size()>0){
			inputCorpus();
			System.out.println("\nWords filled: " + getNumWords());
			setProbability();
			makeTweets(comments);
			publishResponseTweet(comments);
		}else{
			logger.info("No valid comments in time period");
			System.exit(0);
		}

	}


	public void getFollowersAndPublish() throws IOException {
		ArrayList<Response> comments = getFollowers();

		//pull in the corpus to analize
		inputCorpus();
		logger.info("\nWords filled: " + getNumWords());
		setProbability();
		makeTweets(comments);
		twitClient.publishFriendTweet(comments);

	}

	public ArrayList<Response> getFollowers() {
		return twitClient.getFollowers(botType);
	}

	public ArrayList<Response> parseMentions() {
		return twitClient.parseMentions();
	}

	public void inputCorpus() throws IOException {
		String dirName = "src/main/resources/" + botType.name + "_text";
		File dir = new File(dirName);
		if (dir.isDirectory()){
			for (File f : dir.listFiles()){
				if(!f.isHidden()){
					BufferedReader inCorpus = new BufferedReader(new FileReader(f));
					this.fillMap(inCorpus);
					inCorpus.close();
				}
			}
		}
	}

	public void fillMap(BufferedReader in) throws IOException{
		String str = null;
		String secondWord = null;
		String firstWord = null;
		HashMap<String,Integer> tempMap;
		int tempInt;
		while ((str = in.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(str);
			this.numWords+=st.countTokens();
			if(st.hasMoreTokens()){
				secondWord = st.nextToken();
			}
			while (st.hasMoreTokens()) {
				firstWord = secondWord;
				secondWord = st.nextToken();
				if (this.wordMap.containsKey(firstWord)){
					tempMap=this.wordMap.get(firstWord);
					if (tempMap.containsKey(secondWord)){
						tempInt = (Integer)tempMap.get(secondWord)+1;
						tempMap.put(secondWord, tempInt);
						this.wordMap.put(firstWord, tempMap);
					} else {
						tempMap.put(secondWord, 1);
						this.wordMap.put(firstWord, tempMap);
					}
				} else {
					tempMap=new HashMap<String, Integer>();
					tempMap.put(secondWord, 1);
					this.wordMap.put(firstWord, tempMap);
				}

			}
		}
	}

	public void makeTweets(ArrayList<Response> comments) {
		for(int i=0; i<comments.size(); i++){
			String tweet = "";
			String commentator = comments.get(i).getCommentor();
			String searchWord = comments.get(i).getSearchWord();
			while(!tweet.endsWith(".") && !tweet.endsWith("?") && !tweet.endsWith("!")){
				tweet = getTweet(commentator, searchWord);
			}
			comments.get(i).setResponse(tweet);
			logger.info(i+1 + ".   " + comments.get(i).getResponse());
		}
	}

	public String getTweet(String commentor, String srchWrd){
		StringBuilder sb = new StringBuilder("@" + commentor);
		String nextWord = srchWrd;
		if(wordProbablityMap.containsKey(nextWord)){
			sb.append(" " + nextWord);
			nextWord = this.getNext(nextWord);
		} else if(wordProbablityMap.containsKey(nextWord.toLowerCase())){
			sb.append(" " + nextWord);
			nextWord = this.getNext(nextWord.toLowerCase());
		} else if(wordProbablityMap.containsKey(stripPunctuation(nextWord))){
			sb.append(" " + nextWord);
			nextWord = this.getNext(nextWord.substring(0, (nextWord.length()-1)));
		} else {
			nextWord = GENERIC_START_WORD;
		}
		try{
			while((sb.length() + nextWord.length()) < 140){
				sb.append(" " + nextWord);
				if(hasPunctuation(nextWord) && sb.length()>70){
					break;
				}
				nextWord = this.getNext(nextWord);
			}
		} catch(Exception e){
			logger.error("NULL getTweet():   " + sb.toString());
		}
		return sb.toString();
	}

	private Object stripPunctuation(String word) {
		return hasPunctuation(word) ? word.substring(0, (word.length()-1)) : word;
	}

	private boolean hasPunctuation(String nextWord) {
		return (nextWord.endsWith(".") || nextWord.endsWith("?") || nextWord.endsWith("!"));
	}

	private ArrayList<Response> getMentionsInTime(ArrayList<Response> comments) {
		ArrayList<Response> validComments = new ArrayList<Response>();
		Date cutoffTime = getCutoffTime();
		for(Response comment : comments){
			Date twtDate = comment.getDate();
			if(twtDate.after(cutoffTime)){
				validComments.add(comment);
			}
		}
		return validComments;
	}

	public long publishResponseTweet(ArrayList<Response> comments) {
		long tweetId = 0;
		try{
			Date cutoffTime = getCutoffTime();
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

	private Date getCutoffTime() {
		Calendar cd = new GregorianCalendar();
		cd.setTime(new Date());
		cd.add(Calendar.MINUTE,	-botType.checkTime);
		return new Date(cd.getTimeInMillis());
	}

	public String getNext(String firstWord) {
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

	public void setProbability(){
		for(String key : wordMap.keySet()){
			wordProbablityMap.put(key, setPercentage(key));
		}
	}

	public HashMap<String,Float> setPercentage(String firstWord){
		int secondWordCount;
		int secondWordSum = getSum(firstWord);
		float percent = 0;
		HashMap<String,Float> wordPercentMap = new HashMap<String,Float>();
		HashMap<String, Integer> firstWordMap = wordMap.get(firstWord);
		for(String keyWord : firstWordMap.keySet() ){
			secondWordCount = firstWordMap.get(keyWord);
			percent += ((float)secondWordCount/(float)secondWordSum)*100;
			wordPercentMap.put(keyWord, percent);
		}
		return wordPercentMap;
	}

	public Integer getSum(String firstWord){
		Integer sum = 0;
		HashMap<String, Integer> firstWordMap = wordMap.get(firstWord);
		for(String secondWord : firstWordMap.keySet()){
			sum += firstWordMap.get(secondWord);
		}
		return sum;
	}
	public HashMap<String, HashMap<String, Integer>> getWordMap() {
		return wordMap;
	}

	public void setWordMap(HashMap<String, HashMap<String, Integer>> wordMap) {
		this.wordMap = wordMap;
	}

	public HashMap<String, HashMap<String, Float>> getWordProbablityMap() {
		return wordProbablityMap;
	}

	public void setWordProbablityMap(
			HashMap<String, HashMap<String, Float>> wordProbablityMap) {
		this.wordProbablityMap = wordProbablityMap;
	}

	public int getNumWords() {
		return numWords;
	}

	public void setNumWords(int numWords) {
		this.numWords = numWords;
	}

	public TwitterClient getTwitClient() {
		return twitClient;
	}

	public void setTwitClient(TwitterClient twitClient) {
		this.twitClient = twitClient;
	}

}