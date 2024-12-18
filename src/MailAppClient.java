import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class MailAppClient extends JFrame{
	private JPanel mainPanel;
	private CardLayout cardLayout;
	private SmtpMailService smtpMailService;
	
	MailAppClient(){
		smtpMailService = new SmtpMailService(this);
		setTitle("MailAppClient");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		cardLayout = new CardLayout();
		mainPanel = new JPanel(cardLayout);
        mainPanel.add(new LoginPanel(this, smtpMailService), "LoginPanel");
        mainPanel.add(new ContentPanel(this, smtpMailService), "ContentPanel");
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		
		JButton exitButton = new JButton("Exit");
		exitButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		exitButton.setPreferredSize(new Dimension(70, 30));	// 레이아웃 매니저가 크기를 관리하기 때문에, PreferredSize를 사용해야함
		exitButton.setBackground(new Color(128, 128, 128, 150));

		exitButton.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        System.exit(0); // 애플리케이션 종료
		    }
		});
		
		JPanel exitButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		exitButtonPanel.add(exitButton);
		getContentPane().add(exitButtonPanel, BorderLayout.SOUTH);
		
		setSize(350, 450);
		setVisible(true);
	}
	
	
	public void showLoginPanel() {
		cardLayout.show(mainPanel, "LoginPanel");
		setSize(350, 450);
	}
	
	public void showContentPanel() {
		cardLayout.show(mainPanel, "ContentPanel");
		setSize(600, 400);
	}
	
	public static void main(String[] args) {
		new MailAppClient();
	}
}
