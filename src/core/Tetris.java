package core;

import java.util.Timer;
import java.util.TimerTask;

public class Tetris {
	
	static GUI gui;
	static Timer spawner;
	static Timer dropper;
	static Timer resizer;
	static boolean generate = false;
	
	static int difficulty = 10;
	static int maxDifficulty = 10;

	public static void main(String[] args) {
		
		gui = new GUI(800, 15);
		gui.drawMatrix();
		
		spawner = new Timer();
		dropper = new Timer();
		
		spawner.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				int rand = (int) ((Math.random() * ((maxDifficulty - 1) + 1)) + 1);
				
				if (rand <= difficulty) {
					gui.generateWordRandomColor();
				}
						
			}
		}, 0, 600);
		
		dropper.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				
				gui.dropWords();
				
			}
		}, 0, 500);
		
	}

}
