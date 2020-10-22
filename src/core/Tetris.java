package core;

import java.util.Timer;
import java.util.TimerTask;

public class Tetris {
	
	static GUI gui;
	static Timer game;
	static boolean generate = false;

	public static void main(String[] args) {
		
		gui = new GUI();
		gui.drawMatrix();
		
		game = new Timer();
		game.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				
				gui.dropWords();
				
				
				if (generate) {
					gui.generateWordRandomColor();
					generate = false;
				} else  {
					generate = true;
				}
				
				
			}
		}, 0, 400);
		
	}

}
