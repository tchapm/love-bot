package com.bot.main;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.bot.main.LocalBot.BotNames;
import com.bot.twitter.TwitterClient;


public class LocalBot {
	private static final int TIME_BETWEEN_CHECKS_LOVE = 4;
	private static final int TIME_BETWEEN_CHECKS_POE = 5;
	private static final String POE_NAME = "EdgarAllanBot";
	private static final String LOVE_NAME = "HPbotcraft";
	private TwitterClient twitClient;

	public HashMap<String, HashMap<String, Integer>> wordMap = new HashMap<String, HashMap<String, Integer>>();
	public HashMap<String, HashMap<String, Float>> wordProbablityMap = new HashMap<String, HashMap<String, Float>>();
	private int numWords=0;
	
	private String botType = null;
	private BotNames botName;

	public enum BotNames {
		Lovecraft("HPbotcraft", "lovecraft.token", "lovecraft.secret"),
		Poe("EdgarAllenBot", "poe.token", "poe.secret");

		private BotNames(String name, String token, String secret){
			this.name = name;
			this.token = token;
			this.secret = secret;
		}
		public final String name;
		public final String token;
		public final String secret;
	}
	
	public LocalBot(String botName) {
		this.botName = (botName.equals(BotNames.Lovecraft.name)) ? BotNames.Lovecraft : botName.equals(BotNames.Poe.name) ? BotNames.Poe : null;
		this.botType = botName;
	}
	


	public void createAndPublish() throws IOException {
		ArrayList<Response> comments = parseMentions();

		//String firstWord = new String("can");
		//String nextWord = null;
		//int sentenceNum = 10;
		//int currSentence = 0;

		//pull in the corpus to analize
		inputCorpus();
		System.out.println("\nWords filed: " + getNumWords());
		//System.out.println("The number of I is = " + lovecraft.wordMap.get("I"));
		setProbablity();
		makeTweets(comments);
		publishResponseTweetNew(comments);
		
	}


	public void getFollowersAndPublish() throws IOException {
		ArrayList<Response> comments = getFollowers();

		//pull in the corpus to analize
		inputCorpus();
		System.out.println("\nWords filed: " + getNumWords());
		setProbablity();
		makeTweets(comments);
		twitClient.publishFriendTweet(comments);
		
	}
	
	public ArrayList<Response> getFollowers() {
		return twitClient.getFollowers();
	}
	
//	public LinkedList<Response> getFollowers(BufferedReader input) throws IOException, InterruptedException {
//		String str;
//		LinkedList<Response> followers = new LinkedList<Response>();
//		while ((str = input.readLine()) != null) {
//			if ((botType.equals(LOVE_NAME) && !str.equals(POE_NAME))
//					|| (botType.equals(POE_NAME) && !str.equals(LOVE_NAME))){
//				Response theFollower = new Response("@" + str);
//				followers.add(theFollower);
//			}
//		}
//		return followers;
//	}
	
	public ArrayList<Response> parseMentions() {
		return twitClient.parseMentions();
	}
	
//	public LinkedList<Response> parseInTweets(BufferedReader in) throws IOException {
//		LinkedList<Response> comments = new LinkedList<Response>();
//		while(in.ready()){
//			Response tweet = new Response();
//			tweet.setSearchString(in.readLine());
//			tweet.setCommentor(in.readLine());
//			tweet.setDateStr(in.readLine());
//			if(tweet.getDateStr()!=null && tweet.getSearchWord()!=null && tweet.getCommentor()!=null){
//				comments.add(tweet);
//			}
//		}
//		return comments;
//	}

	public void inputCorpus() throws IOException {
			File dir = new File("resources/" + botName.name + "Text");
			if (dir.isDirectory()){
				for (File f : dir.listFiles()){
					if(!f.isHidden()){
						BufferedReader inCorpus= new BufferedReader(new FileReader(f));
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
		String tweet = "";

		for(int i=0; i<comments.size(); i++){
			String commentator = comments.get(i).getCommentor();
			String searchWord = comments.get(i).getSearchWord();
			while(!tweet.endsWith(".") && !tweet.endsWith("?") && !tweet.endsWith("!")){
				tweet = getTweet(commentator, searchWord);
			}
			comments.get(i).setResponse(tweet);
			System.out.println("\n\n" + comments.get(i).getResponse());
			tweet = "";
		}
	}
	
	public String getTweet(String commWrd, String srchWrd){
		String tweet = commWrd;
		String nextWord = srchWrd;
		tweet = tweet.concat(" " +  nextWord);
		try{
			if(this.getNext(nextWord)!=null){
				nextWord = this.getNext(nextWord);
			} else if(this.getNext(nextWord.toLowerCase())!=null){
				nextWord = this.getNext(nextWord.toLowerCase());
			} else if(this.getNext(nextWord.substring(0, (nextWord.length()-1)))!=null){
				nextWord = this.getNext(nextWord.substring(0, (nextWord.length()-1)));
			} else {
				nextWord = this.getNext(nextWord);
			}
			while((tweet.length() + nextWord.length()) < 140){
				tweet = tweet.concat(" " + nextWord);
				if((nextWord.contains(".") || nextWord.contains("?") || nextWord.contains("!")) && tweet.length()>70){
					break;
				}
				nextWord = this.getNext(nextWord);
			}
			//tweet = tweet.concat("...");
		}catch (Exception e){
			tweet = commWrd;
			nextWord = "When";
			while((tweet.length() + nextWord.length()) < 140){
				tweet = tweet.concat(" " + nextWord);
				if((nextWord.contains(".") || nextWord.contains("?") || nextWord.contains("!")) && tweet.length()>70){
					break;
				}
				nextWord = this.getNext(nextWord);
			}
		}
		return tweet;
	}
	

//	public void publishResponseTweet(LinkedList<Response> comments) {
//		try{
//			DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");//ISODateTimeFormat.dateTime();
//			DateTime londonTime = new DateTime();
//			//timestamp of tweets
//			londonTime = londonTime.withZone(DateTimeZone.forID("America/Los_Angeles"));
//			DateTime tweetTime;
//			if(TimeZone.getDefault().inDaylightTime(new Date())){
//				londonTime = londonTime.plusHours(7);
//			}else{
//				londonTime = londonTime.plusHours(8);
//			}
//
//			FileWriter fstream = null;
//			String outTweets =  this.path + "tweets/outTweets" + this.botType + ".txt";
//			fstream = new FileWriter(outTweets); 
//
//			BufferedWriter out = new BufferedWriter(fstream);
//			for(int i=0; i<comments.size(); i++){	
//				tweetTime = fmt.parseDateTime(comments.get(i).getDateStr());
//				if (this.botType.equals(LOVE_NAME)) {
//					tweetTime = tweetTime.plusMinutes(TIME_BETWEEN_CHECKS_LOVE); //set this based on the time between checks
//				}else if (this.botType.equals(POE_NAME)) {
//					tweetTime = tweetTime.plusMinutes(TIME_BETWEEN_CHECKS_POE); 
//				}
//				if(tweetTime.isAfter(londonTime)){
//					System.out.println("Tweet is within the last search");
//					out.write(comments.get(i).getResponse() + "\n");
//				}
//				System.out.println("Current: " + londonTime.toString());
//				System.out.println("Tweet: " + tweetTime.toString());
//			}
//			out.close();
//		}catch (Exception e){//Catch exception if any
//			System.err.println("Error: " + e.getMessage());
//		}
//		
//	}

	public void publishResponseTweetNew(ArrayList<Response> comments) {
		try{
			DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");//ISODateTimeFormat.dateTime();
			DateTime londonTime = new DateTime();
			//timestamp of tweets
			londonTime = londonTime.withZone(DateTimeZone.forID("America/Los_Angeles"));
			DateTime tweetTime;
			if(TimeZone.getDefault().inDaylightTime(new Date())){
				londonTime = londonTime.plusHours(7);
			}else{
				londonTime = londonTime.plusHours(8);
			}

			for(Response comment : comments){
				Date twtDate = comment.getDate();
				
				tweetTime = fmt.parseDateTime(comment.getDateStr());
				if (this.botType.equals(LOVE_NAME)) {
					tweetTime = tweetTime.plusMinutes(TIME_BETWEEN_CHECKS_LOVE); //set this based on the time between checks
				}else if (this.botType.equals(POE_NAME)) {
					tweetTime = tweetTime.plusMinutes(TIME_BETWEEN_CHECKS_POE); 
				}
				if(tweetTime.isAfter(londonTime)){
					System.out.println("Tweet is within the last search");
					twitClient.publishResponse(comment.getResponse());
				}
				System.out.println("Current: " + londonTime.toString());
				System.out.println("Tweet: " + tweetTime.toString());
			}
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
	}

	
//	public void publishFriendTweet(LinkedList<Response> comments) throws IOException {
//		FileWriter fstream = null;
//		String outTweets =  this.path + "tweets/outFollow" + this.botType + ".txt";
//		fstream = new FileWriter(outTweets); 	
//		BufferedWriter out = new BufferedWriter(fstream);
//		for(int i=0; i<comments.size(); i++){	
//			out.write(comments.get(i).getResponse() + "\n");
//		}
//		out.close();
//	}

	private String getNext(String firstWord) {
		String keyLower = null;
		String keyUpper = null;
		float value;
		Random generator = new Random();
		float randomIndex = generator.nextFloat()*100;
		if (!this.wordProbablityMap.containsKey(firstWord)){
			return null;
		}
		
		ValueComparator bvc =  new ValueComparator(this.wordProbablityMap.get(firstWord));
        TreeMap<String,Float> sortedMap = new TreeMap<String,Float>(bvc);
        sortedMap.putAll(this.wordProbablityMap.get(firstWord));
		Iterator<String> it = sortedMap.keySet().iterator();
		keyLower = (String)it.next();
		
		while (it.hasNext()) {
			keyUpper = (String)it.next();
//			HashMap<String, Float> temp  = this.wordProbablityMap.get(firstWord);
			value = sortedMap.get(keyLower);
//			value = this.wordProbablityMap.get(firstWord).get(keyLower);
			if (randomIndex<=value) {
				return keyLower;
			} else if(randomIndex>value && randomIndex<=this.wordProbablityMap.get(firstWord).get(keyUpper)){
				return keyUpper;
			}
			keyLower = keyUpper;
		}
		return keyLower;
	}
	
	class ValueComparator implements Comparator<String> {

	    HashMap<String, Float> base;
	    public ValueComparator(HashMap<String, Float> hashMap) {
	        this.base = hashMap;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(String a, String b) {
	    	
	        if (base.get(a) > base.get(b)) {
	            return 1;
	        } else if (base.get(a)==base.get(b)){
	        	return 0;
	        }else{
	            return -1;
	        } // returning 0 would merge keys
	    }
	}

	public void setProbablity(){
		Iterator<String> it = this.wordMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String)it.next();
			this.setPercentage(key);
		}
	}
	
	public void setPercentage(String firstWord){
		Iterator<String> it = this.wordMap.get(firstWord).keySet().iterator();
		int secondWordCount;
		int secondWordSum = this.getSum(firstWord);
		float percent = 0;
		HashMap<String,Float> tempMap = new HashMap<String,Float>();
		while (it.hasNext()) {
			String key = (String)it.next();
			secondWordCount=this.wordMap.get(firstWord).get(key);
			percent += ((float)secondWordCount/(float)secondWordSum)*100;
			tempMap.put(key, percent);
		}
		this.wordProbablityMap.put(firstWord, tempMap);
	}
	
	public Integer getSum(String firstWord){
		Integer sum = 0;
		Iterator<String> it = this.wordMap.get(firstWord).keySet().iterator();
		while (it.hasNext()) {
			String secondWord = (String)it.next();
			sum+=this.wordMap.get(firstWord).get(secondWord);
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

	public String getBotType() {
		return botType;
	}

	public void setBotType(String botType) {
		this.botType = botType;
	}







}