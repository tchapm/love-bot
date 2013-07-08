package com.bot.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Corpus {
	HashMap<String, HashMap<String, Integer>> wordMap = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Float>> wordProbablityMap = new HashMap<String, HashMap<String, Float>>();
	private static final String RESOURCE_DIR_NAME = "src/main/resources/";
	private static final String TEXT_PATH = "_text";
	private int numWords = 0;
	static final Logger logger = Logger.getLogger(Bot.class);
	
	public Corpus(String botName) {
		PropertyConfigurator.configure("log4j.properties");
		try {
			this.wordMap = inputCorpus(botName);
			this.wordProbablityMap = setProbability(wordMap);
			logger.info("Words filled from " + botName + " corpus : " + getNumWords());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, HashMap<String, Integer>> inputCorpus(String botName) throws IOException {
		String dirName = RESOURCE_DIR_NAME + botName + TEXT_PATH;
		File dir = new File(dirName);
		HashMap<String, HashMap<String, Integer>> theWordMap = new HashMap<String, HashMap<String,Integer>>();
		if (dir.isDirectory()){
			for (File f : dir.listFiles()){
				if(!f.isHidden()){
					BufferedReader inCorpus = new BufferedReader(new FileReader(f));
					theWordMap = fillMap(inCorpus);
					inCorpus.close();
				}
			}
		}
		return theWordMap;
	}
	
	public HashMap<String, HashMap<String, Integer>> fillMap(BufferedReader in) throws IOException{
		String str = null;
		String secondWord = null;
		String firstWord = null;
		HashMap<String,Integer> tempMap;
		HashMap<String, HashMap<String, Integer>> theWordMap = new HashMap<String, HashMap<String,Integer>>();
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
