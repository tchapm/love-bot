package com.bot.main;
import java.util.Date;
import java.util.Random;
import com.bot.utils.StringUtils;

/**
 * Class for the response tweet from the bot. 
 * @author tchap
 *
 */
public class Response {
	private String commentor = null;
	private String searchWord = null;
	private String response = null;
	private String dateStr = null;
	private Date date = null;

	public Response(String theFollower) throws InterruptedException {
		this.commentor = theFollower;
		this.searchWord = Response.genRandomStart(theFollower);
	}
	
	public Response(String text, String screenName, Date createdAt) {
		this.searchWord = setSearchString(text);
		this.commentor = screenName;
		this.date = createdAt;
	}
	
	public static String genRandomStart(String seedWord) {
		String startWord;
		Random generator = new Random(System.currentTimeMillis() + seedWord.length());
		int randomInt = generator.nextInt(9);
		switch (randomInt) {
		case 0:  startWord = "Who";
		break;
		case 1:  startWord = "Is";
		break;
		case 2:  startWord = "Are";
		break;
		case 3:  startWord = "To";
		break;
		case 4:  startWord = "How";
		break;
		case 5:  startWord = "That";
		break;
		case 6:  startWord = "Where";
		break;
		case 7:  startWord = "When";
		break;
		default: startWord = "The";
		break;
		}
		return startWord;
	}
	
	public String setSearchString(String inQuestion) {
		String startWord;
		Random generator = new Random(System.currentTimeMillis());
		String[] words = inQuestion.split(" ");
		int rand = generator.nextInt(words.length);
		startWord = words[rand];
		if(startWord.contains("@")){
			if(rand!=0){
				startWord=words[rand-1];
			}else{
				startWord=words[rand+1];
			}
		}
		return StringUtils.stripPunctuation(startWord);
	}

	public String getDateStr() {
		return dateStr;
	}
	public void setDateStr(String date) {
		this.dateStr = date;
	}
	public String getCommenter() {
		return commentor;
	}
	public void setCommentor(String commentor) {
		this.commentor = commentor;
	}
	public String getSearchWord() {
		return searchWord;
	}

	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public void setDate(Date createdAt) {
		this.date  = createdAt;
	}
	public Date getDate() {
		return this.date;
	}
}
