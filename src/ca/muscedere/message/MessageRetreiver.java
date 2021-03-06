package ca.muscedere.message;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class MessageRetreiver {
		
	public MessageRetreiver(String websiteAddress, String username, String encryptedPassword) {
		m_webAddress = websiteAddress;
		m_username = username;
		m_password = encryptedPassword;
	}
	
	public boolean UpdateCredentials(String username, String password)
	{
		m_username = username;
		m_password = password;
		
		return Connect();
	}
	
	public boolean ValidateConnection() {
		boolean status = Connect();
		Disconnect();
		
		return status;
	}
	
	private boolean Connect() {
		if ( IsConnected() ) return true;
		
		// Try to create a connection.
		try {
			m_dbConnection = DriverManager.getConnection("jdbc:mysql://" + m_webAddress, m_username, m_password);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private boolean Disconnect() {
		if ( !IsConnected() ) return true;
		
		try {
			m_dbConnection.close();
		} catch (SQLException e) {
			return false;
		}
		
		return true;
	}
	
	private boolean IsConnected() {
		try {
			if ( m_dbConnection == null || m_dbConnection.isClosed() ) return false;
		} catch (SQLException e) {
			// If we hit here, assume we're closed.
			return false;
		}
		
		return true;
	}
	
	public boolean HasNewMessages()
	{
		return HasNewMessages(-1);
	}
	
	public boolean HasNewMessages(int lastMessageID) {
		if ( !Connect() ) return false;
		
		// Generate SQL statement to check.
		int count = 0;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = m_dbConnection.createStatement();
			rs = stmt.executeQuery(COUNT_MESSAGE + lastMessageID);
			if ( !rs.next() ) return false;
			
			count = rs.getInt(1);
		} catch (SQLException e) {
			return false;
		} finally {
			try {
				if ( rs != null ) rs.close();
				if ( stmt != null ) stmt.close();
			} catch (Exception e) {}
			Disconnect();
		}
		
		// Finally, check the results.
		if ( count > 0 ) return true;
		return false;
	}
	
	public int GetLatestID() {
		int lastID = 0;
		if ( !Connect() ) return lastID;
		
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = m_dbConnection.createStatement();
			rs = stmt.executeQuery(LAST_ID_MESSAGE);
			
			while (rs.next()) {
				lastID = rs.getInt("ID");
			}
		} catch (SQLException e) {
			return lastID;
		} finally {
			try {
				if ( rs != null ) rs.close();
				if ( stmt != null ) stmt.close();
			} catch (Exception e) {}
			Disconnect();
		}
		
		return lastID;
	}
	
	// TODO: Combine these.
	public String GetNextMessageText(int messageID) {
		if ( !Connect() ) return "";
		
		Statement stmt = null;
		ResultSet rs = null;
		String message = "";
		try {
			stmt = m_dbConnection.createStatement();
			rs = stmt.executeQuery(TEXT_MESSAGE + messageID);
	
			while (rs.next()) {
				message = rs.getString("Text");
			}
		} catch (SQLException e) {
			return "";
		} finally {
			try {
				if ( rs != null ) rs.close();
				if ( stmt != null ) stmt.close();
			} catch (Exception e) {}
			
			Disconnect();
		}
		
		return message;
	}
	
	public String GetNextMessageResource(int messageID) {
		if ( !Connect() ) return "";
		
		Statement stmt = null;
		ResultSet rs = null;
		String message = "";
		try {
			stmt = m_dbConnection.createStatement();
			rs = stmt.executeQuery(RES_MESSAGE + messageID);
	
			while (rs.next()) {
				message = rs.getString("Media");
			}
		} catch (SQLException e) {
			return "";
		} finally {
			try {
				if ( rs != null ) rs.close();
				if ( stmt != null ) stmt.close();
			} catch (Exception e) {}
			
			Disconnect();
		}
		
		return message;
	}
	
	public Vector<MessageBundle> getAllMessages(int lastMessageID) {
		if ( !Connect() ) return new Vector<MessageBundle>();
		
		Statement stmt = null;
		ResultSet rs = null;
		Vector<MessageBundle> bundles = new Vector<MessageBundle>();
		try {
			stmt = m_dbConnection.createStatement();
			rs = stmt.executeQuery(ALL_MESSAGE + lastMessageID);
	
			while (rs.next()) {
				MessageBundle cur = new MessageBundle(rs.getDate("Date").toLocalDate().toString(),
						rs.getString("Text"), rs.getString("Media"));
				bundles.add(cur);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return bundles;
		} finally {
			try {
				if ( rs != null ) rs.close();
				if ( stmt != null ) stmt.close();
			} catch (Exception e) {}
			
			Disconnect();
		}
		
		return bundles;
	}
	
	private Connection m_dbConnection = null;
	
	private String m_webAddress;
	private String m_username;
	private String m_password;
	
	private final String COUNT_MESSAGE = "SELECT COUNT(*) FROM u644124777_message_box.message WHERE message.ID > ";
	private final String ALL_MESSAGE = "SELECT * FROM u644124777_message_box.message WHERE message.ID <= ";
	private final String TEXT_MESSAGE = "SELECT Text FROM u644124777_message_box.message WHERE message.ID = ";
	private final String RES_MESSAGE = "SELECT Media FROM u644124777_message_box.message WHERE message.ID = ";
	private final String LAST_ID_MESSAGE = "SELECT ID FROM u644124777_message_box.message WHERE ID = (SELECT MAX(ID) FROM u644124777_message_box.message)";
}
