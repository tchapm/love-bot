package com.bot.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * This class handles the corpus of the specific author. It grabs all of the text from the resources
 * directory and creates maps of the words to implement the markov chaining. The wordMap maps the words
 * with the number of occurrences of the words that follow in the text. The wordProbabilityMap maps
 * the words with the likelihood of occurrence of the following word. 
 * @author tchap
 *
 */

public class Corpus {
	HashMap<String, HashMap<String, Integer>> wordMap = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Float>> wordProbablityMap = new HashMap<String, HashMap<String, Float>>();
	private static final String RESOURCE_DIR_NAME = "src/main/resources/";
	private static final String TEXT_PATH = "_text";
	private int numWords = 0;
	
	public Corpus(String botName) {
		try {
			this.wordMap = inputCorpus(botName);
			this.wordProbablityMap = setProbability(wordMap);
			MainBotResponder.logger.info("Words filled from " + botName + " corpus : " + getNumWords());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Method to ingest the text and return the wordMap
	 * @param botName 
	 * @return wordMap
	 * @throws IOException
	 */
	public HashMap<String, HashMap<String, Integer>> inputCorpus(String botName) throws IOException {
		String dirName = RESOURCE_DIR_NAME + botName + TEXT_PATH;
		File dir = new File(dirName);
		HashMap<String, HashMap<String, Integer>> theWordMap = new HashMap<String, HashMap<String,Integer>>();
		if (dir.isDirectory()){
			for (File f : dir.listFiles()){
				if(!f.isHidden()){
					BufferedReader inCorpus = new BufferedReader(new FileReader(f));
					theWordMap = fillMap(inCorpus, theWordMap);
					inCorpus.close();
				}
			}
		}
		return theWordMap;
	}
	/**
	 * Method to create the word map
	 * @param in
	 * @return word map of the words in the corpus with the number of occurrences of 
	 * the words that follow
	 * @throws IOException
	 */
	public HashMap<String, HashMap<String, Integer>> fillMap(BufferedReader in, HashMap<String, HashMap<String, Integer>> theWordMap) throws IOException{
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
				if (theWordMap.containsKey(firstWord)){
					tempMap=theWordMap.get(firstWord);
					if (tempMap.containsKey(secondWord)){
						tempInt = (Integer)tempMap.get(secondWord)+1;
						tempMap.put(secondWord, tempInt);
						theWordMap.put(firstWord, tempMap);
					} else {
						tempMap.put(secondWord, 1);
						theWordMap.put(firstWord, tempMap);
					}
				} else {
					tempMap=new HashMap<String, Integer>();
					tempMap.put(secondWord, 1);
					theWordMap.put(firstWord, tempMap);
				}

			}
		}
		return theWordMap;
	}
	
	public HashMap<String, HashMap<String, Float>> setProbability(HashMap<String, HashMap<String, Integer>> wrdMap){
		HashMap<String, HashMap<String, Float>> wordProbMap = new HashMap<String, HashMap<String,Float>>();
		for(String key : wrdMap.keySet()){
			wordProbMap.put(key, setPercentage(key, wrdMap));
		}
		return wordProbMap;
	}
	/**
	 * Method to set the likelihood of occurrence of the words that follow firstWord. Returns a map
	 * where the distance between the float and the float that is the next lowest is the percentage 
	 * of occurrence. This is somewhat awkward way of representing likelihood, but it allows for easy 
	 * lookup of the next word in a markov chain.  
	 * @param firstWord
	 * @param wrdMap
	 * @return percentage map of following words for firstWord
	 */
	public HashMap<String,Float> setPercentage(String firstWord, HashMap<String, HashMap<String, Integer>> wrdMap){
		int secondWordCount;
		int secondWordSum = getSum(firstWord, wrdMap);
		float percent = 0;
		HashMap<String,Float> wordPercentMap = new HashMap<String,Float>();
		HashMap<String, Integer> firstWordMap = wrdMap.get(firstWord);
		for(String keyWord : firstWordMap.keySet() ){
			secondWordCount = firstWordMap.get(keyWord);
			percent += ((float)secondWordCount/(float)secondWordSum)*100;
			wordPercentMap.put(keyWord, percent);
		}
		return wordPercentMap;
	}
	/**
	 * @param firstWord
	 * @param wrdMap
	 * @return the total number of occurrences of firstWord
	 */
	public Integer getSum(String firstWord, HashMap<String, HashMap<String, Integer>> wrdMap){
		Integer sum = 0;
		HashMap<String, Integer> firstWordMap = wrdMap.get(firstWord);
		for(String secondWord : firstWordMap.keySet()){
			sum += firstWordMap.get(secondWord);
		}
		return sum;
	}
	
	public int getNumWords() {
		return numWords;
	}

	public void setNumWords(int numWords) {
		this.numWords = numWords;
	}

	public HashMap<String, HashMap<String, Float>> getwordProbablityMap() {
		return wordProbablityMap;
	}
}
