package ca.muscedere.message;

public interface MessageNotifier {
	void NotifyNewMessage();
	void NotifyMessageDetails(String message, String resource);
	
	boolean ReadyForNext();
}
