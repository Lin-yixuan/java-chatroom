package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {

	private static final int PORT_NUM = 9999;
	private static final int MAX_CONNECTIONS = 10;
	private ServerSocket serverSocket;
	
	public static void main(String[] args) {
		Server serverService = new Server(PORT_NUM, MAX_CONNECTIONS);
		serverService.start();
	}
	
	public Server(int portNum, int maxConnections) {
		try {
			serverSocket = new ServerSocket(portNum, maxConnections);
			System.out.println("等待連線......");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		ConnectionService connectionService = new ConnectionService();
		while(true) {
			try {
				Socket socket = serverSocket.accept(); //接受連線
				
				ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
				SuperSocket superSocket = new SuperSocket(socket, output, input);
				
				Connection connection = new Connection(superSocket, connectionService);
				connection.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
