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
import javazoom.jl.player.Player;

public class MessagePanel extends JPanel implements MessageNotifier {
	private static final long serialVersionUID = 4115950307058869469L;

	public static MessageChecker messageChecker;
	private volatile boolean readyForNext = false;
	private volatile boolean messageLoaded = false;
	
	private boolean checkingNetworkStatus = false;
	private String currentPanel = "NoMessages";
	private String lastPanel = "";
	private String messagePanel = "";
	
	private static final long CHECK_TIME = 10000;
	private static final long PLAY_TIME = 5000;
	
	private JPanel pnlMainDisplay;
	private JPanel pnlContent;
	private JButton btnDismiss;
	private JButton btnPastMessages;
	private JButton btnSettings;
	private JLabel lblMessageContentsNC;
	private JLabel lblMessageContentsC;
	private JLabel lblMessageImageC;
	private JScrollPane scrPastMessages;
	private JLabel lblFunIcon;
	
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
		lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
		sl_pnlNoMessages.putConstraint(SpringLayout.WEST, lblIcon, 0, SpringLayout.WEST, pnlNoMessages);
		sl_pnlNoMessages.putConstraint(SpringLayout.EAST, lblIcon, 0, SpringLayout.EAST, pnlNoMessages);
		lblIcon.setIcon(createScaledImageIcon("/img/empty1.png"));
		lblIcon.setForeground(Color.WHITE);
		pnlNoMessages.add(lblIcon);
		
		JLabel lblNoMessages = new JLabel("No new messages!");
		sl_pnlNoMessages.putConstraint(SpringLayout.NORTH, lblNoMessages, 24, SpringLayout.SOUTH, lblIcon);
		sl_pnlNoMessages.putConstraint(SpringLayout.WEST, lblNoMessages, 0, SpringLayout.WEST, lblIcon);
		sl_pnlNoMessages.putConstraint(SpringLayout.EAST, lblNoMessages, 0, SpringLayout.EAST, pnlNoMessages);
		lblNoMessages.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 32));
		lblNoMessages.setHorizontalAlignment(SwingConstants.CENTER);
		lblNoMessages.setForeground(Color.WHITE);
		pnlNoMessages.add(lblNoMessages);
		
		JLabel lblNoMessagesSubtitle = new JLabel("...but don't worry because Bryan is stil thinking of you.");
		sl_pnlNoMessages.putConstraint(SpringLayout.NORTH, lblNoMessagesSubtitle, 10, SpringLayout.SOUTH, lblNoMessages);
		sl_pnlNoMessages.putConstraint(SpringLayout.WEST, lblNoMessagesSubtitle, 0, SpringLayout.WEST, lblIcon);
		sl_pnlNoMessages.putConstraint(SpringLayout.EAST, lblNoMessagesSubtitle, 0, SpringLayout.EAST, lblIcon);
		lblNoMessagesSubtitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblNoMessagesSubtitle.setForeground(Color.WHITE);
		lblNoMessagesSubtitle.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 24));
		pnlNoMessages.add(lblNoMessagesSubtitle);
		
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
		
		JLabel lblNewMessage = new JLabel("You received a new message!");
		sl_pnlNewMessage.putConstraint(SpringLayout.NORTH, lblNewMessage, 289, SpringLayout.NORTH, lblMessageIcon);
		sl_pnlNewMessage.putConstraint(SpringLayout.WEST, lblNewMessage, 0, SpringLayout.WEST, pnlNewMessage);
		sl_pnlNewMessage.putConstraint(SpringLayout.EAST, lblNewMessage, 0, SpringLayout.EAST, pnlNewMessage);
		sl_pnlNewMessage.putConstraint(SpringLayout.SOUTH, lblMessageIcon, -6, SpringLayout.NORTH, lblNewMessage);
		lblNewMessage.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewMessage.setForeground(Color.WHITE);
		lblNewMessage.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 32));
		pnlNewMessage.add(lblNewMessage);
		
		JPanel pnlMessageDisplayC = new JPanel();
		pnlMessageDisplayC.setBackground(Color.BLACK);
		pnlMainDisplay.add(pnlMessageDisplayC, "CurrentMessageContent");
		SpringLayout sl_pnlMessageDisplayC = new SpringLayout();
		pnlMessageDisplayC.setLayout(sl_pnlMessageDisplayC);
		
		lblMessageImageC = new JLabel("");
		sl_pnlMessageDisplayC.putConstraint(SpringLayout.EAST, lblMessageImageC, 0, SpringLayout.EAST, pnlMessageDisplayC);
		lblMessageImageC.setHorizontalAlignment(SwingConstants.CENTER);
		lblMessageImageC.setForeground(Color.WHITE);
		lblMessageImageC.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 29));
		pnlMessageDisplayC.add(lblMessageImageC);
		
		lblMessageContentsC = new JLabel("");
		sl_pnlMessageDisplayC.putConstraint(SpringLayout.NORTH, lblMessageImageC, -181, SpringLayout.NORTH, lblMessageContentsC);
		sl_pnlMessageDisplayC.putConstraint(SpringLayout.WEST, lblMessageImageC, 0, SpringLayout.WEST, lblMessageContentsC);
		sl_pnlMessageDisplayC.putConstraint(SpringLayout.SOUTH, lblMessageImageC, -6, SpringLayout.NORTH, lblMessageContentsC);
		sl_pnlMessageDisplayC.putConstraint(SpringLayout.NORTH, lblMessageContentsC, 220, SpringLayout.NORTH, pnlMessageDisplayC);
		sl_pnlMessageDisplayC.putConstraint(SpringLayout.WEST, lblMessageContentsC, 0, SpringLayout.WEST, pnlMessageDisplayC);
		sl_pnlMessageDisplayC.putConstraint(SpringLayout.SOUTH, lblMessageContentsC, -80, SpringLayout.SOUTH, pnlMessageDisplayC);
		sl_pnlMessageDisplayC.putConstraint(SpringLayout.EAST, lblMessageContentsC, 0, SpringLayout.EAST, pnlMessageDisplayC);
		lblMessageContentsC.setHorizontalAlignment(SwingConstants.CENTER);
		lblMessageContentsC.setForeground(Color.WHITE);
		lblMessageContentsC.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 24));
		pnlMessageDisplayC.add(lblMessageContentsC);
		
		JPanel pnlMessageDisplayNC = new JPanel();
		pnlMessageDisplayNC.setBackground(Color.BLACK);
		pnlMainDisplay.add(pnlMessageDisplayNC, "CurrentMessageNoContent");
		SpringLayout sl_pnlMessageDisplayNC = new SpringLayout();
		pnlMessageDisplayNC.setLayout(sl_pnlMessageDisplayNC);
		
		lblFunIcon = new JLabel("");
		sl_pnlMessageDisplayNC.putConstraint(SpringLayout.EAST, lblFunIcon, -860, SpringLayout.EAST, pnlMessageDisplayNC);
		lblFunIcon.setVerticalAlignment(SwingConstants.TOP);
		lblFunIcon.setHorizontalAlignment(SwingConstants.CENTER);
		sl_pnlMessageDisplayNC.putConstraint(SpringLayout.NORTH, lblFunIcon, 10, SpringLayout.NORTH, pnlMessageDisplayNC);
		sl_pnlMessageDisplayNC.putConstraint(SpringLayout.WEST, lblFunIcon, 10, SpringLayout.WEST, pnlMessageDisplayNC);
		sl_pnlMessageDisplayNC.putConstraint(SpringLayout.SOUTH, lblFunIcon, -200, SpringLayout.SOUTH, pnlMessageDisplayNC);
		lblFunIcon.setForeground(Color.WHITE);
		pnlMessageDisplayNC.add(lblFunIcon);
		
		lblMessageContentsNC = new JLabel("");
		sl_pnlMessageDisplayNC.putConstraint(SpringLayout.NORTH, lblMessageContentsNC, 87, SpringLayout.NORTH, pnlMessageDisplayNC);
		sl_pnlMessageDisplayNC.putConstraint(SpringLayout.WEST, lblMessageContentsNC, 0, SpringLayout.WEST, pnlMessageDisplayNC);
		sl_pnlMessageDisplayNC.putConstraint(SpringLayout.SOUTH, lblMessageContentsNC, -49, SpringLayout.SOUTH, pnlMessageDisplayNC);
		sl_pnlMessageDisplayNC.putConstraint(SpringLayout.EAST, lblMessageContentsNC, 0, SpringLayout.EAST, pnlMessageDisplayNC);
		lblMessageContentsNC.setHorizontalAlignment(SwingConstants.CENTER);
		lblMessageContentsNC.setForeground(Color.WHITE);
		lblMessageContentsNC.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 24));
		pnlMessageDisplayNC.add(lblMessageContentsNC);
		
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
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.NORTH, lblIconNoInternet, 60, SpringLayout.NORTH, pnlNoNetworkConnection);
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.WEST, lblIconNoInternet, 0, SpringLayout.WEST, pnlNoNetworkConnection);
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.EAST, lblIconNoInternet, 0, SpringLayout.EAST, pnlNoNetworkConnection);
		lblIconNoInternet.setIcon(createScaledImageIcon("/img/empty1.png"));
		lblIconNoInternet.setHorizontalAlignment(SwingConstants.CENTER);
		lblIconNoInternet.setForeground(Color.WHITE);
		pnlNoNetworkConnection.add(lblIconNoInternet);
		
		JLabel lblCouldNotConnect = new JLabel("Could not connect to the message box service!");
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.NORTH, lblCouldNotConnect, 170, SpringLayout.NORTH, lblIconNoInternet);
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.SOUTH, lblIconNoInternet, -20, SpringLayout.NORTH, lblCouldNotConnect);
		lblCouldNotConnect.setHorizontalAlignment(SwingConstants.CENTER);
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.WEST, lblCouldNotConnect, 0, SpringLayout.WEST, pnlNoNetworkConnection);
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.EAST, lblCouldNotConnect, 0, SpringLayout.EAST, pnlNoNetworkConnection);
		lblCouldNotConnect.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 30));
		lblCouldNotConnect.setForeground(Color.WHITE);
		pnlNoNetworkConnection.add(lblCouldNotConnect);
		
		JLabel lblCouldNotConnectSub = new JLabel("Please check your connection and credentials...");
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.NORTH, lblCouldNotConnectSub, 10, SpringLayout.SOUTH, lblCouldNotConnect);
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.WEST, lblCouldNotConnectSub, 0, SpringLayout.WEST, pnlNoNetworkConnection);
		sl_pnlNoNetworkConnection.putConstraint(SpringLayout.EAST, lblCouldNotConnectSub, 0, SpringLayout.EAST, pnlNoNetworkConnection);
		lblCouldNotConnectSub.setHorizontalAlignment(SwingConstants.CENTER);
		lblCouldNotConnectSub.setForeground(Color.WHITE);
		lblCouldNotConnectSub.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 24));
		pnlNoNetworkConnection.add(lblCouldNotConnectSub);

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
	
	private static void playSound(String resource, boolean shouldBlock, final int playCount) {
		CountDownLatch latch = new CountDownLatch(1);
		
		// Create a thread.
		Thread playThread = new Thread(){
		    public void run(){
		    	int curPlayCount = playCount;
		    	if ( MainFrame.settings.shouldPlaySound() )
				{
		    		try {
				    	while ( curPlayCount > 0 ) {
				    		Player playMP3 = new Player(MessagePanel.class.getResourceAsStream(resource));
				    		playMP3.play();
				    		curPlayCount--;
				    	}	
		    		} catch (Exception e) {
		    			e.printStackTrace();
		    		}
				}
		    	else
		    	{
		    		try {
						Thread.sleep((long) PLAY_TIME);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
		    	}
		    	
		    	latch.countDown();
		    }
		};
		playThread.start();
		
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
		if (lastPanel.equals("CurrentMessageNoContent") || lastPanel.equals("CurrentMessageContent")) {
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
					playSound("/sound/message.mp3", true, 2);
				} while (!messageLoaded);
				
				// Finally switch to the new message routine.
				btnDismiss.setEnabled(true);
				btnPastMessages.setEnabled(true);
				btnSettings.setEnabled(true);
				
				// Switch to the new message thread.
				switchPanel(messagePanel);
		    }
		  };
		newMessageThread.start();
	}

	public void NotifyMessageDetails(String message, String resource) {
		if (resource.isEmpty()) {
			messagePanel = "CurrentMessageNoContent";
			lblMessageContentsNC.setText("<html><center>" + message + "</center></html>");
			
			// Pick a random number.
			String image = "deco";
			if(Math.random() < 0.5) {
				image += "1.gif";
			} else {
				image += "2.gif";
			}
			lblFunIcon.setIcon(createScaledImageIcon("/img/" + image));
		} else {
			messagePanel = "CurrentMessageContent";
			lblMessageContentsC.setText("<html><center>" + message + "</center></html>");
			
			Image image = null;
			try {
			    URL url = new URL(resource.trim());
			    image = ImageIO.read(url);
			    image = image.getScaledInstance(175, 175,  java.awt.Image.SCALE_DEFAULT);
			    
			    lblMessageImageC.setIcon(new ImageIcon(image));
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
