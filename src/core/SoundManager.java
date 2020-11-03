package core;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class SoundManager {
	
	public static String type = "sounds/type.wav";
	public static String change = "sounds/change.wav";
	public static String remove = "sounds/remove.wav";
	
	public static synchronized void playSound(final String url, final float volumeStrength) {
	     new Thread(new Runnable() {
	     // The wrapper thread is unnecessary, unless it blocks on the
	     // Clip finishing; see comments.
	       public void run() {
	         try {
	           Clip clip = AudioSystem.getClip();
	           
	           // AudioInputStream inputStream = AudioSystem.getAudioInputStream(Terminal.class.getResourceAsStream(url));
	           InputStream bufferedInput = new BufferedInputStream(Tetris.class.getResourceAsStream(url));
	           AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedInput);
	           
	           clip.open(audioStream);
	           
	           FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
	           gainControl.setValue(volumeStrength); // Reduce volume by decibels.
	           
	           clip.start(); 
	         } catch (Exception e) {
	           System.err.println(e.getMessage());
	         }
	       }
	     }).start();
	}

}
