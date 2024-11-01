import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.Base64;
import java.util.HashMap;

import java.io.File;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map; 


public class MailAppClient extends JFrame{
	private JPanel mainPanel;
	private CardLayout cardLayout;
	private MailService mailService;
	
	private char mode = 'c';
	
	MailAppClient(){
		mailService = new MailService();
		
		setTitle("MailAppClient");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		cardLayout = new CardLayout();
		mainPanel = new JPanel(cardLayout);
        mainPanel.add(new LoginPanel(this, mailService), "LoginPanel");
        mainPanel.add(new ContentPanel(this, mailService), "contentPanel");
        mainPanel.add(new ReceiverPanel(this), "ReceiverPanel");
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		
		JButton exitButton = new JButton("Exit");
		exitButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		exitButton.setPreferredSize(new Dimension(70, 30));	// 레이아웃 매니저가 크기를 관리하기 때문에, PreferredSize를 사용해야함
		exitButton.setBackground(new Color(128, 128, 128, 150));
		
		JPanel exitButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		exitButtonPanel.add(exitButton);
		getContentPane().add(exitButtonPanel, BorderLayout.SOUTH);
		
		setSize(350, 450);
		setVisible(true);
	}
	
	
	public void showContentPanel() {
		cardLayout.show(mainPanel, "contentPanel");
		setSize(600, 400);
	}
	
	public void showReceiverPanel() {
		cardLayout.show(mainPanel, "ReceiverPanel");
		setSize(600, 400);
	}
	
	
	public void changeMod() {
		if (mode == 'c') {
			mode = 'r';
			showReceiverPanel();
		}
		else if (mode == 'r') {
			mode = 'c';
			showContentPanel();
		}
	}
	
	
	public static void main(String[] args) {
		new MailAppClient();
	}
}
