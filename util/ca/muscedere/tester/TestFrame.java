package ca.muscedere.tester;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import ca.muscedere.message.MessageSender;
import ca.muscedere.settings.SettingsBundle;

public class TestFrame extends JFrame {
	private static final long serialVersionUID = 4381541712304550794L;
	private JPanel pnlMain;
	private JTextField txtMessageText;
	private JTextField txtMessagePreview;
	private JLabel lblResourcePreview;
	private JLabel lblMessagePreview;
	
	private static SettingsBundle settings;
	private static MessageSender messageSender;
	
	public static final String WEB_ADDRESS = "sql174.main-hosting.eu";
	public static final String SAVE_LOCATION = TestFrame.class.getProtectionDomain()
			.getCodeSource().getLocation().getPath();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		// Create the settings bundle.
		settings = new SettingsBundle( SAVE_LOCATION );
		
		// Create the message sender.
		messageSender = new MessageSender( WEB_ADDRESS, settings.getUsername(), settings.getPassword() );
		messageSender.Connect();
		
		// Last, create the GUI.
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TestFrame frame = new TestFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public TestFrame() {
		setResizable(false);
		setTitle("Alex's Message Box - Message Tester");
		setIconImage(Toolkit.getDefaultToolkit().getImage(TestFrame.class.getResource("/img/love.png")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 557, 380);
		pnlMain = new JPanel();
		pnlMain.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(pnlMain);
		pnlMain.setLayout(null);
		
		JLabel lblMessageText = new JLabel("Message Text:");
		lblMessageText.setBounds(12, 13, 112, 16);
		pnlMain.add(lblMessageText);
		
		txtMessageText = new JTextField();
		txtMessageText.setBounds(139, 10, 393, 22);
		pnlMain.add(txtMessageText);
		txtMessageText.setColumns(10);
		
		JLabel lblMessageResource = new JLabel("Message Resource:");
		lblMessageResource.setBounds(12, 42, 112, 16);
		pnlMain.add(lblMessageResource);
		
		txtMessagePreview = new JTextField();
		txtMessagePreview.setColumns(10);
		txtMessagePreview.setBounds(139, 39, 393, 22);
		pnlMain.add(txtMessagePreview);
		
		JPanel pnlPreview = new JPanel();
		pnlPreview.setBorder(new TitledBorder(null, "Message Preview", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnlPreview.setBounds(12, 110, 520, 184);
		pnlMain.add(pnlPreview);
		SpringLayout sl_pnlPreview = new SpringLayout();
		pnlPreview.setLayout(sl_pnlPreview);
		
		lblResourcePreview = new JLabel("");
		lblResourcePreview.setHorizontalAlignment(SwingConstants.CENTER);
		sl_pnlPreview.putConstraint(SpringLayout.WEST, lblResourcePreview, 10, SpringLayout.WEST, pnlPreview);
		sl_pnlPreview.putConstraint(SpringLayout.EAST, lblResourcePreview, -10, SpringLayout.EAST, pnlPreview);
		pnlPreview.add(lblResourcePreview);
		
		lblMessagePreview = new JLabel("");
		sl_pnlPreview.putConstraint(SpringLayout.NORTH, lblResourcePreview, -66, SpringLayout.NORTH, lblMessagePreview);
		sl_pnlPreview.putConstraint(SpringLayout.SOUTH, lblResourcePreview, -20, SpringLayout.NORTH, lblMessagePreview);
		sl_pnlPreview.putConstraint(SpringLayout.EAST, lblMessagePreview, -10, SpringLayout.EAST, pnlPreview);
		lblMessagePreview.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 18));
		lblMessagePreview.setHorizontalAlignment(SwingConstants.CENTER);
		sl_pnlPreview.putConstraint(SpringLayout.NORTH, lblMessagePreview, -82, SpringLayout.SOUTH, pnlPreview);
		sl_pnlPreview.putConstraint(SpringLayout.WEST, lblMessagePreview, 10, SpringLayout.WEST, pnlPreview);
		sl_pnlPreview.putConstraint(SpringLayout.SOUTH, lblMessagePreview, -28, SpringLayout.SOUTH, pnlPreview);
		pnlPreview.add(lblMessagePreview);
		
		JButton btnPreview = new JButton("Preview Message...");
		btnPreview.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Check for validity.
				if (txtMessageText.getText().trim().equals("") && txtMessagePreview.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(pnlMain, "Cannot preview a blank message!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				// Try to resolve the image.
				if (!txtMessagePreview.getText().trim().equals("")) {
					Image image = null;
					try {
					    URL url = new URL(txtMessagePreview.getText().trim());
					    image = ImageIO.read(url);
					    image = image.getScaledInstance(120, 120,  java.awt.Image.SCALE_SMOOTH);
					    
					    lblResourcePreview.setIcon(new ImageIcon(image));
					    lblResourcePreview.setVisible(true);
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(pnlMain, "Invalid URL given!", 
								"Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				
				// Next, populate the text where possible.
				if (!txtMessageText.getText().trim().equals("")) {
					lblMessagePreview.setText(txtMessageText.getText());
				}
			}
		});
		btnPreview.setBounds(173, 74, 192, 25);
		pnlMain.add(btnPreview);
		
		JButton btnClear = new JButton("Clear");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtMessageText.setText("");
				txtMessagePreview.setText("");
				lblMessagePreview.setText("");
				lblResourcePreview.setVisible(false);
			}
		});
		btnClear.setBounds(12, 307, 151, 25);
		pnlMain.add(btnClear);
		
		JButton btnSend = new JButton("Send Message!");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Check for validity.
				if (txtMessageText.getText().trim().equals("") && txtMessagePreview.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(pnlMain, "Cannot preview a blank message!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (!txtMessagePreview.getText().trim().equals("")) {
					try {
					    URL url = new URL(txtMessagePreview.getText().trim());
					    ImageIO.read(url);
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(pnlMain, "Invalid URL given!", 
								"Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				
				// Next, attempt to write to the database.
				boolean status = messageSender.WriteNewMessage(txtMessageText.getText().trim(), 
						txtMessagePreview.getText().trim());
				if ( status ) {
					JOptionPane.showMessageDialog(pnlMain, "Message sent successfully!", "Success!", JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(pnlMain, "Could not send the message to the database!", "Error", JOptionPane.ERROR_MESSAGE);
				}
				
				// Clear the fields.
				txtMessageText.setText("");
				txtMessagePreview.setText("");
				lblMessagePreview.setText("");
				lblResourcePreview.setVisible(false);
			}
		});
		btnSend.setBounds(381, 307, 151, 25);
		pnlMain.add(btnSend);
	}
}
