package core;

import java.io.File;
import java.util.Scanner;

public class FileSearcher {
	
	Scanner scan;
	int linesOfFile = 871;
	File config;
	
	String wordStringComplete;
	String[] wordArray;
	
	@SuppressWarnings("resource")
	public FileSearcher() {
		
		wordStringComplete = new Scanner(Tetris.class.getResourceAsStream("words.txt"), "UTF-8").useDelimiter("999").next();
		wordStringComplete = wordStringComplete.replace("\n", " ");
		
		wordArray = wordStringComplete.split(" ");
		
	}
	
	public String getRandomWord() {
		
		int rand = (int) (Math.random() * ((wordArray.length-1) - 0 + 1) + 0);
		
		return wordArray[rand].replaceAll("\\s+", "");
	}

}
