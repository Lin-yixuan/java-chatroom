package server;

import java.util.HashMap;
import java.util.LinkedList;

public class ConnectionService {

	private static HashMap<String, SuperSocket> connections = new HashMap<String, SuperSocket>();
	
	/**
	 * �s�W�s�u
	 * @param newUsername
	 * @param socket
	 * @return �s�W�O�_���\
	 */
	public synchronized boolean addConnection(String newUsername, SuperSocket socket) {
		//�ϥ�useranme�@��Key�A�G���i����
		if(connections.containsKey(newUsername)) {
			return false;
		} else {
			connections.put(newUsername, socket);
			return true;
		}
	}
	
	/**
	 * �R���s�u
	 * @param userName
	 */
	public synchronized void deleteConnection(String userName){
		connections.remove(userName);
	}
	
	/**
	 * ���o�Ҧ��b�u���ϥΪ̲M��(���H�e�̥~)
	 * @param senderName
	 */
	public synchronized LinkedList<SuperSocket> getConnectionList(String senderName) {
		LinkedList<SuperSocket> userList = new LinkedList<SuperSocket>();
		for(String userName: connections.keySet()) {
			if(!userName.equals(senderName)) {
				userList.add(connections.get(userName));
			}
		}
		return userList;
	}
}
