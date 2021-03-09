package server;

import java.util.HashMap;
import java.util.LinkedList;

public class ConnectionService {

	private static HashMap<String, SuperSocket> connections = new HashMap<String, SuperSocket>();
	
	/**
	 * 新增連線
	 * @param newUsername
	 * @param socket
	 * @return 新增是否成功
	 */
	public synchronized boolean addConnection(String newUsername, SuperSocket socket) {
		//使用useranme作為Key，故不可重複
		if(connections.containsKey(newUsername)) {
			return false;
		} else {
			connections.put(newUsername, socket);
			return true;
		}
	}
	
	/**
	 * 刪除連線
	 * @param userName
	 */
	public synchronized void deleteConnection(String userName){
		connections.remove(userName);
	}
	
	/**
	 * 取得所有在線的使用者清單(除寄送者外)
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
