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
			username = JOptionPane.showInputDialog(clientFrame, "�п�J�ϥΪ̦W�١G");
			//��ܡu�����v����J�ϥΪ̦W��
			if(username == null) {
				close();
				clientFrame.dispose();
				break;
			}
			
			//�P�_�ϥΪ̦W�٬O�_��J�u�ťաv
			if(username.trim().isEmpty()) {
				JOptionPane.showMessageDialog(clientFrame, "�ϥΪ̦W�٤��i��J�ť�!", "��J���~", JOptionPane.ERROR_MESSAGE);
			} else {
				try {
					//�ǰe�ϥΪ̦W��
					output.writeObject(username);
					output.flush();
					//���o�ϥΪ̦W���ˮ֬O�_�q�L���T��
					connectedMessage = (String) input.readObject();
					//�P�_�W�٬O�_����
					if(connectedMessage.equals("DUPLICATENAME")) {
						JOptionPane.showMessageDialog(clientFrame, "�ϥΪ̦W�٤w����!�Э��s��J", "��J���~", JOptionPane.ERROR_MESSAGE);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}			
		} while (username.trim().isEmpty() || connectedMessage.equals("DUPLICATENAME")); 
		
		//�]�w�s�u���\��ܰT��
		clientFrame.appendToTextArea(true, String.format("�i%s�w���\�s�u�I�w��[�J��ѫǡj", username));
		clientFrame.appendToTextArea(false, "�i��J�H�i�d�߸ԲӪ��ާ@���ӡj");
		//�}�ұ����T���������
		openReceivedMessageThread();  
	}

	/**
	 * �H�e�T��
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
	 * ����Server�ǰe���T��
	 */
	public void openReceivedMessageThread() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while(true) {
					try {
						//�T���榡���u���A#�^�ǰT���v
						String message = (String) input.readObject();
						//�̾ڡuCHAT#�BQUERY#�v�P�_�n�N�T���[����@��JTextArea
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
	 * �����s�u
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
