package core;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileSearcher {
	
	Scanner scan;
	int linesOfFile = 871;
	File config;
	
	public FileSearcher() {
		config = new File("src/core/words.txt");
		try {
			scan = new Scanner(config);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getRandomWord() {
		
		int rand = (int) (Math.random() * (linesOfFile - 0 + 1) + 0);
		
		for(int i = 0; i < rand - 1; i++) {
			scan.nextLine();
		}
		
		return scan.nextLine();
	}

}
