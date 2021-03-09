package server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SuperSocket {
	private Socket socket;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	
	public SuperSocket(Socket socket, ObjectOutputStream output, ObjectInputStream input) {
		this.socket = socket;
		this.output = output;
		this.input = input;
	}
	
	public Socket getSocket() {
		return this.socket;
	}
	
	public ObjectOutputStream getObjectOutputStream() {
		return this.output;
	}
	
	public ObjectInputStream getObjectInputStream() {
		return this.input;
	}
	
}
