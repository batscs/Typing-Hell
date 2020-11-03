package core;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Tetris {

	// last Update @ 25.10.2020 11:20
	static String version = "1.4.4";

	// 1.0.0 	first release
	// 1.0.1 	bug fixes
	// 1.1.0 	added increasing difficulty while playing
	// 1.1.2 	bug fixes
	// 1.2.0 	WPM Counter & Resizable
	// 1.2.1 	bug fixes
	// 1.2.2 	bug fixes
	// 1.3.0 	more words, longer words more often higher difficulty, added help command
	// 1.3.1 	bug fixes: single character words on the matrix fixed, now drop- and writable
	// 1.4.0 	added combos, statistics, and better info displays
	// 1.4.1 	added command: set font <size>
	// 1.4.2 	Words are being highlighted why typing them and settings are being saved
	// 1.4.3 	Notification Message appears on first start to tell where the config file is
	//			effective Score in statistics after the match
	// 1.4.4	Added new Theme (Grayscale)
	//			Added Event Words (1% Chance to Spawn)
	//				freeze: everything freezes in time for 3.4s
	//				no more spawns: no words spawn for 3.2s
	//				eliminate all: all words get removed
	//			Reworked Command Feedback
	
	// TODO: Wörter werden nicht unhighlighted wenn man den geschriebenen Text wegmacht
	// TODO: Noch komischer wirds wenn man mit STRG + A den Text wegmacht und dann weiter schreibt,
	// TODO: Zwei verschiedene Wörter sind dann markiert

	static GUI gui;
	static Timer spawner;
	static Timer dropper;
	static Timer spacer;
	static Timer resizer;
	static boolean generate = false;

	static int difficulty = 10;
	static int maxDifficulty = 100;

	static int oldWidth;
	static int oldHeight;
	
	static int effectiveResolution;

	static boolean isResizable = true;

	static boolean firstDropperRun = true;

	static String currentWordList = "words1k.txt";
	static String currentEventWordList = "eventwords.txt";
	
	static ConfigLoader cfgL;

	public static void main(String[] args) {

		cfgL = new ConfigLoader();
		System.out.println(effectiveResolution);
		gui = new GUI(effectiveResolution);	
		gui.drawMatrix();
		
		

		spawner = new Timer();
		dropper = new Timer();
		spacer = new Timer();
		resizer = new Timer();

		oldWidth = gui.getFrame().getWidth();
		oldHeight = gui.getFrame().getHeight();

		spawner.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				
				if (GUI.breakEngineCount > 0) {
					GUI.breakEngineCount--;
					return;
				}
				
				if (GUI.breakSpawnerCount > 0) {
					GUI.breakSpawnerCount--;
					return;
				}
				
				int rand = (int) ((Math.random() * ((maxDifficulty - 1) + 1)) + 1);

				if (rand <= difficulty) {
					if (!gui.searchRunning) {
						gui.generateWordRandomColor();
					}
				}

			}
		}, 0, 400);

		dropper.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {

				if (firstDropperRun) {
					firstDropperRun = false;
					return;
				}
				
				if (GUI.breakEngineCount > 0) {
					GUI.breakEngineCount--;
					return;
				}

				gui.dropWords();
				if (!gui.lost) {
					gui.calculateWPM();
				}

			}
		}, 0, 300);

		spacer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				spacebarConfirmation();
			}
		}, 0, 20);

		resizer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				
				if (!isResizable) {
					return;
				}
				
				

				JFrame frame = gui.getFrame();
				JPanel panel = gui.getPanel();

				if (oldHeight != frame.getHeight()) {
					frame.setSize((int) (frame.getHeight() * 0.95238095238), frame.getHeight());
					panel.removeAll();
					gui.drawMatrix();

					oldWidth = frame.getWidth();
					oldHeight = frame.getHeight();
					gui.lost = true;
					gui.resetWPM();
					
					effectiveResolution = frame.getWidth();
					cfgL.saveConfig();
					
				} else if (oldWidth != frame.getWidth()) {
					frame.setSize(frame.getWidth(), (int) (frame.getWidth() * 1.05f));
					panel.removeAll();
					gui.drawMatrix();

					oldWidth = frame.getWidth();
					oldHeight = frame.getHeight();
					gui.lost = true;
					gui.resetWPM();
					
					effectiveResolution = frame.getWidth();
					cfgL.saveConfig();
					
				}

			}
		}, 0, 1000);

	}

	public static void spacebarConfirmation() {
		String[] textfield_splitted = gui.getTextfield().getText().split("");
		String lastTextfield = textfield_splitted[textfield_splitted.length - 1];
		if (!gui.lost) {
			if (lastTextfield.equals(" ")) {
				boolean allLabelsEmpty = true;
				
				if (allLabelsEmpty) {
					gui.submitInput();
				}
			}
		}
	}

}
