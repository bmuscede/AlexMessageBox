package ca.muscedere.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SettingsBundle implements Serializable{
	private static final long serialVersionUID = 4647737115726164535L;
	private static final String SETTINGS_NAME = "settings.bin";
	
	private static final long DEFAULT_TIME = 30000;
	private static final boolean DEFAULT_SOUND = true;
	private static final boolean DEFAULT_SCREEN = true;
	
	public SettingsBundle( String bundleLocation ) {
		this.bundleLocation = bundleLocation;
		
		// Create the default arguments.
		this.username = "";
		this.password = "";
		this.messageCheckTime = DEFAULT_TIME;
		this.playSound = DEFAULT_SOUND;
		this.runScreen = DEFAULT_SCREEN;
		
		// Check if the file exists.
		File f = new File(bundleLocation + "/" + SETTINGS_NAME);
		if (f.exists())
		{
			// Load in a deserialized object.
			try {
				FileInputStream settingsStream = new FileInputStream(f.toString());
				ObjectInputStream in = new ObjectInputStream(settingsStream);
				
				SettingsBundle load = (SettingsBundle) in.readObject();
				this.username = load.username;
				this.password = load.password;
				this.messageCheckTime = load.messageCheckTime;
				this.playSound = load.playSound;
				this.runScreen = load.runScreen;
				
				in.close();
				settingsStream.close();
			} catch (Exception e) { }
		}
	}
	
	public boolean SaveSettings() {
		FileOutputStream settingsOut;
		try {
			settingsOut = new FileOutputStream(bundleLocation + "/" + SETTINGS_NAME);
			ObjectOutputStream out = new ObjectOutputStream(settingsOut);
			out.writeObject(this);
			out.close();
			settingsOut.close();
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	public boolean UpdateParameters(String username, String password, long messageCheck, boolean sound, boolean screen )
	{
		boolean changed = false;
		if ( username !=  this.username )
		{
			changed = true;
			this.username = username;
		}
		if ( password != this.password )
		{
			changed = true;
			this.password = password;
		}
		if ( messageCheck != this.messageCheckTime )
		{
			changed = true;
			this.messageCheckTime = messageCheck;
		}
		if ( sound != this.playSound )
		{
			changed = true;
			this.playSound = sound;
		}
		if ( screen != this.runScreen )
		{
			changed = true;
			this.runScreen = screen;
		}
		
		// Attempt to save the settings.
		if ( !changed ) return true;
		return SaveSettings();
		
	}
	
	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public long getMessageCheckTime() {
		return messageCheckTime;
	}

	public boolean shouldPlaySound() {
		return playSound;
	}

	public boolean shouldRunScreen() {
		return runScreen;
	}

	private transient String bundleLocation;
	
	private String username;
	private String password;
	private long messageCheckTime;
	private boolean playSound;
	private boolean runScreen;
}
