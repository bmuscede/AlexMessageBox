package ca.muscedere.message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

public class MessageChecker {
	public static final String WEB_ADDRESS = "sql174.main-hosting.eu";
	public static final String ID_NAME = "id.bin";
	
	public MessageChecker( long intervalMS, String username, String password, String saveLocation, MessageNotifier notifier )
	{
		this.intervalMS = intervalMS;
		this.isRunning = false;
		this.notifier = notifier;
		this.saveLocation = saveLocation;
		
		retreiver = new MessageRetreiver(WEB_ADDRESS, username, password);
	}
	
	public boolean UpdateCredentials(String username, String password)
	{
		StopChecker();
		retreiver.UpdateCredentials(username, password);
		return RunChecker();
	}
	
	public boolean RunChecker() {
		if ( isRunning ) return false;
		
		// Next, attempt to validate the connection.
		if ( !retreiver.ValidateConnection() ) return false;
		
		// Start the checker thread.
		isRunning = true;
		checkerThread = new Thread(){
			public void run(){
				mainThread();
			}
		};
		checkerThread.start();
		
		return true;
	}
	
	public void StopChecker() {
		if (!isRunning) return;
		isRunning = false;
		
		// Wait for it to drain out.
		try {
			checkerThread.join();
		} catch (InterruptedException e) { }
	}
	
	public Vector<MessageBundle> getAllMessages() {
		return retreiver.getAllMessages(lastMessageID);
	}
	
	private void mainThread() {
		// First, attempt to get the last message ID.
		if ( lastMessageID == 0 )
		{
			File f = new File(saveLocation + "/" + ID_NAME);
			if (f.exists())
			{
				// Load the settings file from disk.
				FileInputStream idStream;
				try {
					idStream = new FileInputStream(f.toString());
					ObjectInputStream in = new ObjectInputStream(idStream);
					lastMessageID = ((Integer) in.readObject()).intValue();
					
					in.close();
					idStream.close();
				} catch (Exception e) {
					lastMessageID = retreiver.GetLatestID();
				}
			} else {
				lastMessageID = retreiver.GetLatestID();
			}
		}
		
		while (isRunning) {
			// TODO: Wake up more but only check on a specific interval.
			try {
				Thread.sleep(intervalMS);
			} catch (InterruptedException e) { /* If we wake up early that's OK. */ }
			
			// Is the notifier ready for more?
			if ( !notifier.ReadyForNext() || !isRunning ) continue;
			
			// Validate the connection.
			if ( !retreiver.ValidateConnection() ) {
				isRunning = false;
				notifier.NotifyNoNetwork();
				continue;
			}
			
			// Check if we have a new message.
			if (retreiver.HasNewMessages(lastMessageID)) {
				notifier.NotifyNewMessage();
				updateMessageID();
				
				// Notify of a new message.
				String text = retreiver.GetNextMessageText(lastMessageID);
				String res = retreiver.GetNextMessageResource(lastMessageID);
				
				notifier.NotifyMessageDetails(text, res);
			}
			
		}
	}
	
	private void updateMessageID() {
		lastMessageID++;
		FileOutputStream idOut;
		try {
			idOut = new FileOutputStream(saveLocation + "/" + ID_NAME);
			ObjectOutputStream out = new ObjectOutputStream(idOut);
			out.writeObject(new Integer(lastMessageID));
			out.close();
			idOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private MessageRetreiver retreiver;
	private MessageNotifier notifier;
	private Thread checkerThread;
	private String saveLocation;
	
	private int lastMessageID = 0;
	private boolean isRunning;
	private long intervalMS;
}
