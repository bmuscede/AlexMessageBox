package ca.muscedere.window;

import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.SpringLayout;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.Image;
import java.net.URL;
import java.awt.Dimension;

public class PastPanel extends JPanel {
	private static final long serialVersionUID = 5577478235527755829L;
	public static final int MAX_HEIGHT = 100;
	
	public PastPanel(String messageDate, String messageText, String messageResource) {
		setMinimumSize(new Dimension(600, MAX_HEIGHT));
		setMaximumSize(new Dimension(600, MAX_HEIGHT));
		setPreferredSize(new Dimension(600, MAX_HEIGHT));
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		JLabel lblPastMessage = new JLabel(messageText);
		springLayout.putConstraint(SpringLayout.NORTH, lblPastMessage, 22, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, lblPastMessage, -10, SpringLayout.EAST, this);
		lblPastMessage.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 17));
		add(lblPastMessage);
		
		JSeparator separator = new JSeparator();
		springLayout.putConstraint(SpringLayout.WEST, lblPastMessage, 8, SpringLayout.EAST, separator);
		springLayout.putConstraint(SpringLayout.SOUTH, lblPastMessage, 31, SpringLayout.NORTH, separator);
		springLayout.putConstraint(SpringLayout.NORTH, separator, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, separator, 112, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, separator, -6, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, separator, 122, SpringLayout.WEST, this);
		separator.setOrientation(SwingConstants.VERTICAL);
		add(separator);
		
		JLabel lblDate = new JLabel("Message Sent On: " + messageDate);
		springLayout.putConstraint(SpringLayout.NORTH, lblDate, 20, SpringLayout.SOUTH, lblPastMessage);
		springLayout.putConstraint(SpringLayout.WEST, lblDate, 0, SpringLayout.WEST, lblPastMessage);
		springLayout.putConstraint(SpringLayout.SOUTH, lblDate, -10, SpringLayout.SOUTH, separator);
		springLayout.putConstraint(SpringLayout.EAST, lblDate, -12, SpringLayout.EAST, this);
		lblDate.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 17));
		add(lblDate);
		
		// TODO: Load Message Resource!
		JLabel lblMessageResource = new JLabel("");
		lblMessageResource.setHorizontalAlignment(SwingConstants.CENTER);
		if ( messageResource.isEmpty() ) {
			lblMessageResource.setIcon(createScaledImageIcon("/img/no-photo.png"));
		} else {
			lblMessageResource.setIcon(createScaledImageIcon("/img/loading.gif"));	
		}
		
		springLayout.putConstraint(SpringLayout.NORTH, lblMessageResource, 0, SpringLayout.NORTH, separator);
		springLayout.putConstraint(SpringLayout.WEST, lblMessageResource, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, lblMessageResource, 0, SpringLayout.SOUTH, separator);
		springLayout.putConstraint(SpringLayout.EAST, lblMessageResource, -6, SpringLayout.WEST, separator);
		lblMessageResource.setFont(new Font("Tw Cen MT Condensed Extra Bold", Font.PLAIN, 17));
		add(lblMessageResource);
		
		// Last, if we have an image, load that in a separate thread.
		if (!messageResource.isEmpty()) {
			Thread imageLoadThread = new Thread(){
			    public void run(){
			    	Image image = null;
					try {
					    URL url = new URL(messageResource.trim());
					    image = ImageIO.read(url);
					    image = image.getScaledInstance(70, 70, java.awt.Image.SCALE_DEFAULT);
					    
					    lblMessageResource.setIcon(new ImageIcon(image));
					} catch (Exception e) {
						e.printStackTrace();
						lblMessageResource.setIcon(createScaledImageIcon("/img/no-photo.png"));
					}
			    }
			  };
			imageLoadThread.start();
		}
	}
	
	private static ImageIcon createScaledImageIcon(String resource) {
		ImageIcon imageIcon = new ImageIcon(MessagePanel.class.getResource(resource));
		Image image = imageIcon.getImage(); // transform it 
		Image newimg = image.getScaledInstance(70, 70,  java.awt.Image.SCALE_DEFAULT); 
		return new ImageIcon(newimg);
	}
	
}
