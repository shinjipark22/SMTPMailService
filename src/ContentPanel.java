
import javax.swing.*;
import javax.swing.border.*;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


class ContentPanel extends JPanel {
	private ContentPanel contentPanel;
	private MailAppClient mailAppClient;
	private SmtpMailService smtpMailService;
	
	JLabel senderLabel;
	JLabel receiverLabel;
	JLabel subjectLabel;
	JLabel fileLabel;
	JLabel fileListLabel;
	
	JTextField senderField;
	JTextField receiverField;
	JTextField subjectField;
	
	JButton sendButton;
	JButton attachButton;
	
	JTextArea contentArea;
	
	JTextArea fileField;
	JFileChooser fileSelector;
	private List<File> selectedFiles = new ArrayList<>();

	
	ContentPanel (MailAppClient client, SmtpMailService smtpService){
		contentPanel = this;
		mailAppClient = client;
		smtpMailService = smtpService;
		setLayout(null);
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		// Sender 라벨 및 텍스트 필드
        senderLabel = new JLabel("Sender");
        senderLabel.setBounds(20, 15, 70, 20);
        add(senderLabel);

        senderField = new JTextField();
        senderField.setBounds(100, 20, 170, 20);
        add(senderField);

        // Reciever 라벨 및 텍스트 필드
        receiverLabel = new JLabel("Reciever");
        receiverLabel.setBounds(20, 45, 70, 20);
        add(receiverLabel);

        receiverField = new JTextField();
        receiverField.setBounds(100, 50, 170, 20);
        add(receiverField);

        
        // Send 버튼
        sendButton = new JButton("전송");
        sendButton.setBounds(450, 270, 80, 30);
        add(sendButton);
        

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean result = smtpMailService.sendMail(senderField.getText(), receiverField.getText(), subjectField.getText(), contentArea.getText(), selectedFiles.toArray(new File[0]));
                
                if(result) {
                	receiverField.setText("");
                    subjectField.setText("");
                    contentArea.setText("");
			fileField.setText("");
                    selectedFiles.clear();
                }
            }
        });
        
        // Subject 라벨 및 텍스트 필드
        subjectLabel = new JLabel("Subject");
        subjectLabel.setBounds(20, 80, 50, 20);
        add(subjectLabel);

        subjectField = new JTextField();
        subjectField.setBounds(20, 100, 370, 20);
        add(subjectField);

        // Content 영역
        contentArea = new JTextArea();
        contentArea.setBounds(20, 130, 380, 170);
        contentArea.setLineWrap(true); 
        add(contentArea);

        
        // 파일 첨부 관련
        fileField = new JTextArea();
        fileField.setBounds(430, 130, 120, 100);
        fileField.setEditable(false);
        add(fileField);
        
        fileSelector = new JFileChooser();
        fileSelector.setMultiSelectionEnabled(true);
        
        attachButton = new JButton("파일 첨부");
        attachButton.setBounds(430, 90, 90, 30);
        attachButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
	            int check = fileSelector.showOpenDialog(contentPanel);
	            if (check == JFileChooser.APPROVE_OPTION) {
	                File[] selectedFilesArray = fileSelector.getSelectedFiles();
	                for (File file : selectedFilesArray) {
	                	selectedFiles.add(file);
	                }
	                StringBuilder fileNames = new StringBuilder();
	                for (File file : selectedFiles) {
	                    if (fileNames.length() > 0) 
	                    	fileNames.append("\n");
	                    fileNames.append(file.getName());
	                }
	                fileField.setText(fileNames.toString());
	            }
			}
		});
        add(attachButton);
        
	}

}
