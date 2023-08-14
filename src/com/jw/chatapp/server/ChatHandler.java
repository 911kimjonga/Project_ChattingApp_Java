package com.jw.chatapp.server;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.jw.chatapp.protocol.MessageType;

/**
 * 채팅 핸들러
 * @author 김종원
 */
public class ChatHandler extends Thread {
	
	private Socket socket;
	private DataInput in;
	private DataOutput out;
	
	private ChatServer chatServer;
	private String nickName;
	private String toNickName;
	private boolean running;
	
	
	public ChatHandler(Socket socket, ChatServer chatServer) throws IOException {
		this.socket = socket;
		this.chatServer = chatServer;
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		running = true;
		
	}
	
	public ChatServer getChatServer() {
		return chatServer;
	}

	public String getNickName() {
		return nickName;
	}

	public String getTonickName() {
		return toNickName;
	}

	public Socket getSocket() {
		return socket;
	}

	public DataInput getIn() {
		return in;
	}

	public DataOutput getOut() {
		return out;
	}

	@SuppressWarnings("incomplete-switch")
	public void process() throws IOException {
		
		while(running) {
			// 파싱되지 않은 클라이언트 메세지
			String clientMessage = in.readUTF();
			System.out.println("[디버깅] : " + clientMessage);
			
			// 클라이언트 메세지 파싱
			String[] tokens = clientMessage.split("\\|");
			// 메세지 유형
			MessageType messageType = MessageType.valueOf(tokens[0]);
			
			// 클라이언트 전송 메세지 종류에 따른 처리
			switch (messageType) {
				// 연결 메세지
				case CONNECT:
					// 연결한 클라이언트 닉네임
					nickName = tokens[1];
					chatServer.addChatClient(this);
					chatServer.sendClientList(clientMessage);
					chatServer.sendMessageAll(clientMessage);
					break;
					
				// 다중 채팅 메세지
				case CHAT_MESSAGE:
					chatServer.sendMessageAll(clientMessage);
					break;
					
				// DM 채팅 메세지
				case DM_MESSAGE:
					nickName = tokens[1];
					toNickName = tokens[2];
					chatServer.sendDmMessage(clientMessage);
					break;
					
				// 연결 종료 메세지
				case DIS_CONNECT:
					chatServer.removeChatClient(this);
					chatServer.sendClientList(clientMessage);
					chatServer.sendMessageAll(clientMessage);
					running = false;
					break;
			}
		}
		close();
	}

	/**
	 * 자기 자신에게 메세지 출력
	 * @param message
	 * @throws IOException
	 */
	public void sendMessage(String message) throws IOException {
		out.writeUTF(message);
	}

	/**
	 * 종료
	 */
	public void close() {
		if(socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void run() {
		try {
			process();
			System.out.println("[ChatCilient(" + nickName + ")]님 연결을 종료하였습니다.");
		} catch (IOException e) {
			System.err.println("에코 처리 중 예기치 않은 오류가 발생하였습니다.");
		}
	}
}