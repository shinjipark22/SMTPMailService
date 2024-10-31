import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.EtchedBorder;


public class ReceiverPanel extends JPanel{
	private MailAppClient mailAppClient;
	private Pop3MailService pop3MailService;
	
	JTextField senderField;
	JTextField subjectField;
	JTextArea textArea;
	
	ReceiverPanel (MailAppClient client, Pop3MailService service){
		mailAppClient = client;
		pop3MailService = service;
		setLayout(null);
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		
		JButton changeMod = new JButton("메일쓰기");
		changeMod.setBounds(5, 10, 82, 50);
		changeMod.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mailAppClient.changeMod();
			}
		});
		add(changeMod);
		
		
		JButton refreshBtn = new JButton("새로고침");
		refreshBtn.setBounds(92, 10, 82, 50);
		refreshBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pop3MailService.receiveMail();
			}
		});
		add(refreshBtn);
		
		
		JLabel RecievedMailLabel = new JLabel("수신함");
		RecievedMailLabel.setBounds(180, 25, 60, 20);
        add(RecievedMailLabel);
        
        JLabel senderLabel = new JLabel("Sender");
        senderLabel.setBounds(100, 70, 50, 20);
        add(senderLabel);

		JLabel subjectLabel = new JLabel("Subject");
        subjectLabel.setBounds(10, 80, 50, 20);
        add(subjectLabel);

        senderField = new JTextField();
        senderField.setBounds(180, 70, 400, 20);
        senderField.setEditable(false);
        add(senderField);
        
        subjectField = new JTextField();
        subjectField.setBounds(10, 100, 570, 20);
        subjectField.setEditable(false);
        add(subjectField);

        textArea = new JTextArea();
        textArea.setBounds(10, 130, 570, 220);
        textArea.setEditable(false);
        add(textArea);
        
        setVisible(true);
	}
	
	public void fetchReceiveMail() {
		ReceiveEmail[] emailArray = pop3MailService.receiveMail();
		
		String[] EmailSubjects = new String[emailArray.length];
        for (int i = 0; i < emailArray.length; i++) {
            EmailSubjects[i] = emailArray[i].subject;
        }
        
        // JList 생성 및 JScrollPane에 추가
        JList<String> mailList = new JList<>(EmailSubjects);
        mailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane mailListScrollPane = new JScrollPane(mailList);
        mailListScrollPane.setBounds(250, 10, 330, 50);
        EtchedBorder border = new EtchedBorder(EtchedBorder.RAISED);
        mailListScrollPane.setBorder(border);
        add(mailListScrollPane);
        
        
     // JList 항목 선택 시 Email 데이터 표시
        mailList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // 값이 최종적으로 선택될 때만 동작
                int selectedIndex = mailList.getSelectedIndex();
                if (selectedIndex != -1) {
                    ReceiveEmail selectedEmail = emailArray[selectedIndex];
                    subjectField.setText(selectedEmail.subject);
                    senderField.setText(selectedEmail.sender);
                    textArea.setText(selectedEmail.content);
                }
            }
        });
	}
	
}