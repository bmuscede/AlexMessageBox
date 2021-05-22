package ca.muscedere.window;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import ca.muscedere.message.MessageBundle;
import ca.muscedere.message.MessageChecker;
import ca.muscedere.message.MessageNotifier;
import ca.muscedere.settings.SettingsDialog;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class MessagePanel extends JPanel implements MessageNotifier {
	private static final long serialVersionUID = 4115950307058869469L;

	public static MessageChecker messageChecker;
	private volatile boolean readyForNext = false;
	private volatile boolean messageLoaded = false;
	
	private boolean checkingNetworkStatus = false;
	private String currentPanel = "NoMessages";
	private String lastPanel = "";
	private static final long CHECK_TIME = 10000;
	
	private JPanel pnlMainDisplay;
	private JPanel pnlContent;
	private JButton btnDismiss;
	private JButton btnPastMessages;
	private JButton btnSettings;
	private JLabel lblMessageContents;
	private JScrollPane scrPastMessages;
	
	public MessagePanel() {
		Dimension size = Toolkit. getDefaultToolkit(). getScreenSize();
		
		setBackground(Color.BLACK);
		setLayout(new BorderLayout(0, 0));
		
		JPanel pnlController = new JPanel();
		pnlController.setBackground(Color.BLACK);
		add(pnlController, BorderLayout.SOUTH);
		pnlController.setLayout(new BorderLayout(0, 0));
		
		JPanel pnlNorth = new JPanel();
		pnlNorth.setBackground(Color.BLACK);
		pnlController.add(pnlNorth, BorderLayout.NORTH);
		
		Component northStrut = Box.createVerticalStrut(size.height / 50);
		pnlNorth.add(northStrut);
		
		JPanel pnlEast = new JPanel();
		pnlEast.setBackground(Color.BLACK);
		pnlController.add(pnlEast, BorderLayout.EAST);
		
		Component eastStrut = Box.createHorizontalStrut(size.width / 15);
		pnlEast.add(eastStrut);
		
		JPanel pnlWest = new JPanel();
		pnlWest.setBackground(Color.BLACK);
		pnlController.add(pnlWest, BorderLayout.WEST);
		
		Component westStrut = Box.createHorizontalStrut(size.width / 15);
		pnlWest.add(westStrut);
		
		JPanel pnlSouth = new JPanel();
		pnlSouth.setBackground(Color.BLACK);
		pnlController.add(pnlSouth, BorderLayout.SOUTH);
		
		Component southStrut = Box.createVerticalStrut(size.height / 30);
		pnlSouth.add(southStrut);
		
		JPanel pnlButtons = new RoundedPanel();
		pnlButtons.setBackground(UIManager.getColor("MenuItem.selectionBackground"));
		pnlController.add(pnlButtons);
		
		btnDismiss = new JButton("Dismiss Current Message");
		btnDismiss.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 16));
		pnlButtons.add(btnDismiss);
		btnDismiss.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// We are now ready again!
				btnDismiss.setEnabled(false);
				switchPanel("NoMessages");
				readyForNext = true;
			}
		});
		btnDismiss.setEnabled(false);
		
		btnPastMessages = new JButton("View Past Messages");
		btnPastMessages.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 16));
		pnlButtons.add(btnPastMessages);
		
		btnSettings = new JButton("Settings");
		btnSettings.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 16));
		pnlButtons.add(btnSettings);
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
		btnPastMessages.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Check which panel we're on.
				if (currentPanel == "PastMessages") {
					pnlContent.removeAll();
					pnlContent.revalidate();
					
					btnPastMessages.setText("View Past Messages");
					readyForNext = true;
					switchPanel(lastPanel);
				} else {
					btnPastMessages.setText("View Current Messages");
					
					// Calculate the number of rows to fit the proper layout.
					int minNumPanels = scrPastMessages.getViewport().getSize().height / PastPanel.MAX_HEIGHT;
					
					// Fetch all messages.
					Vector<MessageBundle> bundles = messageChecker.getAllMessages();
					pnlContent.setLayout(new GridLayout(
							(bundles.size() > minNumPanels) ? bundles.size() : minNumPanels, 
							1, 0, 0));
					pnlContent.setPreferredSize(new Dimension(0, PastPanel.MAX_HEIGHT * bundles.size()));
					for ( MessageBundle bundle : bundles ) {
						JPanel currentPanel = new PastPanel(bundle.getMessageDate(), bundle.getMessage(), bundle.getMessageURL());
						pnlContent.add(currentPanel);
					}
					readyForNext = false;
					pnlContent.repaint();
					switchPanel("PastMessages");
				}				
			}
		});
		
		pnlMainDisplay = new JPanel();
		add(pnlMainDisplay, BorderLayout.CENTER);
		pnlMainDisplay.setLayout(new CardLayout(0, 0));
		
		JPanel pnlNoMessages = new JPanel();
		pnlNoMessages.setBackground(Color.BLACK);
		pnlMainDisplay.add(pnlNoMessages, "NoMessages");
		SpringLayout sl_pnlNoMessages = new SpringLayout();
		pnlNoMessages.setLayout(sl_pnlNoMessages);
		
		JLabel lblIcon = new JLabel("");
		sl_pnlNoMessages.putConstraint(SpringLayout.NORTH, lblIcon, 81, SpringLayout.NORTH, pnlNoMessages);
		sl_pnlNoMessages.putConstraint(SpringLayout.SOUTH, lblIcon, -242, SpringLayout.SOUTH, pnlNoMessages);
		lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
		sl_pnlNoMessages.putConstraint(SpringLayout.WEST, lblIcon, 0, SpringLayout.WEST, pnlNoMessages);
		sl_pnlNoMessages.putConstraint(SpringLayout.EAST, lblIcon, 0, SpringLayout.EAST, pnlNoMessages);
		lblIcon.setIcon(createScaledImageIcon("/img/empty1.png"));
		lblIcon.setForeground(Color.WHITE);
		pnlNoMessages.add(lblIcon);
		
		JLabel lblNewLabel = new JLabel("No new messages!");
		sl_pnlNoMessages.putConstraint(SpringLayout.NORTH, lblNewLabel, 217, SpringLayout.NORTH, pnlNoMessages);
		sl_pnlNoMessages.putConstraint(SpringLayout.WEST, lblNewLabel, 0, SpringLayout.WEST, pnlNoMessages);
		sl_pnlNoMessages.putConstraint(SpringLayout.SOUTH, lblNewLabel, -160, SpringLayout.SOUTH, pnlNoMessages);
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
		sl_pnlNewMessage.putConstraint(SpringLayout.NORTH, lblMessageIcon, 32, SpringLayout.NORTH, pnlNewMessage);
		sl_pnlNewMessage.putConstraint(SpringLayout.WEST, lblMessageIcon, 0, SpringLayout.WEST, pnlNewMessage);
		sl_pnlNewMessage.putConstraint(SpringLayout.EAST, lblMessageIcon, 0, SpringLayout.EAST, pnlNewMessage);
		lblMessageIcon.setIcon(createScaledImageIcon("/img/new1.gif", 250, 250));
		lblMessageIcon.setHorizontalAlignment(SwingConstants.CENTER);
		lblMessageIcon.setForeground(Color.WHITE);
		pnlNewMessage.add(lblMessageIcon);
		
		JLabel lblNewMessage = new JLabel("You've received a new message!");
		sl_pnlNewMessage.putConstraint(SpringLayout.WEST, lblNewMessage, 0, SpringLayout.WEST, pnlNewMessage);
		sl_pnlNewMessage.putConstraint(SpringLayout.EAST, lblNewMessage, 0, SpringLayout.EAST, pnlNewMessage);
		sl_pnlNewMessage.putConstraint(SpringLayout.SOUTH, lblMessageIcon, -6, SpringLayout.NORTH, lblNewMessage);
		sl_pnlNewMessage.putConstraint(SpringLayout.NORTH, lblNewMessage, 289, SpringLayout.NORTH, pnlNewMessage);
		sl_pnlNewMessage.putConstraint(SpringLayout.SOUTH, lblNewMessage, -10, SpringLayout.SOUTH, pnlNewMessage);
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
		
		pnlContent = new JPanel();
		pnlContent.setLayout(new GridLayout(0, 1, 0, 0));
		
		scrPastMessages = new JScrollPane(pnlContent);
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
		lblIconNoInternet.setIcon(createScaledImageIcon("/img/empty1.png"));
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
		return createScaledImageIcon(resource, 120, 120);
	}
	
	private static ImageIcon createScaledImageIcon(String resource, int width, int height) {
		ImageIcon imageIcon = new ImageIcon(MessagePanel.class.getResource(resource));
		Image image = imageIcon.getImage(); // transform it 
		Image newimg = image.getScaledInstance(width, height, java.awt.Image.SCALE_DEFAULT); 
		return new ImageIcon(newimg);
	}
	
	private static void playSound(String resource, boolean shouldBlock, int playCount) {
		CountDownLatch latch = new CountDownLatch(1);
		
		MediaPlayer mediaPlayer = new MediaPlayer(new Media(resource));
		mediaPlayer.setOnReady(new Runnable() {
			@Override
		    public void run() {
				double millis = (mediaPlayer.getTotalDuration().toMillis() * playCount) + 3000;

				if ( MainFrame.settings.shouldPlaySound() )
				{
					AudioClip media = new AudioClip(resource);
					media.setCycleCount(playCount);
					media.play();
				}
				
				try {
					Thread.sleep((long) millis);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				latch.countDown();
		}});
		
		if (shouldBlock)
		{
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void turnOnScreen() {
		String OS = System.getProperty("os.name");
		if ( OS.startsWith("Windows")) {
			// On Windows, jiggle the mouse as a way to turn the screen on.
			Robot robot;
			try {
				robot = new Robot();
				robot.mouseMove(0,0);
			} catch (AWTException e) {
				e.printStackTrace();
			}
		} else {
			// Run the xset command.
			Runtime rt = Runtime.getRuntime();
			try {
				rt.exec("xset -display ${DISPLAY} dpms force on");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
		if (lastPanel.equals("CurrentMessage")) {
			btnDismiss.setEnabled(true);
		} else {
			btnDismiss.setEnabled(false);
		}
		
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
		messageLoaded = false;
		
		// On a new message, run the new message routine.
		Thread newMessageThread = new Thread(){
		    public void run(){
				// Disable the buttons below.
				btnDismiss.setEnabled(false);
				btnPastMessages.setEnabled(false);
				btnSettings.setEnabled(false);
				
				// Switch to the new message thread.
				switchPanel("NewMessages");
				
				// Turn on the screen.
				if ( MainFrame.settings.shouldRunScreen() )
				{
					turnOnScreen();
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				// Play the new message sound and block on this.
				do {
					playSound(MessagePanel.class.getResource("/sound/message.mp3").toString(), true, 2);
				} while (!messageLoaded);
				
				// Finally switch to the new message routine.
				btnDismiss.setEnabled(true);
				btnPastMessages.setEnabled(true);
				btnSettings.setEnabled(true);
				
				// Switch to the new message thread.
				switchPanel("CurrentMessage");
		    }
		  };
		newMessageThread.start();
	}

	public void NotifyMessageDetails(String message, String resource) {
		// Apply the new message.
		lblMessageContents.setText(message);
		if (!resource.isEmpty())
		{
			Image image = null;
			try {
			    URL url = new URL(resource.trim());
			    image = ImageIO.read(url);
			    image = image.getScaledInstance(120, 120,  java.awt.Image.SCALE_DEFAULT);
			    
			    //lblResourcePreview.setIcon(new ImageIcon(image));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// Finally, the message is ready.
		messageLoaded = true;
	}

	public boolean ReadyForNext() {
		return readyForNext;
	}
}
