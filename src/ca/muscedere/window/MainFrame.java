package ca.muscedere.window;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;

import ca.muscedere.settings.SettingsBundle;
import javafx.embed.swing.JFXPanel;

import java.awt.Toolkit;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = -3974286338889827329L;
	
	@SuppressWarnings("unused")
	private static final JFXPanel fxPanel = new JFXPanel();
	
	public static final String SAVE_LOCATION = MainFrame.class.getProtectionDomain().
			getCodeSource().getLocation().getPath();
	
	public static SettingsBundle settings;
	private JPanel contentPane;

	@SuppressWarnings("restriction")
	public static void main(String[] args) {
		com.sun.javafx.application.PlatformImpl.startup(()->{});
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
					frame.setUndecorated(true);
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
	public MainFrame() {
		setTitle("Alex's Message Box");
		setIconImage(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/love.png")));
		
		// Create the settings system.
		settings = new SettingsBundle( SAVE_LOCATION );
		
		// Initialize the message frame.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new MessagePanel();
		setContentPane(contentPane);
	}
}
