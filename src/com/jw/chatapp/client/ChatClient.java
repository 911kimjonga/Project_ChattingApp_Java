package com.jw.chatapp.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.jw.chatapp.protocol.MessageType;

/**
 * TCP/IP 기반의 채팅 클라이언트
 */
public class ChatClient {

	private static final String SERVER_IP = "localhost";
	private static final int SERVER_PORT = 7777;

	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;

	ChatFrame chatFrame;

	public ChatClient(ChatFrame chatFrame) {
		this.chatFrame = chatFrame;
	}

	/**
	 * 서버 연결
 	 */
	public void connectServer() throws IOException {
		socket = new Socket(SERVER_IP, SERVER_PORT);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
	}

	/**
	 * 서버 연결 종료
	 */
	public void disConnectServer() throws IOException {
		if (socket != null) {
			socket.close();
		}
	}

	/**
	 * 메세지 전송
	 * @param message 전송 메세지
	 * @throws IOException
	 */
	public void sendMessage(String message) throws IOException {
		out.writeUTF(message);
	}

	/**
	 * 메세지 수신
	 */
	public void receiveMessage() {
		// 서버로부터 전송되는 메시지를 실시간 수신하기 위해 스레드 생성 및 시작
		Thread thread = new Thread() {
			public void run() {
				try {
					while (true) {
						String serverMessage = in.readUTF();

						System.out.println(serverMessage);
						String[] tokens = serverMessage.split("\\|");

						MessageType messageType = MessageType.valueOf(tokens[0]);

						String senderNickName = tokens[1];

						// 클라이언트 전송 메세지 종류에 따른 처리
						switch (messageType) {
						// 연결 메세지
						case CONNECT:
							chatFrame.appendMessage("@@@@ " + senderNickName + "님이 연결하였습니다 @@@@");
							break;

						// 다중 채팅 메세지
						case CHAT_MESSAGE:
							String chatMessage = tokens[2];
							chatFrame.appendMessage("[" + senderNickName + "] : " + chatMessage);
							break;

						// 접속자 리스트 메세지
						case CLIENT_LIST:
							chatFrame.resetList();
							chatFrame.restChoice();
							String listName = tokens[2];
							String[] names = listName.split(",");
							String name = null;
							for (int i = 0; i < names.length; i++) {
								name = names[i];
								chatFrame.appendList(name);
								if (!name.equals(chatFrame.nicknameTF.getText())) {
									chatFrame.appendChoice(name);
								}
							}
							break;

						// DM 채팅 메세지
						case DM_MESSAGE:
							String toNickName = tokens[2];
							chatMessage = tokens[3];
							if (toNickName.equals(chatFrame.nicknameTF.getText())) {
								chatFrame.appendMessage("[" + senderNickName + "]님 귓속말 수신 : " + chatMessage);
							} else if (senderNickName.equals(chatFrame.nicknameTF.getText())) {
								chatFrame.appendMessage("[" + toNickName + "]님에게 귓속말 전송 : " + chatMessage);
							}
							break;

						// 연결 종료 메세지
						case DIS_CONNECT:
							chatFrame.appendMessage("#### " + senderNickName + "님이 연결 해제하였습니다 ####");
							break;
						}
					}
				} catch (IOException e) {

				} finally {

				}
			}
		};
		thread.start();
	}
}