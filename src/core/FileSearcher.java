package core;

import java.io.File;
import java.util.Scanner;

public class FileSearcher {

	Scanner scan;
	int linesOfFile = 871;
	File config;

	String wordStringComplete, wordEventStringComplete;
	String[] wordArray, wordEventArray;

	@SuppressWarnings("resource")
	public FileSearcher() {

		reloadWordArray();

	}

	@SuppressWarnings("resource")
	public void reloadWordArray() {
		wordStringComplete = "";
		wordEventStringComplete = "";

		wordStringComplete = new Scanner(Tetris.class.getResourceAsStream(Tetris.currentWordList), "UTF-8").useDelimiter("999")
				.next();
		
		wordEventStringComplete = new Scanner(Tetris.class.getResourceAsStream(Tetris.currentEventWordList), "UTF-8").useDelimiter("999")
				.next();
		
		wordStringComplete = wordStringComplete.replace("\n", " ");
		wordEventStringComplete = wordEventStringComplete.replace("\n", " ");

		wordArray = wordStringComplete.split(" ");
		wordEventArray = wordEventStringComplete.split(" ");
	}

	public String getRandomWord() {
		reloadWordArray();
		int rand = (int) (Math.random() * ((wordArray.length - 1) - 0 + 1) + 0);

		return wordArray[rand].replaceAll("\\s+", "");
	}
	
	public String getRandomWordEvent() {
		reloadWordArray();
		int rand = (int) (Math.random() * ((wordEventArray.length - 1) - 0 + 1) + 0);

		return wordEventArray[rand].replaceAll("\\s+", "");
	}

	public String getRandomWordLongerThan(int minLength) {
		reloadWordArray();
		boolean found = false;
		int rand = 10;
		while (!found) {
			rand = (int) (Math.random() * ((wordArray.length - 1) - 0 + 1) + 0);
			if (wordArray[rand].length() > minLength) {
				found = true;
			}
		}

		return wordArray[rand].replaceAll("\\s+", "");
	}

	public String getRandomWordShorterThan(int maxLength) {
		reloadWordArray();
		boolean found = false;
		int rand = 10;
		while (!found) {
			rand = (int) (Math.random() * ((wordArray.length - 1) - 0 + 1) + 0);
			if (wordArray[rand].length() <= maxLength) {
				found = true;
			}
		}

		return wordArray[rand].replaceAll("\\s+", "");
	}

	public String getRandomWordBetween(int min, int max) {
		reloadWordArray();
		boolean found = false;
		int rand = 10;
		while (!found) {
			rand = (int) (Math.random() * ((wordArray.length - 1) - 0 + 1) + 0);
			if (wordArray[rand].length() >= min && wordArray[rand].length() <= max) {
				found = true;
			}
		}

		return wordArray[rand].replaceAll("\\s+", "");
	}

}
