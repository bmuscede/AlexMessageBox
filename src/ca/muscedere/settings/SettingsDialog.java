package ca.muscedere.settings;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import ca.muscedere.window.MainFrame;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;

import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class SettingsDialog extends JDialog {
	private static final long serialVersionUID = 5541604593216754944L;
	
	private final JPanel contentPanel = new JPanel();
	private JPasswordField txtPassword;
	private JTextField txtUsername;
	private JSpinner spnCheckInterval;
	private JCheckBox chkEnableSound;
	private JCheckBox chkScreen;
	private boolean isAccepted;
	
	public SettingsDialog(JFrame parent) {
		super(parent, "Settings", Dialog.ModalityType.APPLICATION_MODAL);
		
		setResizable(false);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JPanel pnlGeneral = new JPanel();
		pnlGeneral.setBorder(new TitledBorder(null, "General Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnlGeneral.setBounds(12, 13, 420, 98);
		contentPanel.add(pnlGeneral);
		pnlGeneral.setLayout(null);
		
		JLabel lblCheckInterval = new JLabel("Check Interval:");
		lblCheckInterval.setBounds(12, 29, 108, 16);
		pnlGeneral.add(lblCheckInterval);
		
		spnCheckInterval = new JSpinner();
		spnCheckInterval.setModel(new SpinnerNumberModel(new Integer((int) MainFrame.settings.getMessageCheckTime()), new Integer(5), null, new Integer(1)));
		spnCheckInterval.setBounds(132, 26, 225, 22);
		pnlGeneral.add(spnCheckInterval);
		
		JLabel lblMs = new JLabel("ms");
		lblMs.setBounds(364, 29, 44, 16);
		pnlGeneral.add(lblMs);
		
		JLabel lblOther = new JLabel("Other Settings:");
		lblOther.setBounds(12, 61, 108, 16);
		pnlGeneral.add(lblOther);
		
		chkEnableSound = new JCheckBox("Enable Sound");
		chkEnableSound.setBounds(129, 57, 113, 25);
		chkEnableSound.setSelected(MainFrame.settings.shouldPlaySound());
		pnlGeneral.add(chkEnableSound);
		
		chkScreen = new JCheckBox("Enable Screen On/Off");
		chkScreen.setBounds(246, 57, 162, 25);
		chkScreen.setSelected(MainFrame.settings.shouldRunScreen());
		pnlGeneral.add(chkScreen);
		
		JPanel pnlLoginInfo = new JPanel();
		pnlLoginInfo.setLayout(null);
		pnlLoginInfo.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Account Settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		pnlLoginInfo.setBounds(12, 124, 420, 98);
		contentPanel.add(pnlLoginInfo);
		
		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setBounds(12, 29, 108, 16);
		pnlLoginInfo.add(lblUsername);
		
		txtUsername = new JTextField();
		txtUsername.setBounds(132, 26, 276, 22);
		txtUsername.setText(MainFrame.settings.getUsername());
		pnlLoginInfo.add(txtUsername);
		txtUsername.setColumns(10);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setBounds(12, 58, 108, 16);
		pnlLoginInfo.add(lblPassword);
		
		txtPassword = new JPasswordField();
		txtPassword.setBounds(132, 55, 276, 22);
		txtPassword.setText(MainFrame.settings.getPassword());
		
		pnlLoginInfo.add(txtPassword);
		{
			JPanel pnlBtn = new JPanel();
			pnlBtn.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(pnlBtn, BorderLayout.SOUTH);
			{
				JButton btnOK = new JButton("OK");
				btnOK.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// On OK, record the changes.
						isAccepted = MainFrame.settings.UpdateParameters(
								txtUsername.getText(), 
								txtPassword.getText(), 
								((Integer) spnCheckInterval.getValue()).longValue(), 
								chkEnableSound.isSelected(), 
								chkScreen.isSelected());
						
						// Dispose the box.
						Component component = (Component) e.getSource();
				        JDialog dialog = (JDialog) SwingUtilities.getRoot(component);
				        dialog.dispose();
					}
				});
				btnOK.setActionCommand("OK");
				pnlBtn.add(btnOK);
				getRootPane().setDefaultButton(btnOK);
			}
			{
				JButton btnCancel = new JButton("Cancel");
				btnCancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// On cancel, discard the settings and close the dialog.
						Component component = (Component) e.getSource();
				        JDialog dialog = (JDialog) SwingUtilities.getRoot(component);
				        dialog.dispose();
					}
				});
				btnCancel.setActionCommand("Cancel");
				pnlBtn.add(btnCancel);
			}
		}
	}

	public boolean WasAccepted() {
		return isAccepted;
	}
}
