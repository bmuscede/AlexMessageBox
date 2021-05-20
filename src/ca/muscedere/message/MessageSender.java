package ca.muscedere.message;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MessageSender {
	public MessageSender(String websiteAddress, String username, String encryptedPassword) {
		m_webAddress = websiteAddress;
		m_username = username;
		m_password = encryptedPassword;
	}
	
	private boolean Connect() {
		if ( IsConnected() ) return true;
		
		// Try to create a connection.
		try {
			m_dbConnection = DriverManager.getConnection("jdbc:mysql://" + m_webAddress, m_username, m_password);
		} catch (SQLException e) {
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

	public boolean WriteNewMessage(String messageText, String resourceURL) {
		// Check if we're connected.
		if ( !Connect() ) return false;
		
		// Create a statement.
		Statement stmt = null;
		try {
			String sql = SQL_STATEMENT_1 + "\'" + messageText + "\',\'" + resourceURL + "\'" + SQL_STATEMENT_2;
			stmt = m_dbConnection.createStatement();
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if ( stmt != null ) stmt.close();
			} catch (Exception e) {}
			Disconnect();
		}
		return true;
	}
	
	private final static String SQL_STATEMENT_1 = "INSERT INTO u644124777_message_box.message (Text, Media) VALUES (";
	private final static String SQL_STATEMENT_2 = ");";
	
	private Connection m_dbConnection = null;
	
	private String m_webAddress;
	private String m_username;
	private String m_password;
}
