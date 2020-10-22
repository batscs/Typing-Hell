package core;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class GUI {

	JFrame frame;
	JPanel panel;
	JTextField textfield;
	
	int score = 0;

	int matrixLength = 14;
	
	public boolean lost = false;

	JLabel label[] = new JLabel[matrixLength * matrixLength];
	JLabel labelScore = new JLabel();
	JLabel labelLost = new JLabel();

	Font font = new Font("Trebuchet MS", Font.BOLD, 42);
	
	SoundManager soundManager = new SoundManager();

	public GUI() {

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setSize(800, 900);
		frame.setTitle("Type Tetris | Made by MAXOHNO");
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		
		panel = (JPanel) frame.getContentPane();
		panel.setBackground(Color.DARK_GRAY);
		panel.setLayout(null);

		for (JLabel labels : label) {
			labels = new JLabel();
			labels.setVisible(true);
		}

	}

	public void drawMatrix() {
		drawJLabels();
		drawTextField();
		drawLabelScore();
		drawLabelLost();
	}

	public void generateWord(Color color) {
		if (lost) {
			panel.setBackground(Color.red);
			labelScore.setBackground(Color.red);
			return;
		}
		
		//while (!foundPosition) {
			FileSearcher fs = new FileSearcher();
			String word = fs.getRandomWord();
			String[] word_split = word.split("");
			Color word_color = color;

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
				}
			} else {
				lost = true;
			}

			//failSafe--;

			//if (failSafe <= 0) {
				//foundPosition = true;
			//}

		//}

	}

	public void dropWords() {

		// TODO: Probleme 2 fix:
		// 1: Es ist ein Loop, die Schleife geht von oben nach unten durch, wenn etwas
		// nach
		// unten gesetzt wird, wird es gleich wieder erkannt und wieder nach unten
		// gesetzt und immer wieder
		// 2: Wenn currentIndex > matrixLength * matrixLength - matrixLength,dann return

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

	}

	private void dropLogic(int pos, int size) {

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

	private void putLabelDown(int original, int destination) {
		label[destination].setText(label[original].getText());
		label[destination].setBackground(label[original].getBackground());

		resetJLabel(original);
	}

	private boolean isJLabelEmpty(int index) {
		if (label[index].getBackground() == Color.black) {
			return true;
		} else {
			return false;
		}
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

		Random rand = new Random();
		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();

		return new Color(r, g, b);

	}

	public void generateWordRandomColor() {
		generateWord(getRandomColor());
	}

	private void submitInput() {

		if (lost) {
			textfield.setText("you just lost | made by MAXOHNO");
			return;
		}
		
		String text = textfield.getText();

		if (text.equals("d")) {
			dropWords();
		} else if (text.equals("g")) {

			generateWordRandomColor();
		}
		
		searchForWord(text.replaceAll("\\s+",""));
		
		SoundManager.playSound("sounds/change.wav", -20f);

		textfield.setText("");
	}

	private void searchForWord(String word) {

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
							tempWord = tempWord.replaceAll("\\s+","");
							if (tempWord.equals(word)) {
								clearJLabelRange(wordPos, wordSize);
								JLabelScoreAdd(word.length());
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
						tempWord = tempWord.replaceAll("\\s+","");
						if (tempWord.equals(word)) {
							clearJLabelRange(wordPos, wordSize);
							JLabelScoreAdd(word.length());
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
				for (int i = matrixLength - 1; i > 0; i--) {
					for (int j = 0; j <= matrixLength; j++) {

						int currentIndex = ((i) * matrixLength) + j;
						
						if (currentIndex >= matrixLength * matrixLength) {
							currentIndex = 195;
						}

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
									tempWord = tempWord.replaceAll("\\s+","");
									if (tempWord.equals(word)) {
										clearJLabelRange(wordPos, wordSize);
										JLabelScoreAdd(word.length());
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
								tempWord = tempWord.replaceAll("\\s+","");
								if (tempWord.equals(word)) {
									clearJLabelRange(wordPos, wordSize);
									JLabelScoreAdd(word.length());
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

	}
	
	private void JLabelScoreAdd(int addition) {
		score += addition;
		labelScore.setText("Score: " + score);
		panel.repaint();
	}

	private void clearJLabelRange(int pos, int size) {
		for(int i = pos; i <= pos + size; i++) {
			resetJLabel(i);
		}
	} 

	private void drawLabelScore() {
		labelScore = new JLabel("Score: " + score);
		labelScore.setVisible(true);
		labelScore.setBounds(50, 10, 685, 50);
		labelScore.setFont(font);
		labelScore.setOpaque(true);
		labelScore.setForeground(Color.white);
		labelScore.setBackground(Color.DARK_GRAY);

		panel.add(labelScore);
		panel.repaint();
	}
	
	private void drawLabelLost() { 
		labelLost = new JLabel("GAME OVER YOU LOSE");
		labelLost.setBounds(0, 300, 800, 200);
		labelLost.setFont(font);
		labelLost.setOpaque(true);
		labelLost.setForeground(Color.white);
		labelLost.setBackground(Color.RED);
		labelLost.setVisible(false);
	
		panel.add(labelLost);
		panel.repaint();
	}
	
	private void drawTextField() {
		textfield = new JTextField();
		textfield.setVisible(true);
		textfield.setBounds(50, 800, 685, 50);
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

				SoundManager.playSound("sounds/remove.wav", -20f);

			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				
				SoundManager.playSound("sounds/type.wav", -20f);

			}

			@Override
			public void changedUpdate(DocumentEvent e) {

			}

		});


		panel.add(textfield);
		panel.repaint();
	}

	private void drawJLabels() {

		int width = 49;

		int xInc = width;
		int yInc = width;

		int xStart = 50;
		int yStart = 20;

		int y = yStart;
		int x = xStart;

		for (int i = 0; i < label.length; i++) {
			label[i] = new JLabel("   ");
			label[i].setOpaque(true);
			label[i].setVisible(true);
			label[i].setBackground(Color.black);
			label[i].setForeground(Color.white);
			label[i].setFont(font);

			if (i % (matrixLength) == 0) {
				y += yInc;
				x = xStart;
			} else {
				x += xInc;
			}

			// System.out.println("i: " + i + " | x: " + x + " | y: " + y + " | iV: " +
			// label[i].isVisible());

			label[i].setBounds(x, y, width, width);
			panel.add(label[i]);

		}

		panel.repaint();
	}

}
