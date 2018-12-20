package com.deschere.awsmc;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.Color;
import javax.swing.SwingConstants;
import com.deschere.awsmc.MinecraftManager;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MinecraftMaster {

	private JFrame frame;
	private JTextField textServerStatus;
	private JTextField textServerIP;
	private MinecraftManager mcMM;
	private JComboBox<String> worldTag;
	private String tagDefault = "<Select a Tag>";
	private JButton btnStartServer;
	private String iPDefault = "nonya.business.com.org";
	private JButton btnCopyIpTo;
	private String nothing;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MinecraftMaster window = new MinecraftMaster();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		
	}

	/**
	 * Create the application.
	 */
	public MinecraftMaster() {
		initialize();
		mcMM = new MinecraftManager();
		mcMM.setCreds("<<CHANGE TO YOURS>>", "<<CHANGE TO YOURS>>");
		String[] tagList = mcMM.getInstancesMCWorldTag().stream().toArray(String[]::new);
	    List<String> both = new ArrayList<String>(1 + tagList.length);
	    Collections.addAll(both, new String[] {tagDefault});
	    Collections.addAll(both, tagList);		
		
		worldTag.setModel(new DefaultComboBoxModel(both.toArray(new String[both.size()])));
		worldTag.setEnabled(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 341, 200);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		
		JLabel lblServertag = new JLabel("ServerTag");
		panel.add(lblServertag);
		
		worldTag = new JComboBox();
		worldTag.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			
				if(arg0.getActionCommand().equals("comboBoxChanged") && !worldTag.getSelectedItem().equals(tagDefault))
				{
					setStatus(mcMM.getInstanceStateByMCWorldTag(worldTag.getSelectedItem().toString()));
					setIP(mcMM.getInstanceIPByMCWorldTag(worldTag.getSelectedItem().toString()));
				} else {
					setStatus("stopped");
					setIP(iPDefault);
				}
			}
		});
		worldTag.setEnabled(false);
		worldTag.setToolTipText("Select a Server tag");
		worldTag.setModel(new DefaultComboBoxModel(new String[] {tagDefault}));
		panel.add(worldTag);
		
		btnStartServer = new JButton("Start Server");
		btnStartServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(arg0.getActionCommand().equals("Start Server")) { try {
					callStartServer();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} }
			}
		});
		panel.add(btnStartServer);
		btnStartServer.setEnabled(false);
		
		JPanel panel_1 = new JPanel();
		frame.getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(null);
		
		JLabel lblServerStatus = new JLabel("Server Status");
		lblServerStatus.setBounds(10, 8, 95, 14);
		panel_1.add(lblServerStatus);
		
		textServerStatus = new JTextField();
		textServerStatus.setHorizontalAlignment(SwingConstants.CENTER);
		textServerStatus.setText("Offline");
		textServerStatus.setBackground(Color.RED);
		textServerStatus.setFont(new Font("Tahoma", Font.BOLD, 11));
		textServerStatus.setBounds(110, 5, 86, 20);
		textServerStatus.setEditable(false);
		panel_1.add(textServerStatus);
		textServerStatus.setColumns(10);
		
		JLabel lblServerIp = new JLabel("Server IP");
		lblServerIp.setBounds(10, 33, 60, 14);
		panel_1.add(lblServerIp);
		
		textServerIP = new JTextField();
		textServerIP.setHorizontalAlignment(SwingConstants.CENTER);
		textServerIP.setText(iPDefault);
		textServerIP.setEditable(false);
		textServerIP.setBounds(71, 33, 242, 20);
		panel_1.add(textServerIP);
		textServerIP.setColumns(10);
		
		btnCopyIpTo = new JButton("Copy IP to Clipboard");
		btnCopyIpTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (arg0.getActionCommand().equals("Copy IP to Clipboard")) {
					
					Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
			        Clipboard systemClipboard = defaultToolkit.getSystemClipboard();
	
			        systemClipboard.setContents(new StringSelection(textServerIP.getText()), null);
				}
			}
		});
		btnCopyIpTo.setEnabled(false);
		btnCopyIpTo.setBounds(151, 60, 162, 23);
		panel_1.add(btnCopyIpTo);
	}
	
	private void setStatus(String status)
	{
		if(status.equals("stopped") || status.equals("stopping"))
		{
			textServerStatus.setText("Offline");
			textServerStatus.setBackground(Color.RED);
			if (!worldTag.getSelectedItem().equals(tagDefault)) {btnStartServer.setEnabled(true);}
			btnCopyIpTo.setEnabled(false);
		} else if(status.equals("starting") || status.equals("running"))
		{
			textServerStatus.setText("Online");
			textServerStatus.setBackground(Color.GREEN);
			btnStartServer.setEnabled(false);
			btnCopyIpTo.setEnabled(true);
		}
		
	}
	
	private void setIP(String iPAddress)
	{
		textServerIP.setText(iPAddress);
	}
	
	private void callStartServer() throws InterruptedException
	{
		mcMM.startInstance(mcMM.getInstanceIDByMCWorldTag(worldTag.getSelectedItem().toString()));
		Thread.sleep(4000);
		setIP(mcMM.getInstanceIPByMCWorldTag(worldTag.getSelectedItem().toString()));
		setStatus(mcMM.getInstanceStateByMCWorldTag(worldTag.getSelectedItem().toString()));
	}
	
}
