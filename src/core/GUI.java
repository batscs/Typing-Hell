package core;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class GUI {

	JFrame frame;
	JPanel panel;
	JTextField textfield;

	// String frame_title = "Typing Hell |";
	static String game_name = "Typing Hell";
	static String frame_title = game_name + " |";
	String frame_title_suffix = " | Version: " + Tetris.version;

	int score = 0;

	int matrixLength = 20;

	// Stats to save in config file
	static int infoDisplay;
	static int fontSizeSaved;
	static int resolutionSaved = -1;
	public static boolean soundsEnabled = true;

	public boolean lost = true;

	public boolean story = true;
	int storyInc = 1;

	public boolean searchRunning = false;
	public boolean physicsRunning = false;

	// label[] zu JTextArea machen damit keine "..." mehr
	public JTextArea label[] = new JTextArea[matrixLength * matrixLength + 1];
	JLabel labelScore = new JLabel();
	JLabel labelWPM = new JLabel();
	JLabel labelCombo = new JLabel();

	double startTime = LocalTime.now().toNanoOfDay();
	double currentTime = LocalTime.now().toNanoOfDay();

	int countedLetters = 0;
	int countedWords = 0;
	int correctWords = 0;
	int WPM = 0;

	static boolean eventWords;

	int comboCount = 0;
	int highestCombo = 0;

	// TODO
	// chance2getEventWord / 1000 = Chance
	int chance2getEventWord = 3;

	boolean firstCount = true;
	boolean hadfirstStart = false;

	static int THEME_DEFAULT = 1;
	static int THEME_GRAYSCALE = 2;
	static int theme;
	// Color colorDead = Color.decode("#8b0000");
	Color colorDead = Color.decode("#473939");
	Color colorPlay = Color.DARK_GRAY;
	Color colorInside = Color.black;

	Font font;

	public GUI(int frameWidth) {

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setSize(frameWidth, (int) (frameWidth * 1.05f));
		frame.setTitle(frame_title + " Starting" + frame_title_suffix);
		frame.setVisible(true);
		frame.setResizable(true);
		frame.setLocationRelativeTo(null);

		panel = (JPanel) frame.getContentPane();
		panel.setLayout(null);
		
		for (JTextArea labels : label) {
			labels = new JTextArea();
			labels.setVisible(true);
		}

		setFont();

		Image icon1 = Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("core/logo.png"));

		frame.setIconImage(icon1);

		checkForNotLoadedValues();

	}

	void checkForNotLoadedValues() {
		if (infoDisplay == -1) {
			infoDisplay = 1;
		}

		if (theme == -1 || theme == 0) {
			theme = THEME_DEFAULT;
		}

	}

	public void toggleInfoDisplay() {

		// 1 = Combo
		// 2 = WPM

		if (infoDisplay == 1) {
			infoDisplay = 2;

			labelWPM.setVisible(true);
			labelCombo.setVisible(false);
			calculateWPM();

		} else if (infoDisplay == 2) {
			infoDisplay = 1;

			labelWPM.setVisible(false);
			labelCombo.setVisible(true);
			calculateLabelCombo();

		}

		System.out.println("info" + infoDisplay);

		Tetris.cfgL.saveConfig();
	}

	public void increaseCombo() {
		comboCount++;

		if (comboCount >= highestCombo) {
			highestCombo = comboCount;
		}

	}

	public void setFont() {
		if (fontSizeSaved == -1) {
			fontSizeSaved = (int) (frame.getWidth() * 0.03625);
		}
		font = new Font("Trebuchet MS", Font.BOLD, fontSizeSaved);
	}

	public void setFont(int size) {
		fontSizeSaved = size;
		font = new Font("Trebuchet MS", Font.BOLD, size);

		Tetris.cfgL.saveConfig();
	}

	public void turnRed() {
		if (!hadfirstStart) {
			turnGray();
			return;
		}

		panel.setBackground(colorDead);
		labelScore.setBackground(colorDead);
		labelWPM.setBackground(colorDead);
		labelCombo.setBackground(colorDead);

	}

	public void turnGray() {
		panel.setBackground(colorPlay);
		labelScore.setBackground(colorPlay);
		labelWPM.setBackground(colorPlay);
		labelCombo.setBackground(colorPlay);

	}

	public void resetWPM() {
		startTime = LocalTime.now().toNanoOfDay();
		currentTime = LocalTime.now().toNanoOfDay();
		countedLetters = 0;
		countedWords = 0;
		correctWords = 0;
		comboCount = 0;
		highestCombo = 0;
		WPM = 0;
	}

	public void calculateWPM() {

		currentTime = LocalTime.now().toNanoOfDay();
		double elapsedTime = currentTime - startTime;
		double seconds = elapsedTime / 1000000000;
		WPM = (int) ((((double) countedLetters / 5) / seconds) * 60);

		panel.repaint();
		labelWPM.setText("WPM: " + WPM);
	}

	public void calculateLabelCombo() {
		labelCombo.setText("Combo: " + comboCount);
		panel.repaint();

		if (comboCount % 10 == 0) {
			if (comboCount != 0) {
				labelCombo.setForeground(Color.RED);
			}
		} else {
			labelCombo.setForeground(Color.white);
		}

	}

	public void drawMatrix() {
		drawJLabels();
		drawTextField();
		drawLabelScore();
		resetWPM();
		drawLabelCombo();
		drawLabelWPM();

		setFont();
		turnRed();

		drawIntro();

		textfield.setText("type: start <number>");
	}

	private void eventWordListener(String eventWord) {
		// TODO
		if (eventWord.equals("{fr33ze}")) {
			breakEngineCount = 20;
		} else if (eventWord.equals("[r3moveALL]")) {
			clearJLabelRange(0, label.length - 1);
		}
		if (eventWord.equals("-NOMORESPAWNS-")) {
			breakSpawnerCount = 8;
		}
	}

	public void generateWord(Color color) {
		if (lost) {
			turnRed();
			drawStats();
			return;
		}

		if (physicsRunning) {
			return;
		}

		// while (!foundPosition) {
		FileSearcher fs = new FileSearcher();

		String word = fs.getRandomWord();

		if (Tetris.currentWordList.equals("words10k.txt")) {
			// Random zahl Zwischen (maxDifficulty - currentDifficulty) und 0
			// Chance langes Wort zu bekommen höher wenn im Story Modus
			int rond = (int) (Math.random() * ((Tetris.maxDifficulty - Tetris.difficulty) - 0 + 1)
					+ (Tetris.maxDifficulty - Tetris.difficulty));

			if (rond == 0) {
				int bonk = (int) (Math.random() * (5 - 0 + 1) + 1);

				if (bonk == 5) {
					word = fs.getRandomWordLongerThan(13);
				} else if (bonk == 4) {
					word = fs.getRandomWordShorterThan(5);
				}
			}
		}

		if (eventWords) {
			int randomEvent = (int) (Math.random() * (1000 - 0 + 1) + 0);
			if (randomEvent <= chance2getEventWord) {
				word = fs.getRandomWordEvent();
			}
		}

		String[] word_split = word.split("");
		Color word_color = color;
		Color tempForeground = Color.WHITE;

		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getBlue(), color.getGreen(), null);

		float brightness = hsb[2];

		if (brightness < 0.7) {
			tempForeground = Color.white;

		} else {
			tempForeground = Color.black;

		}

		int x = (int) (Math.random() * (matrixLength - word_split.length - 0 + 1) + 0);

		int emptyCount = 0;
		for (int i = x; i < x + word_split.length; i++) {
			if (isJLabelEmpty(i)) {
				emptyCount++;
			}
		}

		if (emptyCount >= word_split.length) {
			for (int i = 0; i < word_split.length; i++) {
				changeJLabel(x + i, word_split[i], word_color);
				changeJLabelForeground(x + i, tempForeground);
			}
		} else {
			turnRed();
			lost = true;
		}

		// failSafe--;

		// if (failSafe <= 0) {
		// foundPosition = true;
		// }

		// }

	}

	public JFrame getFrame() {
		return frame;
	}

	public JPanel getPanel() {
		return panel;
	}

	static boolean physicsBlocked = false;
	static int breakEngineCount = 0;
	static int breakSpawnerCount = 0;

	public void dropWords() {

		while (searchRunning) {
			System.out.println("looping, waiting for word search to be finished");
		}

		physicsRunning = true;

		if (physicsBlocked) {
			return;
		}

		int wordPos = -1, wordSize = -1;
		Color currentColor = null;
		for (int i = matrixLength - 1; i > 0; i--) {
			for (int j = 0; j <= matrixLength; j++) {

				int currentIndex = ((i - 1) * matrixLength) + j;

				if (currentIndex >= matrixLength * matrixLength) {
					System.out.println("current index too big");
					return;
				}

				if (getJLabelColor(currentIndex) != Color.black) {
					if (getJLabelColor(currentIndex) != currentColor) {
						currentColor = getJLabelColor(currentIndex);

						// //////////////////////////////
						// DROP DIESES WORT HIER LETS GO
						if (wordPos != -1 && wordSize != -1) {
							dropLogic(wordPos, wordSize + 1);
						}
						// //////////////////////////////

						wordPos = currentIndex;
						wordSize = -1;

					} else {
						// gleiche farbe, gleiches wort
						if (wordSize == -1) {
							wordSize = 0;
						}

						wordSize++;
					}

				} else {

					// //////////////////////////////
					// DROP DIESES WORT HIER LETS GO
					if (wordPos != -1 && wordSize != -1) {
						dropLogic(wordPos, wordSize + 1);
					}
					// //////////////////////////////

					wordPos = -1;
					wordSize = -1;

					// check if word can drop 1 zeile, if so do it

				}
			}
		}

		// Letzte Zeile wird hier durchsucht nach einzelnen Wörtern
		if (!lost) {
			for (int q = label.length - 1; q >= 0; q--) {
				if (label[q].getBackground() != Color.black) {
					if (q < matrixLength * matrixLength - matrixLength) {
						if (label[q + 1].getBackground() != label[q].getBackground()) {
							if (label[q - 1].getBackground() != label[q].getBackground()) {
								dropLogic(q, 1);
							}
						}
					}

				}
			}
		}

		physicsRunning = false;
	}

	private void dropLogic(int pos, int size) {

		if (searchRunning) {
			return;
		}

		int dropCount = 0;
		for (int i = pos; i < pos + size; i++) {
			if (isJLabelEmpty(i + matrixLength)) {
				dropCount++;
			}
		}

		if (dropCount >= size) {
			for (int i = 0; i < size; i++) {

				putLabelDown(pos + i, pos + i + matrixLength);

			}

		}

	}

	private Color getJLabelColor(int index) {
		return label[index].getBackground();
	}

	public boolean hasDrawnStats = false;

	public void drawIntro() {

		resetMatrix();
		physicsBlocked = true;

		Color backg = Color.decode("#34495e");
		Color foreg = Color.decode("#ecf0f1");

		matrixSetText(0, "Welcome to", foreg, backg);
		matrixSetText(1, game_name + "!", foreg, backg);
		matrixSetText(19, "For help type: help", foreg, backg);
	}

	public void drawHelpList() {
		resetMatrix();
		physicsBlocked = true;

		Color backg = Color.decode("#34495e");
		Color foreg = Color.decode("#ecf0f1");

		matrixSetText(0, "Commands:", foreg, backg);
		matrixSetText(1, "• help 1", foreg, backg);
		matrixSetText(2, "• help 2", foreg, backg);
	}

	public void drawHelpPage1() {
		resetMatrix();
		physicsBlocked = true;

		Color backg = Color.decode("#34495e");
		Color foreg = Color.decode("#ecf0f1");

		matrixSetText(0, "Commands:", foreg, backg);
		matrixSetText(1, "• start", foreg, backg);
		matrixSetText(2, "• start <difficulty>", foreg, backg);

		matrixSetText(4, "• toggle sounds", foreg, backg);
		matrixSetText(5, "• toggle words", foreg, backg);
		matrixSetText(6, "• toggle info", foreg, backg);
		matrixSetText(7, "• toggle theme", foreg, backg);
		matrixSetText(8, "• toggle events", foreg, backg);

		matrixSetText(10, "• set font <size>", foreg, backg);
	}

	private void drawStats() {

		if (hasDrawnStats) {
			return;
		}

		if (!hadfirstStart) {
			return;
		}

		while (physicsRunning) {

		}

		resetMatrix();
		physicsBlocked = true;

		float acc = (float) correctWords / (float) countedWords;
		int accFinal = (int) (acc * 100);

		System.out.println(accFinal);

		Color backg = Color.decode("#34495e");
		Color foreg = Color.decode("#ecf0f1");

		backg = getRandomColor();
		foreg = null;

		matrixSetText(0, "You lost!", foreg, backg);

		matrixSetText(2, "Statistics:", foreg, backg);
		// matrixSetText(1, "-------------------------", null, getRandomColor());
		matrixSetText(4, "eScore: " + (int) ((double) score * (double) acc), foreg, backg);
		matrixSetText(5, "WPM: " + WPM, foreg, backg);
		matrixSetText(6, "Accuracy: " + accFinal + "%", foreg, backg);
		matrixSetText(7, "Words: " + countedWords, foreg, backg);
		matrixSetText(8, "Events: " + eventWords, foreg, backg);

		matrixSetText(10, "Highest Combo: " + highestCombo + "x", foreg, backg);
		matrixSetText(11, "Combo: " + comboCount + "x", foreg, backg);

		String modeType;

		if (story)
			modeType = "Increasing";
		else
			modeType = "Static";

		matrixSetText(11, "Mode: " + modeType, foreg, backg);
		matrixSetText(12, "Difficulty: " + Tetris.difficulty, foreg, backg);

		hasDrawnStats = true;

	}

	private void matrixSetText(int row, String text, Color foreg, Color backg) {
		String[] text_splitted = text.split("");

		int indexRow = row * matrixLength;

		if (foreg == null) {
			float[] hsb = Color.RGBtoHSB(backg.getRed(), backg.getBlue(), backg.getGreen(), null);

			float brightness = hsb[2];

			if (brightness < 0.6) {
				foreg = Color.white;

			} else {
				foreg = Color.black;

			}
		}

		for (int i = 0; i < matrixLength; i++) {

			int currentIndex = indexRow + i;
			if (i < text_splitted.length) {
				label[currentIndex].setText(" " + text_splitted[i]);
				label[currentIndex].setForeground(foreg);
				if (!(label[currentIndex].getText().equals(" " + " "))) {
					label[currentIndex].setBackground(backg);
				} else {
					label[currentIndex].setBackground(backg);
				}
				// System.out.println(i);
			}
		}

	}

	private void putLabelDown(int original, int destination) {
		label[destination].setBackground(label[original].getBackground());
		label[destination].setText(label[original].getText());
		label[destination].setForeground(label[original].getForeground());
		// label[destination].setBorder(label[original].getBorder());

		resetJLabel(original);
	}

	private boolean isJLabelEmpty(int index) {
		if (index >= label.length) {
			return true;
		}
		if (label[index].getBackground() == Color.black) {
			return true;
		} else {
			return false;
		}
	}

	private void resetMatrix() {
		for (int i = 0; i < matrixLength * matrixLength; i++) {
			resetJLabel(i);
		}
	}

	public void changeJLabelForeground(int index, Color color) {
		label[index].setForeground(color);
	}

	public void changeJLabel(int index, String text, Color color) {
		label[index].setText(" " + text);
		label[index].setBackground(color);
		panel.repaint();
	}

	public void resetJLabel(int index) {
		label[index].setBackground(Color.black);
		label[index].setText(" ");
		panel.repaint();
	}

	private Color getRandomColor() {

		float r = 0, g = 0, b = 0;

		if (theme == THEME_DEFAULT) {
			Random rand = new Random();
			r = rand.nextFloat();
			g = rand.nextFloat();
			b = rand.nextFloat();
		} else if (theme == THEME_GRAYSCALE) {
			Random rand = new Random();
			float grayScale = rand.nextFloat();
			r = grayScale;
			g = grayScale;
			b = grayScale;
		}
		return new Color(r, g, b);

	}

	public void generateWordRandomColor() {
		generateWord(getRandomColor());
	}

	public void calculateTitle() {

		if (!lost) {

			if (Tetris.difficulty > Tetris.maxDifficulty) {
				Tetris.difficulty = Tetris.maxDifficulty;
			}

			int diff = Tetris.difficulty;

			if (story) {
				frame.setTitle(frame_title + " Increasing at Difficulty: " + diff + "/" + Tetris.maxDifficulty
						+ frame_title_suffix);
			} else {
				frame.setTitle(frame_title + " Static at Difficulty: " + diff + "/" + Tetris.maxDifficulty
						+ frame_title_suffix);
			}
		}

	}

	boolean Switch = false;

	public void increaseDifficulty() {
		if (Switch) {
			Tetris.difficulty += storyInc;
			Switch = false;
		} else {
			Switch = true;
		}
	}

	public void toggleMatrixTheme() {
		if (theme == THEME_DEFAULT) {
			theme = THEME_GRAYSCALE;
		} else if (theme == THEME_GRAYSCALE) {
			theme = THEME_DEFAULT;
		}

		Tetris.cfgL.saveConfig();
	}

	public void submitInput() {

		String text = textfield.getText();
		String text2split = textfield.getText();
		if (text.startsWith(" ")) {
			text2split = text2split.substring(1);
		}
		String[] text_splitted = text2split.split(" ");

		if (firstCount) {
			firstCount = false;
		} else {
			if (!lost) {
				countedLetters += text.split("").length;
				countedWords++;
			}
		}

		if (lost) {
			Color backg = Color.decode("#34495e");
			Color foreg = Color.decode("#ecf0f1");
			
			if (text_splitted[0].equalsIgnoreCase("start")) {
				resetMatrix();
				lost = false;
				int diff = 0;

				if (text_splitted.length <= 1) {
					diff = 25;
					story = true;

				} else if (text_splitted.length > 1) {

					diff = Integer.parseInt(text_splitted[1]);
					story = false;

					if (diff > Tetris.maxDifficulty) {
						diff = Tetris.maxDifficulty;
					} else if (diff <= 0) {
						diff = 7;
					}
				}

				Tetris.difficulty = diff;

				turnGray();
				textfield.setText("");

				score = 0;
				labelScore.setText("Score: " + score);

				calculateTitle();
				resetWPM();
				hadfirstStart = true;
				hasDrawnStats = false;
				physicsBlocked = false;

			} else if (text_splitted[0].equalsIgnoreCase("toggle")) {
				// TODO
				if (text_splitted.length <= 1) {
					clearJLabelRange(matrixLength * 19, matrixLength);
					matrixSetText(19, "> not enough args", foreg, backg);
					textfield.setText("");
					return;
				}

				if (text_splitted[1].equalsIgnoreCase("sounds")) {

					soundsEnabled = !soundsEnabled;
					clearJLabelRange(matrixLength * 19, matrixLength);
					matrixSetText(19, "> Sounds:" + soundsEnabled, foreg, backg);
					textfield.setText("");
					Tetris.cfgL.saveConfig();

				} else if (text_splitted[1].equalsIgnoreCase("words")) {

					if (Tetris.currentWordList.equalsIgnoreCase("words10k.txt")) {
						Tetris.currentWordList = "words1k.txt";
						clearJLabelRange(matrixLength * 19, matrixLength);
						matrixSetText(19, "> Words: 1000 words", foreg, backg);
						textfield.setText("");

					} else if (Tetris.currentWordList.equalsIgnoreCase("words1k.txt")) {
						Tetris.currentWordList = "words10k.txt";
						clearJLabelRange(matrixLength * 19, matrixLength);
						matrixSetText(19, "> Words: 10000 words", foreg, backg);
						textfield.setText("");
					}
					Tetris.cfgL.saveConfig();

				} else if (text_splitted[1].equalsIgnoreCase("info")) {
					toggleInfoDisplay();
					textfield.setText("");
				} else if (text_splitted[1].equalsIgnoreCase("theme")) {
					toggleMatrixTheme();
					if (theme == THEME_DEFAULT) {
						clearJLabelRange(matrixLength * 19, matrixLength);
						matrixSetText(19, "> Theme: Default", foreg, backg);
						textfield.setText("");
					} else if (theme == THEME_GRAYSCALE) {
						clearJLabelRange(matrixLength * 19, matrixLength);
						matrixSetText(19, "> Theme: Grayscale", foreg, backg);
						textfield.setText("");
					}
				} else if (text_splitted[1].equalsIgnoreCase("events")) {
					eventWords = !eventWords;
					clearJLabelRange(matrixLength * 19, matrixLength);
					matrixSetText(19, "> Event Words: " + eventWords, foreg, backg);
					textfield.setText("");
					Tetris.cfgL.saveConfig();
				}

			} else if (text_splitted[0].equalsIgnoreCase("set")) {
				if (text_splitted.length > 1) {
					if (text_splitted[1].equalsIgnoreCase("font")) {
						if (text_splitted.length > 2) {
							setMatrixFontSize(Integer.parseInt(text_splitted[2]));
							clearJLabelRange(matrixLength * 19, matrixLength);
							matrixSetText(19, "> Font Size: " + text_splitted[2], foreg, backg);
							textfield.setText("");
						}
					}
				}

			} else if (text_splitted[0].equalsIgnoreCase("help")) {
				if (text_splitted.length == 1) {
					drawHelpPage1();
				} else if (text_splitted.length == 2) {
					if (text_splitted[1].equals("1")) {
						drawHelpPage1();
					}
				}
				textfield.setText("");
			}

			return;
		}

		if (text.equals("")) {
			return;
		}

		searchForWord(text.replaceAll("\\s+", ""));

		if (soundsEnabled) {
			SoundManager.playSound(SoundManager.change, -20f);
		}

		textfield.setText("");
	}

	private void setMatrixFontSize(int size) {
		setFont(size);
		fontSizeSaved = size;

		labelScore.setFont(font);
		labelWPM.setFont(font);
		labelCombo.setFont(font);
		textfield.setFont(font);

		for (int i = 0; i < label.length; i++) {
			label[i].setFont(font);
		}
	}

	public void infoBox(String infoMessage, String titleBar) {
		JOptionPane.showMessageDialog(null, infoMessage, frame_title + " " + titleBar, JOptionPane.INFORMATION_MESSAGE);
	}

	private void highlightCurrentWord(String word) {

		int wordPos = -1, wordSize = -1;
		String tempWord = "";
		Color currentColor = null;
		// ********************************************************************************
		for (int i = matrixLength - 1; i > 0; i--) {
			for (int j = 0; j <= matrixLength; j++) {

				int currentIndex = ((i - 1) * matrixLength) + j;

				if (currentIndex > matrixLength * matrixLength) {
					System.out.println("current index too big");
					return;
				}

				if (getJLabelColor(currentIndex) != Color.black) {
					if (getJLabelColor(currentIndex) != currentColor) {
						currentColor = getJLabelColor(currentIndex);

						// //////////////////////////////
						// DROP DIESES WORT HIER LETS GO
						if (wordPos != -1 && wordSize != -1) {
							tempWord = tempWord.replaceAll("\\s+", "");
							if (tempWord.startsWith(word)) {

								highlightWord(wordPos, word.length() - 1);

							} else {
								unhighlightWord(wordPos, word.length() - 1);
							}
						}
						// //////////////////////////////

						wordPos = currentIndex;
						wordSize = -1;
						tempWord = "";

					} else {
						// gleiche farbe, gleiches wort
						if (wordSize == -1) {
							wordSize = 0;
							tempWord += label[currentIndex - 1].getText();
						}

						wordSize++;
						tempWord += label[currentIndex].getText();
					}

				} else {

					// //////////////////////////////
					// DROP DIESES WORT HIER LETS GO
					if (wordPos != -1 && wordSize != -1) {
						tempWord = tempWord.replaceAll("\\s+", "");
						if (tempWord.startsWith(word)) {

							highlightWord(wordPos, word.length() - 1);

						} else {
							unhighlightWord(wordPos, word.length() - 1);
						}
					}
					// //////////////////////////////

					wordPos = -1;
					wordSize = -1;
					tempWord = "";

					// check if word can drop 1 zeile, if so do it

				}
			}

		}

		// ********************************************************************************
		// Hier wird nur die letzte Zeile durchsucht

		for (int i = 0; i <= matrixLength; i++) {

			int currentIndex = (matrixLength * (matrixLength - 1)) + i;

			if (getJLabelColor(currentIndex) != Color.black) {
				if (getJLabelColor(currentIndex) != currentColor) {
					currentColor = getJLabelColor(currentIndex);

					// //////////////////////////////
					// DROP DIESES WORT HIER LETS GO
					if (wordPos != -1 && wordSize != -1) {
						tempWord = tempWord.replaceAll("\\s+", "");
						if (tempWord.startsWith(word)) {
							highlightWord(wordPos, word.length() - 1);
						} else {
							unhighlightWord(wordPos, word.length() - 1);
						}
					}
					// //////////////////////////////

					wordPos = currentIndex;
					wordSize = -1;
					tempWord = "";

				} else {
					// gleiche farbe, gleiches wort
					if (wordSize == -1) {
						wordSize = 0;
						tempWord += label[currentIndex - 1].getText();
					}

					wordSize++;
					tempWord += label[currentIndex].getText();
				}

			} else {

				// //////////////////////////////
				// DROP DIESES WORT HIER LETS GO
				if (wordPos != -1 && wordSize != -1) {
					tempWord = tempWord.replaceAll("\\s+", "");
					if (tempWord.startsWith(word)) {
						highlightWord(wordPos, word.length() - 1);
					} else {
						unhighlightWord(wordPos, word.length() - 1);
					}
				}
				// //////////////////////////////

				wordPos = -1;
				wordSize = -1;
				tempWord = "";

				// check if word can drop 1 zeile, if so do it

			}

		}

		// Für 1 stellige Wörter
		if (!lost && wordsFound == 0) {
			for (int q = label.length - 1; q >= 0; q--) {
				if (label[q].getText().replaceAll("\\s+", "").startsWith(word)) {

					if (q > label.length - 2) {
						return;
					}

					if (label[q + 1].getBackground() != label[q].getBackground()
							&& label[q - 1].getBackground() != label[q].getBackground()) {

						highlightWord(q, 1);
					} else {
						unhighlightWord(q, 1);
					}

				}
			}
		}

	}

	int wordsFound;

	private void searchForWord(String word) {

		searchRunning = true;
		wordsFound = 0;

		while (physicsRunning) {
			System.out.println("looping waiting for physics to be done");
		}

		int wordPos = -1, wordSize = -1;
		String tempWord = "";
		Color currentColor = null;
		// ********************************************************************************
		for (int i = matrixLength - 1; i > 0; i--) {
			for (int j = 0; j <= matrixLength; j++) {

				int currentIndex = ((i - 1) * matrixLength) + j;

				if (currentIndex > matrixLength * matrixLength) {
					System.out.println("current index too big");
					return;
				}

				if (getJLabelColor(currentIndex) != Color.black) {
					if (getJLabelColor(currentIndex) != currentColor) {
						currentColor = getJLabelColor(currentIndex);

						// //////////////////////////////
						// DROP DIESES WORT HIER LETS GO
						if (wordPos != -1 && wordSize != -1) {
							tempWord = tempWord.replaceAll("\\s+", "");
							if (tempWord.equals(word) && wordsFound == 0) {

								eliminateWord(wordPos, wordSize, word);

							}
						}
						// //////////////////////////////

						wordPos = currentIndex;
						wordSize = -1;
						tempWord = "";

					} else {
						// gleiche farbe, gleiches wort
						if (wordSize == -1) {
							wordSize = 0;
							tempWord += label[currentIndex - 1].getText();
						}

						wordSize++;
						tempWord += label[currentIndex].getText();
					}

				} else {

					// //////////////////////////////
					// DROP DIESES WORT HIER LETS GO
					if (wordPos != -1 && wordSize != -1) {
						tempWord = tempWord.replaceAll("\\s+", "");
						if (tempWord.equals(word) && wordsFound == 0) {

							eliminateWord(wordPos, wordSize, word);

						}
					}
					// //////////////////////////////

					wordPos = -1;
					wordSize = -1;
					tempWord = "";

					// check if word can drop 1 zeile, if so do it

				}
			}

		}

		// ********************************************************************************
		// Hier wird nur die letzte Zeile durchsucht

		for (int i = 0; i <= matrixLength; i++) {

			int currentIndex = (matrixLength * (matrixLength - 1)) + i;

			if (getJLabelColor(currentIndex) != Color.black) {
				if (getJLabelColor(currentIndex) != currentColor) {
					currentColor = getJLabelColor(currentIndex);

					// //////////////////////////////
					// DROP DIESES WORT HIER LETS GO
					if (wordPos != -1 && wordSize != -1) {
						tempWord = tempWord.replaceAll("\\s+", "");
						if (tempWord.equals(word) && wordsFound == 0) {
							eliminateWord(wordPos, wordSize, word);
						}
					}
					// //////////////////////////////

					wordPos = currentIndex;
					wordSize = -1;
					tempWord = "";

				} else {
					// gleiche farbe, gleiches wort
					if (wordSize == -1) {
						wordSize = 0;
						tempWord += label[currentIndex - 1].getText();
					}

					wordSize++;
					tempWord += label[currentIndex].getText();
				}

			} else {

				// //////////////////////////////
				// DROP DIESES WORT HIER LETS GO
				if (wordPos != -1 && wordSize != -1) {
					tempWord = tempWord.replaceAll("\\s+", "");
					if (tempWord.equals(word) && wordsFound == 0) {
						eliminateWord(wordPos, wordSize, word);
					}
				}
				// //////////////////////////////

				wordPos = -1;
				wordSize = -1;
				tempWord = "";

				// check if word can drop 1 zeile, if so do it

			}

		}

		// Für 1 stellige Wörter
		if (!lost && wordsFound == 0) {
			for (int q = label.length - 1; q >= 0; q--) {
				if (label[q].getText().replaceAll("\\s+", "").equals(word)) {
					if (!(q + 2 > label.length)) {
						if (label[q + 1].getBackground() != label[q].getBackground()) {
							if (label[q - 1].getBackground() != label[q].getBackground()) {

								eliminateWord(q, 1, word);
							}
						}
					}
				}
			}
		}

		if (wordsFound == 0) {
			comboCount = 0;
			calculateLabelCombo();
		}

		searchRunning = false;

	}

	private void eliminateWord(int wordPos, int wordSize, String word) {
		wordsFound++;
		correctWords++;
		eventWordListener(word);
		increaseCombo();
		clearJLabelRange(wordPos, wordSize);
		JLabelScoreAdd(word.length());
		calculateLabelCombo();
		calculateWPM();

		if (story) {
			increaseDifficulty();
			calculateTitle();
		}
	}

	private void highlightWord(int wordPos, int wordSize) {
		highlightJLabelRange(wordPos, wordSize);
	}

	private void unhighlightWord(int wordPos, int wordSize) {
		unhighlightJLabelRange(wordPos, wordSize);
	}

	private void JLabelScoreAdd(int addition) {
		score += addition * Tetris.difficulty;
		labelScore.setText("Score: " + score);
		panel.repaint();
	}

	private void clearJLabelRange(int pos, int size) {
		for (int i = pos; i <= pos + size; i++) {
			resetJLabel(i);
		}
	}

	private void highlightJLabelRange(int pos, int size) {
		for (int i = pos; i <= pos + size; i++) {

			if (i > label.length - 1) {
				return;
			}

			int backR = label[i].getBackground().getRed();
			int backG = label[i].getBackground().getGreen();
			int backB = label[i].getBackground().getBlue();
			Color inverted = new Color(255 - backR, 255 - backG, 255 - backB);

			Color dynamic = Color.white;

			if (theme == THEME_DEFAULT) {
				if (backR < 170) {
					dynamic = Color.red;
				} else if (backG < 170) {
					dynamic = Color.green;
				} else if (backB < 170) {
					dynamic = Color.blue;
				} else {
					dynamic = inverted;
				}
			} else if (theme == THEME_GRAYSCALE) {
				dynamic = Color.red;
			}

			label[i].setForeground(dynamic);

		}
	}

	private void unhighlightJLabelRange(int pos, int size) {
		for (int i = pos; i <= pos + size; i++) {

			if (i > label.length - 1) {
				return;
			}

			Color color = label[i].getBackground();
			Color foreg = Color.white;
			float[] hsb = Color.RGBtoHSB(color.getRed(), color.getBlue(), color.getGreen(), null);

			float brightness = hsb[2];

			if (brightness < 0.7) {
				foreg = Color.white;

			} else {
				foreg = Color.black;

			}

			label[i].setForeground(foreg);
		}
	}

	private void drawLabelScore() {

		int x = (int) (frame.getWidth() * 0.06875);
		int y = (int) (frame.getWidth() * 0);
		int width = (int) (frame.getWidth() * 0.5);
		int height = (int) (frame.getWidth() * 0.0625);

		labelScore = new JLabel("Score: " + score);
		labelScore.setVisible(true);
		labelScore.setBounds(x, y, width, height);
		labelScore.setFont(font);
		labelScore.setOpaque(true);
		labelScore.setForeground(Color.white);

		panel.add(labelScore);
		panel.repaint();

	}

	// labelWPM = labelCombo
	private void drawLabelWPM() {

		int x = (int) (frame.getWidth() * 0.75625);
		int y = (int) (frame.getWidth() * 0);
		int width = (int) (frame.getWidth() * 0.85625);
		int height = (int) (frame.getWidth() * 0.0625);

		labelWPM = new JLabel("WPM: " + WPM);
		if (infoDisplay == 2) {
			labelWPM.setVisible(true);
		} else {
			labelWPM.setVisible(false);
		}
		labelWPM.setBounds(x, y, width, height);
		labelWPM.setFont(font);
		labelWPM.setOpaque(true);
		labelWPM.setForeground(Color.white);

		panel.add(labelWPM);
		panel.repaint();
	}

	private void drawLabelCombo() {

		int x = (int) (frame.getWidth() * 0.70625);
		int y = (int) (frame.getWidth() * 0);
		int width = (int) (frame.getWidth() * 0.85625);
		int height = (int) (frame.getWidth() * 0.0625);

		labelCombo = new JLabel("Combo: " + comboCount);
		if (infoDisplay == 1) {
			labelCombo.setVisible(true);
		} else {
			labelCombo.setVisible(false);
		}
		labelCombo.setBounds(x, y, width, height);
		labelCombo.setFont(font);
		labelCombo.setOpaque(true);
		labelCombo.setForeground(Color.white);

		panel.add(labelCombo);
		panel.repaint();
		
		System.out.println("x: " + labelCombo.getBounds().x);
		System.out.println("y: " + labelCombo.getBounds().y);
	}

	public JTextField getTextfield() {
		return textfield;
	}

	private void drawTextField() {

		textfield = new JTextField();
		textfield.setVisible(true);

		int x = (int) (frame.getWidth() * 0.06875);
		int y = (int) (frame.getWidth() * 0.925);
		int width = (int) (frame.getWidth() * 0.85);
		int height = (int) (frame.getWidth() * 0.05555555555);

		textfield.setBounds(x, y, width, height);
		textfield.setFont(font);

		textfield.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				submitInput();
			}

		});

		textfield.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				if (soundsEnabled) {
					SoundManager.playSound(SoundManager.remove, -20f);
				}

				if (!lost && textfield.getText() != "") {
					if (!(textfield.getText().replace(" ", "7355608").contains("7355608"))) {
						highlightCurrentWord(textfield.getText());

					}
				}

			}

			@Override
			public void insertUpdate(DocumentEvent e) {

				if (soundsEnabled) {
					SoundManager.playSound(SoundManager.type, -20f);
				}

				if (!lost && textfield.getText() != "") {
					if (!(textfield.getText().replace(" ", "7355608").contains("7355608"))) {
						highlightCurrentWord(textfield.getText());
					}
				}

			}

			@Override
			public void changedUpdate(DocumentEvent e) {

			}

		});

		panel.add(textfield);
		panel.repaint();
	}

	private void drawJLabels() {

		int width = (int) (frame.getWidth() * 0.0425);

		int xInc = width;
		int yInc = width;

		int xStart = (int) (frame.getWidth() * 0.06875);
		int yStart = (int) (frame.getWidth() * 0.01875);

		int y = yStart;
		int x = xStart;

		for (int i = 0; i < label.length; i++) {
			label[i] = new JTextArea("     ");
			// label[i].setOpaque(true);
			label[i].setVisible(true);
			label[i].setBackground(Color.black);
			label[i].setForeground(Color.white);
			label[i].setFont(font);
			label[i].setLineWrap(true);
			label[i].setWrapStyleWord(true);
			label[i].setEditable(false);
			label[i].setFocusable(false);

			if (i % (matrixLength) == 0) {
				y += yInc;
				x = xStart;
			} else {
				x += xInc;
			}

			label[i].setBounds(x, y, width, width);
			panel.add(label[i]);

		}

		label[label.length - 1].setVisible(false);

		panel.repaint();
	}

}
