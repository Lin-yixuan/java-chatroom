package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JOptionPane;

public class Client extends Thread {

	private static ClientWindow clientFrame;
	private static final String SERVER_NAME = "localhost";
	private static final int PORT_NUM = 9999;
	private Socket socket;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	
	public Client(ClientWindow frame) {
		clientFrame = frame;
		try {
			socket = new Socket(InetAddress.getByName(SERVER_NAME), PORT_NUM);
			output = new ObjectOutputStream(socket.getOutputStream());
			input = new ObjectInputStream(socket.getInputStream());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		String username = null, connectedMessage = null;
		do {
			username = JOptionPane.showInputDialog(clientFrame, "請輸入使用者名稱：");
			//選擇「取消」不輸入使用者名稱
			if(username == null) {
				close();
				clientFrame.dispose();
				break;
			}
			
			//判斷使用者名稱是否輸入「空白」
			if(username.trim().isEmpty()) {
				JOptionPane.showMessageDialog(clientFrame, "使用者名稱不可輸入空白!", "輸入錯誤", JOptionPane.ERROR_MESSAGE);
			} else {
				try {
					//傳送使用者名稱
					output.writeObject(username);
					output.flush();
					//取得使用者名稱檢核是否通過的訊息
					connectedMessage = (String) input.readObject();
					//判斷名稱是否重複
					if(connectedMessage.equals("DUPLICATENAME")) {
						JOptionPane.showMessageDialog(clientFrame, "使用者名稱已重複!請重新輸入", "輸入錯誤", JOptionPane.ERROR_MESSAGE);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}			
		} while (username.trim().isEmpty() || connectedMessage.equals("DUPLICATENAME")); 
		
		//設定連線成功顯示訊息
		clientFrame.appendToTextArea(true, String.format("【%s已成功連線！歡迎加入聊天室】", username));
		clientFrame.appendToTextArea(false, "【輸入？可查詢詳細的操作明細】");
		//開啟接收訊息的執行緒
		openReceivedMessageThread();  
	}

	/**
	 * 寄送訊息
	 * @param isChatting
	 * @param message
	 */
	public synchronized void sendMessage(Boolean isChatting, String message) {
		String sendMessage;
		if(isChatting)
			sendMessage = "CHAT#" + message;
		else
			sendMessage = "QUERY#" + message;
		
		try {
			output.writeObject(sendMessage);
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 接收Server傳送的訊息
	 */
	public void openReceivedMessageThread() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while(true) {
					try {
						//訊息格式為「狀態#回傳訊息」
						String message = (String) input.readObject();
						//依據「CHAT#、QUERY#」判斷要將訊息加到哪一個JTextArea
						if(message.startsWith("CHAT#")) {
							message = message.substring("CHAT#".length());
							clientFrame.appendToTextArea(true, message);
						} else if (message.startsWith("QUERY#")) {
							message = message.substring("QUERY#".length());
							clientFrame.appendToTextArea(false, message);
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						close();
						break;
					}
				}
				
			}			
		}).start();
	}
	
	/**
	 * 關閉連線
	 */
	private void close() {
		try {
			input.close();
			output.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
