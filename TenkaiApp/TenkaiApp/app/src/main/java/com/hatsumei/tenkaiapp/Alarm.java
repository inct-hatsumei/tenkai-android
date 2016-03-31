package com.hatsumei.tenkaiapp;

import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by Owner on 2016/03/29.
 */
public class Alarm {
	MediaPlayer mmediaPlayer = null;
	private Sound sound;

	public Alarm(MediaPlayer mediaPlayer) {
		mmediaPlayer = mediaPlayer;
		mmediaPlayer.setLooping(true);
	}

	public void readMessage(String message) {
		sound = new Sound(message);
	}

	private class Sound extends Thread{
		public Sound(String message){
			if (message.equals("ON")){
				soundON();
			}else if (message.equals("OFF")) {
				soundOFF();
			}else if (message.equals("Destroy")){
				Destroy();
			}
		}

		public void soundON(){
			mmediaPlayer.start();
		}
		public void soundOFF() {
			mmediaPlayer.stop();
			try {
				mmediaPlayer.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		public void Destroy() {
			mmediaPlayer.release();
			mmediaPlayer = null;
		}
	}
}
