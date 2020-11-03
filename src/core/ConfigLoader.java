package core;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.ini4j.*;

public class ConfigLoader {

	File config;
	String FileFolderName = "TypingHell";
	String FileFolder = System.getenv("APPDATA") + "\\" + FileFolderName;
	String configPATH = FileFolder + "\\" + "config.ini";
	Wini configIni;

	public ConfigLoader() {
		createConfig();
		loadConfig();

	}

	public void saveConfig() {

		try {
			configIni.put("Settings", "font-size", GUI.fontSizeSaved);
			configIni.put("Settings", "info-display", GUI.infoDisplay);
			configIni.put("Settings", "resolution", Tetris.effectiveResolution);
			configIni.put("Settings", "sounds", GUI.soundsEnabled);
			configIni.put("Settings", "theme", GUI.theme);
			configIni.put("Settings", "eventWords", GUI.eventWords);
			configIni.store();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	public void loadConfig() {

		// ########## Font Size Saved
		if (configIni.get("Settings", "font-size") == null) {
			GUI.fontSizeSaved = -1;
		} else {
			GUI.fontSizeSaved = Integer.parseInt(configIni.get("Settings", "font-size"));
		}

		// ########## Info Display
		if (configIni.get("Settings", "info-display") == null) {
			GUI.infoDisplay = 1;
		} else {
			GUI.infoDisplay = Integer.parseInt(configIni.get("Settings", "info-display"));
		}

		// ########## Resolution
		if (configIni.get("Settings", "resolution") == null) {
			Tetris.effectiveResolution = 800;
		} else {
			Tetris.effectiveResolution = Integer.parseInt(configIni.get("Settings", "resolution"));
		}

		// ########## Sounds
		if (configIni.get("Settings", "sounds") == null) {
			GUI.soundsEnabled = true;
		} else {
			GUI.soundsEnabled = Boolean.parseBoolean((String) configIni.get("Settings", "sounds"));
		}

		// ########## Theme
		if (configIni.get("Settings", "theme") == null) {
			GUI.theme = GUI.THEME_DEFAULT;
		} else {
			GUI.theme = Integer.parseInt((String) configIni.get("Settings", "theme"));
		}

		// ########## Event Words
		if (configIni.get("Settings", "eventWords") == null) {
			GUI.eventWords = true;
		} else {
			GUI.eventWords = Boolean.parseBoolean((String) configIni.get("Settings", "eventWords"));
		}

	}

	private void createConfig() {
		System.out.println("Searching for system");
		config = new File(configPATH);

		try {
			configIni = new Wini(config);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String os = System.getProperty("os.name").toUpperCase();
		if (os.contains("WIN")) {
			FileFolder = System.getenv("APPDATA") + "\\" + FileFolderName;
			System.out.println("Found windows");
		}
		if (os.contains("MAC")) {
			FileFolder = System.getProperty("user.home") + "/Library/Application " + "Support" + FileFolderName;
			System.out.println("Found mac");
		}
		if (os.contains("NUX")) {
			FileFolder = System.getProperty("user.dir") + "." + FileFolderName;
			System.out.println("Found linux");
		}

		System.out.println("Searching for resource folder");
		File directory = new File(FileFolder);

		if (directory.exists()) {
			System.out.println("Found folder");
		} else {
			directory.mkdir();
			infoBox("Welcome to " + GUI.game_name + "! \n" + "It seems like this is your first start. \n"
					+ "A config file has been created at " + FileFolder + " \n"
					+ "Excuse the inconveniences, but you need to restart the game once.", "Setting up...");
		}

		try {
			config.createNewFile();
			System.out.println("config File has been created or already exists");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("config File couldnt be created or found");
		}
	}

	public void infoBox(String infoMessage, String titleBar) {
		JOptionPane.showMessageDialog(null, infoMessage, GUI.frame_title + " " + titleBar,
				JOptionPane.INFORMATION_MESSAGE);
	}

}
