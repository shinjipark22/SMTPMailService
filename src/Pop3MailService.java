import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JOptionPane;


public class Pop3MailService {
	private MailAppClient mailAppClient;
	
	private static final String pop3Server = "pop.naver.com";
    private static final int pop3Port = 995;
    private static final int MAX_EMAILS_COUNT = 30;
    
    private String encodedId;
    private String encodedPassword;
    
    Pop3MailService(MailAppClient client){
    	mailAppClient = client;
    }
    
    public void cachingLoginInfo(String id, String password) {
    	encodedId = id;
    	encodedPassword = password;
    }
    
    private void authenticateToServer(BufferedReader reader, PrintWriter writer) throws Exception {
    	 //아이디 전송
    	 writer.println("USER " + encodedId);
         writer.flush();

         // 비밀번호 전송
         writer.println("PASS " + encodedPassword);
         writer.flush();
         checkLoginResponse(reader.readLine(), "\n로그인을 할 수 없습니다. \\n 1. 아이디와 비밀번호를 확인해주세요. \\n 2. 네이버에서 SMTP 사용 허가를 설정해주세요. \\n 3. 2단계 인증이 설정된 경우, 별도의 어플리케이션 비밀번호가 필요합니다.");
    }
    
    
    public ReceiveEmail[] receiveMail() {
    	List<ReceiveEmail> emailList = new ArrayList<>();
    	try(
    			SSLSocket pop3Socket = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket(pop3Server, pop3Port);
    			BufferedReader reader = new BufferedReader(new InputStreamReader(pop3Socket.getInputStream()));
    			PrintWriter writer = new PrintWriter(new OutputStreamWriter(pop3Socket.getOutputStream()));
    			){
    		checkResponse(reader.readLine(), "현재 POP3 서버에 접속할 수 없습니다.");
    		
    		authenticateToServer(reader, writer);
    		
    		writer.println("STAT");
            writer.flush();
            checkResponse(reader.readLine(), "메일 수신에 실패하였습니다.");

            String[] statParts = reader.readLine().split(" ");
            int totalEmailsCount = Integer.parseInt(statParts[1]);
            int retrieveCount = Math.min(totalEmailsCount, MAX_EMAILS_COUNT);
            // 최신 메일부터 MAX_EMAILS_COUNT개까지 읽어와서 ReceiveEmail 객체로 저장
            for (int i = totalEmailsCount; i > totalEmailsCount - retrieveCount; i--) {
                ReceiveEmail email = fetchEmail(reader, writer, i);
                if (email != null) {            	
                    emailList.add(email);
                }
            }
            
            writer.println("QUIT");
            writer.flush();
    	}catch(Exception e) {
    		JOptionPane.showMessageDialog(null, "이메일 수신에 실패하였습니다. 원인 : " + e.toString(), "error", JOptionPane.WARNING_MESSAGE);
    		
    		if (e instanceof LoginException) {
    			mailAppClient.showLoginPanel();
    		}
    		return null;
    	}
		
    	return emailList.toArray(new ReceiveEmail[0]);
    }
    
     
    // 각 메일을 가져와 ReceiveEmail 객체로 변환
    private ReceiveEmail fetchEmail(BufferedReader reader, PrintWriter writer, int emailIndex) throws Exception {
        writer.println("RETR " + emailIndex);
        writer.flush();
        String response = reader.readLine();

        if (!response.startsWith("+OK")) {
            System.out.println("[POP3] " + emailIndex + "번째 메일을 가져오지 못했습니다.");
            return null;
        }
        
        
        StringBuilder contentBuilder = new StringBuilder();
        List<String> fileNameList = new ArrayList<String>();
        
        
        boolean isHeader = true;
        boolean isTextHeader = false;
        boolean isContentReading = false;
        
        boolean isAttachedFile = false;
        boolean isBase64Content = false;
        boolean isReadingContent = false;
        
        String line;
        String sender = "";
        String subject = "";
        String content = "";
        String boundary = "";
        String encodingMethod = "";
        
        while (!(line = reader.readLine()).equals(".")) {

            if (line.startsWith("From:")) {
            	sender = senderStringBulider(reader, line);
            } else if (line.startsWith("Subject:")) {
                subject = subjectStringBulider(reader, line);
            } else if (line.startsWith("Content-Transfer-Encoding:")) {
            	if(line.toLowerCase().contains("base")) {
            		encodingMethod = "base";
            	}else if(line.toLowerCase().contains("quote")) {
            		encodingMethod = "quote";
            	}else if(line.toLowerCase().contains("bit")){
            		encodingMethod = "bit";
            	}else {
            		encodingMethod = "";
            	}
                isBase64Content = encodingMethod.length() > 0;
            } else if(line.startsWith("Content-Disposition: attachment")) {
            	isAttachedFile = true;
            	int nameIndex = line.indexOf("filename=");
                fileNameList.add(line.substring(nameIndex + 9).replace("\"", "").trim());
            }
            
            
            // 본문 내용 시작
            if (line.isEmpty()) {
                isReadingContent = true;
                continue;
            }

            // 본문 내용 처리
            if (isReadingContent) {
                if (isBase64Content) {
                    contentBuilder.append(line);
                } else {
                    contentBuilder.append(line).append("\n");
                }
            }
            
            content = contentBuilder.toString();
        
        }
        
        if(encodingMethod.equals("quote")) {
        	return null;
        }
        return new ReceiveEmail(sender, subject, content, encodingMethod, fileNameList);
    }
    
        
        
    private String senderStringBulider(BufferedReader reader, String line) throws Exception{
    	StringBuilder senderBuilder = new StringBuilder();
    	Pattern pattern = Pattern.compile("=\\?([A-Za-z0-9-]+)\\?(B|Q)\\?([A-Za-z0-9+/=._-]+)\\?=");
        Matcher matcher = pattern.matcher(line);
        
        //인코딩된 문자열일 경우, 여러 줄로 올 가능성이 있으므로 반복문을 통해 해당 헤더에 해당하는 내용 전부 추출
        if(matcher.find()) {
        	senderBuilder.append(line.substring(5).trim());	// 5 -> "From:"
        	line = reader.readLine();
        	
        	// 다음 줄이 공백으로 시작하면 헤더의 연속 줄로 간주
        	while((line.startsWith(" ") || line.startsWith("\t"))) {
        		senderBuilder.append(line.trim());
        		line = reader.readLine();
        	}
        }else {
        	//인코딩되지 않은 경우, readLine()으로 한번에 처리
        	senderBuilder.append(line.substring(5).trim());
        }
        
        return senderBuilder.toString();
    }
    
    
    private String subjectStringBulider(BufferedReader reader, String line) throws Exception{
    	StringBuilder subjectBuilder = new StringBuilder();
    	Pattern pattern = Pattern.compile("=\\?([A-Za-z0-9-]+)\\?(B|Q)\\?([A-Za-z0-9+/=._-]+)\\?=");
        Matcher matcher = pattern.matcher(line);
        
        //인코딩된 문자열일 경우, 여러 줄로 올 가능성이 있으므로 반복문을 통해 해당 헤더에 해당하는 내용 전부 추출
        if(matcher.find()) {
        	subjectBuilder.append(line.substring(8).trim());	// 8 -> "Sender:"
        	line = reader.readLine();
        	
        	// 다음 줄이 공백으로 시작하면 헤더의 연속 줄로 간주
        	while((line.startsWith(" ") || line.startsWith("\t"))) {
        		subjectBuilder.append(line.trim());
        		line = reader.readLine();
        	}
        }else {
        	//인코딩되지 않은 경우, readLine()으로 한번에 처리
        	subjectBuilder.append(line.substring(8).trim());
        }
        
        return subjectBuilder.toString();
    }
    
    

    
    
    // 응답 메세지를 확인한 후, pop3의 응답 코드로 +OK를 받지 않으면 예외를 던짐
    private void checkResponse(String response, String message) throws Exception{
    	if (response == null || !response.startsWith("+OK"))
    			throw new Exception(response + ", message : " + message);
    }
    
    // 아이디, 비밀번호 관련 에러는 LoginException으로 따로 처리 --> 로그인 UI로 다시 돌아가기 위함
    private void checkLoginResponse(String response, String message) throws LoginException{
    	if (response == null || !response.startsWith("+OK"))
    		throw new LoginException(response + ", message : " + message);
    }
}
