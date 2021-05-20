package ca.muscedere.message;

public class MessageBundle {
	public MessageBundle(String message, String messageURL) {
		this.message = message;
		this.messageURL = messageURL;
	}
	
	public MessageBundle(String messageDate, String message, String messageURL) {
		this.messageDate = messageDate;
		this.message = message;
		this.messageURL = messageURL;
	}
	
	public boolean hasMessageDate() {
		return !messageDate.isEmpty();
	}
	
	public String getMessageDate() {
		return messageDate;
	}

	public String getMessage() {
		return message;
	}

	public String getMessageURL() {
		return messageURL;
	}


	private String messageDate;
	private String message;
	private String messageURL;
}
