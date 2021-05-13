package ca.muscedere.window;

import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;

import java.awt.CardLayout;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import ca.muscedere.message.MessageChecker;
import ca.muscedere.message.MessageNotifier;
import ca.muscedere.settings.SettingsDialog;

import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class MessagePanel extends JPanel implements MessageNotifier {
	private static final long serialVersionUID = 4115950307058869469L;

	public static MessageChecker messageChecker;
	private boolean readyForNext = false;
	
	private boolean checkingNetworkStatus = false;
	private String currentPanel = "NoMessages";
	private String lastPanel = "";
	private static final long CHECK_TIME = 10000;
	
	private JPanel pnlMainDisplay;
	private JButton btnDismiss;
	private JButton btnPastMessages;
	private JLabel lblMessageContents;
	
	public MessagePanel() {
		setBackground(Color.BLACK);
		setLayout(new BorderLayout(0, 0));
		
		JPanel pnlController = new JPanel();
		add(pnlController, BorderLayout.SOUTH);
		
		btnDismiss = new JButton("Dismiss Current Message");
		btnDismiss.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// We are now ready again!
				btnDismiss.setEnabled(false);
				switchPanel("NoMessages");
				readyForNext = true;
			}
		});
		btnDismiss.setEnabled(false);
		pnlController.add(btnDismiss);
		
		btnPastMessages = new JButton("View Past Messages");
		btnPastMessages.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Check which panel we're on.
				if (currentPanel == "PastMessages") {
					btnPastMessages.setText("View Past Messages");
					readyForNext = true;
					switchPanel(lastPanel);
				} else {
					btnPastMessages.setText("View Current Messages");
					readyForNext = false;
					switchPanel("PastMessages");
				}				
			}
		});
		pnlController.add(btnPastMessages);
		
		JButton btnSettings = new JButton("Settings");
		btnSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Open the settings dialog.
				JComponent cmp = (JComponent) e.getSource();
				SettingsDialog dlg = new SettingsDialog( (JFrame) SwingUtilities.getWindowAncestor(cmp) );
				dlg.setVisible(true);
				
				// Check the result.
				if ( dlg.WasAccepted() )
				{
					boolean status = messageChecker.UpdateCredentials(
							MainFrame.settings.getUsername(), MainFrame.settings.getPassword());
					
					if (status && checkingNetworkStatus ) {
						reconnected();
					} else if ( !status && !checkingNetworkStatus ) {
						disconnected();
					}
				}
			}
		});
		pnlController.add(btnSettings);
		
		pnlMainDisplay = new JPanel();
		add(pnlMainDisplay, BorderLayout.CENTER);
		pnlMainDisplay.setLayout(new CardLayout(0, 0));
		
		JPanel pnlNoMessages = new JPanel();
		pnlNoMessages.setBackground(Color.BLACK);
		pnlMainDisplay.add(pnlNoMessages, "NoMessages");
		SpringLayout sl_pnlNoMessages = new SpringLayout();
		pnlNoMessages.setLayout(sl_pnlNoMessages);
		
		JLabel lblIcon = new JLabel("");
		lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
		sl_pnlNoMessages.putConstraint(SpringLayout.WEST, lblIcon, 0, SpringLayout.WEST, pnlNoMessages);
		sl_pnlNoMessages.putConstraint(SpringLayout.EAST, lblIcon, 0, SpringLayout.EAST, pnlNoMessages);
		lblIcon.setIcon(createScaledImageIcon("/img/empty.png"));
		lblIcon.setForeground(Color.WHITE);
		pnlNoMessages.add(lblIcon);
		
		JLabel lblNewLabel = new JLabel("No new messages!");
		sl_pnlNoMessages.putConstraint(SpringLayout.NORTH, lblIcon, -110, SpringLayout.NORTH, lblNewLabel);
		sl_pnlNoMessages.putConstraint(SpringLayout.SOUTH, lblIcon, 10, SpringLayout.NORTH, lblNewLabel);
		sl_pnlNoMessages.putConstraint(SpringLayout.NORTH, lblNewLabel, 191, SpringLayout.NORTH, pnlNoMessages);
		sl_pnlNoMessages.putConstraint(SpringLayout.WEST, lblNewLabel, 0, SpringLayout.WEST, pnlNoMessages);
		sl_pnlNoMessages.putConstraint(SpringLayout.SOUTH, lblNewLabel, -186, SpringLayout.SOUTH, pnlNoMessages);
		sl_pnlNoMessages.putConstraint(SpringLayout.EAST, lblNewLabel, 0, SpringLayout.EAST, pnlNoMessages);
		lblNewLabel.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 32));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setForeground(Color.WHITE);
		pnlNoMessages.add(lblNewLabel);
		
		JPanel pnlNewMessage = new JPanel();
		pnlNewMessage.setBackground(Color.BLACK);
		pnlMainDisplay.add(pnlNewMessage, "NewMessages");
		SpringLayout sl_pnlNewMessage = new SpringLayout();
		pnlNewMessage.setLayout(sl_pnlNewMessage);
		
		JLabel lblMessageIcon = new JLabel("");
		sl_pnlNewMessage.putConstraint(SpringLayout.NORTH, lblMessageIcon, 84, SpringLayout.NORTH, pnlNewMessage);
		sl_pnlNewMessage.putConstraint(SpringLayout.WEST, lblMessageIcon, -440, SpringLayout.EAST, pnlNewMessage);
		sl_pnlNewMessage.putConstraint(SpringLayout.EAST, lblMessageIcon, -320, SpringLayout.EAST, pnlNewMessage);
		lblMessageIcon.setIcon(createScaledImageIcon("/img/new.gif"));
		lblMessageIcon.setHorizontalAlignment(SwingConstants.CENTER);
		lblMessageIcon.setForeground(Color.WHITE);
		pnlNewMessage.add(lblMessageIcon);
		
		JLabel lblNewMessage = new JLabel("You've received a new message!");
		sl_pnlNewMessage.putConstraint(SpringLayout.SOUTH, lblMessageIcon, -15, SpringLayout.NORTH, lblNewMessage);
		sl_pnlNewMessage.putConstraint(SpringLayout.NORTH, lblNewMessage, 210, SpringLayout.NORTH, pnlNewMessage);
		sl_pnlNewMessage.putConstraint(SpringLayout.WEST, lblNewMessage, 0, SpringLayout.WEST, pnlNewMessage);
		sl_pnlNewMessage.putConstraint(SpringLayout.SOUTH, lblNewMessage, 245, SpringLayout.NORTH, pnlNewMessage);
		sl_pnlNewMessage.putConstraint(SpringLayout.EAST, lblNewMessage, 0, SpringLayout.EAST, pnlNewMessage);
		lblNewMessage.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewMessage.setForeground(Color.WHITE);
		lblNewMessage.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 32));
		pnlNewMessage.add(lblNewMessage);
		
		JPanel pnlMessageDisplay = new JPanel();
		pnlMessageDisplay.setBackground(Color.BLACK);
		pnlMainDisplay.add(pnlMessageDisplay, "CurrentMessage");
		SpringLayout sl_pnlMessageDisplay = new SpringLayout();
		pnlMessageDisplay.setLayout(sl_pnlMessageDisplay);
		
		lblMessageContents = new JLabel("");
		lblMessageContents.setHorizontalAlignment(SwingConstants.CENTER);
		sl_pnlMessageDisplay.putConstraint(SpringLayout.NORTH, lblMessageContents, 150, SpringLayout.NORTH, pnlMessageDisplay);
		sl_pnlMessageDisplay.putConstraint(SpringLayout.WEST, lblMessageContents, 0, SpringLayout.WEST, pnlMessageDisplay);
		sl_pnlMessageDisplay.putConstraint(SpringLayout.SOUTH, lblMessageContents, -150, SpringLayout.SOUTH, pnlMessageDisplay);
		sl_pnlMessageDisplay.putConstraint(SpringLayout.EAST, lblMessageContents, 0, SpringLayout.EAST, pnlMessageDisplay);
		lblMessageContents.setForeground(Color.WHITE);
		lblMessageContents.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 29));
		pnlMessageDisplay.add(lblMessageContents);
		
		JPanel pnlPastMessages = new JPanel();
		pnlPastMessages.setBackground(Color.BLACK);
		pnlMainDisplay.add(pnlPastMessages, "PastMessages");
		pnlPastMessages.setLayout(new BorderLayout(0, 0));
		
		JPanel pnlPastTitle = new JPanel();
		pnlPastTitle.setBackground(Color.BLACK);
		pnlPastMessages.add(pnlPastTitle, BorderLayout.NORTH);
		pnlPastTitle.setLayout(new BorderLayout(0, 0));
		
		JLabel lblPastMessages = new JLabel("Your Past Messages");
		lblPastMessages.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 50));
		lblPastMessages.setBackground(Color.BLACK);
		lblPastMessages.setForeground(Color.WHITE);
		lblPastMessages.setHorizontalAlignment(SwingConstants.CENTER);
		pnlPastTitle.add(lblPastMessages, BorderLayout.NORTH);
		
		JScrollPane scrPastMessages = new JScrollPane();
		scrPastMessages.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		pnlPastMessages.add(scrPastMessages, BorderLayout.CENTER);
		
		JPanel pnlNoNetworkConnection = new JPanel();
		pnlNoNetworkConnection.setBackground(Color.BLACK);
		pnlMainDisplay.add(pnlNoNetworkConnection, "NoNetworkConnection");
		SpringLayout sl_pnlNoNetworkConnection = new SpringLayout();
		pnlNoNetworkConnection.setLayout(sl_pnlNoNetworkConnection);
		
		JLabel lblIconNoInternet = new JLabel("");
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.NORTH, lblIconNoInternet, 57, SpringLayout.NORTH, pnlNoNetworkConnection);
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.EAST, lblIconNoInternet, 0, SpringLayout.EAST, pnlNoNetworkConnection);
		lblIconNoInternet.setIcon(createScaledImageIcon("/img/empty.png"));
		lblIconNoInternet.setHorizontalAlignment(SwingConstants.CENTER);
		lblIconNoInternet.setForeground(Color.WHITE);
		pnlNoNetworkConnection.add(lblIconNoInternet);
		
		JLabel lblNoNetworkTitle = new JLabel("Could not connect to the message box service!");
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.WEST, lblIconNoInternet, 0, SpringLayout.WEST, lblNoNetworkTitle);
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.SOUTH, lblIconNoInternet, -6, SpringLayout.NORTH, lblNoNetworkTitle);
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.SOUTH, lblNoNetworkTitle, -222, SpringLayout.SOUTH, pnlNoNetworkConnection);
		lblNoNetworkTitle.setHorizontalAlignment(SwingConstants.CENTER);
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.NORTH, lblNoNetworkTitle, 183, SpringLayout.NORTH, pnlNoNetworkConnection);
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.WEST, lblNoNetworkTitle, 0, SpringLayout.WEST, pnlNoNetworkConnection);
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.EAST, lblNoNetworkTitle, 0, SpringLayout.EAST, pnlNoNetworkConnection);
		lblNoNetworkTitle.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 34));
		lblNoNetworkTitle.setForeground(Color.WHITE);
		pnlNoNetworkConnection.add(lblNoNetworkTitle);
		
		JLabel lblNoNetworkSubtitle = new JLabel("Check your internet connection and credentials...");
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.WEST, lblNoNetworkSubtitle, 0, SpringLayout.WEST, pnlNoNetworkConnection);
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.EAST, lblNoNetworkSubtitle, 0, SpringLayout.EAST, pnlNoNetworkConnection);
		lblNoNetworkSubtitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblNoNetworkSubtitle.setForeground(Color.WHITE);
		lblNoNetworkSubtitle.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.ITALIC, 24));
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.NORTH, lblNoNetworkSubtitle, 22, SpringLayout.SOUTH, lblNoNetworkTitle);
		pnlNoNetworkConnection.add(lblNoNetworkSubtitle);
		
		// Create the message checker.
		messageChecker = new MessageChecker( MainFrame.settings.getMessageCheckTime(), 
				MainFrame.settings.getUsername(), MainFrame.settings.getPassword(), MainFrame.SAVE_LOCATION, this );
		if ( !messageChecker.RunChecker() ) {
			disconnected();
		} else {
			// If we get here, we're ready for messages.
			readyForNext = true;	
		}
	}
	
	private static ImageIcon createScaledImageIcon(String resource) {
		ImageIcon imageIcon = new ImageIcon(MessagePanel.class.getResource(resource));
		Image image = imageIcon.getImage(); // transform it 
		Image newimg = image.getScaledInstance(120, 120,  java.awt.Image.SCALE_SMOOTH); 
		return new ImageIcon(newimg);
	}
	
	private void disconnected() {
		// We aren't ready for messages.
		readyForNext = false;
				
		// Start by disabling the bottom buttons.
		btnPastMessages.setEnabled(false);
		btnDismiss.setEnabled(false);
		
		// Switch to the no connection panel.
		switchPanel("NoNetworkConnection");
		
		// Create a thread that keeps polling for reconnection.
		checkingNetworkStatus = true;
		Thread reconnectionThread = new Thread(){
		    public void run(){
		    	while (checkingNetworkStatus)
		    	{
		    		try {
						Thread.sleep(CHECK_TIME);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
		    		
		    		if (messageChecker.RunChecker()) {
		    			reconnected();
		    		}
		    	}
		    	
		    }
		  };
		reconnectionThread.start();
	}
	
	private void reconnected() {
		// Start by re-enabling the buttons.
		btnPastMessages.setEnabled(true);
		btnDismiss.setEnabled(true);
		
		// Switch back to the no message panel.
		switchPanel(lastPanel);
		
		// Reset our booleans.
		checkingNetworkStatus = false;
		readyForNext = true;
	}
	
	private void switchPanel(String panelName)
	{
		CardLayout layout = (CardLayout) pnlMainDisplay.getLayout();
		layout.show(pnlMainDisplay, panelName);
		lastPanel = currentPanel;
		currentPanel = panelName;
	}
	
	public void NotifyNoNetwork() {
		disconnected();
	}
	
	public void NotifyNewMessage() {
		readyForNext = false;
		
		// On a new message, run the new message routine.
		System.out.println("NEW MESSAGE");
		
		// Finally switch to the new message routine.
		btnDismiss.setEnabled(true);
	}

	public void NotifyMessageDetails(String message, String resource) {
		// Apply the new message.
		// TODO: Deal with the resource.
		lblMessageContents.setText(message);
	}

	public boolean ReadyForNext() {
		return readyForNext;
	}
}
