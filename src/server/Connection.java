package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Connection extends Thread {

	private SuperSocket superSocket;
	private ConnectionService connectionService;
	private LinkedList<SuperSocket> connectionsList;
	private String username;
	private boolean isExistingName = false; //�w�]�ϥΪ̦W�٤��s�b
	private boolean userWantToLeave = false; //�w�]�ϥΪ̤����}

	public Connection(SuperSocket superSocket, ConnectionService connectionService) {
		this.superSocket = superSocket;
		this.connectionService = connectionService;
	}

	@Override
	public void run() {
		String newUsername = new String(), connectedMsg = new String();

		while (!isExistingName) {
			try {
				//�����ϥΪ̦W��
				newUsername = (String) superSocket.getObjectInputStream().readObject();

				//�P�_�ϥΪ̦W�٬O�_�w����
				if (!connectionService.addConnection(newUsername, superSocket)) {
					connectedMsg = "DUPLICATENAME"; // �ϥΪ̦W�٤w����
				} else {
					connectedMsg = "�i" + newUsername + "�w�s�u���\�I�j";
					username = newUsername;
					isExistingName = true;
				}
				superSocket.getObjectOutputStream().writeObject(connectedMsg);
				superSocket.getObjectOutputStream().flush();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				close(); //�����s�u
				break;
			}
		}

		//�s�����Ҧ��b�u�ϥΪ̦��s�ϥΪ̥[�J���T��
		String newUserMessage = String.format("�i%s�w�[�J��ѫǡj", newUsername);
		broadcast(newUsername, newUserMessage);

		while (!userWantToLeave) {
			try {
				String receivedMessage = (String) superSocket.getObjectInputStream().readObject();
				String[] splitMessage = receivedMessage.split("#"); //�T���榡�G���A#�T��
				processReceivedMessage(splitMessage[0], newUsername, splitMessage[1]);
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				//�ϥΪ����}��ѫ�
				userWantToLeave = true;
				String leaveMessage = String.format("�i%s�w���}��ѫǡj", username);
				broadcast(username, leaveMessage); //�s�����}�T��
				close(); //�����s�u
				connectionService.deleteConnection(username); //�����ϥΪ�
			}
		}

	}

	/**
	 * �����s�u
	 */
	private void close() {
		try {
			superSocket.getObjectOutputStream().close();
			superSocket.getObjectInputStream().close();
			superSocket.getSocket().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * �s����ѫǰT��
	 * @param senderName �H��̦W��
	 * @param sendMessage �H�e�T��
	 */
	private void broadcast(String senderName, String sendMessage) {
		String message = "CHAT#" + sendMessage;
		
		//���s��s�@���s�u�M��
		connectionsList = connectionService.getConnectionList(senderName);
		try {
			for (int i = 0; i < connectionsList.size(); i++) {
				connectionsList.get(i).getObjectOutputStream().writeObject(message);
				connectionsList.get(i).getObjectOutputStream().flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * �̾ڪ��A�B�z���쪺�T��
	 * @param state ���A (CHAT-���, QUERY-��Ƭd��)
	 * @param senderName �H�e�̦W��
	 * @param message �H�e�T��
	 * @throws IOException
	 */
	private void processReceivedMessage(String state, String senderName, String message) throws IOException {
		switch (state) {
		case "CHAT": //���
			broadcast(senderName, String.format("%s�G%s", senderName, message));
			break;
		case "QUERY": //��Ƭd��
			String responseMessage;

			//�Ymessage���]�t@�h��ܦ��d�߱���
			if (message.contains("@")) {
				String[] splitMessage = message.split("@");
				responseMessage = String.format("QUERY#Server�G\r\n%s", processQueryData(splitMessage[0], splitMessage[1]));
			} else {
				responseMessage = String.format("QUERY#Server�G\r\n%s", processQueryData(message, null));
			}

			try {
				superSocket.getObjectOutputStream().writeObject(responseMessage);
				superSocket.getObjectOutputStream().flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}
	}

	/**
	 * �̾ڨϥΪ̿�J�����O�A�^�Ǭd�ߵ��G
	 * @param command ���O
	 * @param condition �d�߱���
	 * @return responseMessage �d�ߵ��G
	 * @throws IOException
	 */
	private String processQueryData(String command, String condition) throws IOException {
		String responseMessage = new String();
		
		switch (command.toUpperCase()) {
		case "?": //�d�߾ާ@����
			responseMessage = "�@- �d�߬Y�������Ӥ@�g�Ѯ�A�п�J[weather@���d�߿���]\r\n" +
							  "�@- �d�߬Y��������Ů�~��A�п�J[air@���d�߿���]\r\n" +
							  "�@- �d�ߧY�ɷs�D�A�п�J[news]\r\n" +
							  "�@�d�ҡG��J�uweather#�O�_���v�Y�i�d�߻O�_�����Ӥ@�g���Ѯ�";
			break;
			
		case "WEATHER": //�d�ߥ��Ӥ@�g�Ѯ�@(��ƨӷ��GPChome�s�D�@http://news.pchome.com.tw/weather/taiwan)
			Document document = Jsoup.connect("http://news.pchome.com.tw/weather/taiwan").get(); //�ШD�s�u
			Elements content = document.select("div#container > div#cont-area > div > div.wth_chart_area > div > section"); //�ѪR
			String[] dates = null, conditions = null, temperatures = null;
			
			for (int i = 0; i < content.size(); i++) {
				if (content.get(i).select("div").text().equals(condition)) {
					dates = content.get(i).select("p[class=day] > span").text().replace("/ ", "/").split(" ");			 //���
					conditions = content.get(i).select("p[class=temp_s] > em[class=cond]").text().split(" ");			 //�Ѯ𪬪p
					temperatures = content.get(i).select("p[class=temp_s] > span").text().replace(" /", "/").split(" "); //���
					break;
				}
			}
			
			responseMessage = String.format("�i�d�߿����G%s�j\r\n", condition);
			//�d�ߪ��������s�b
			if(dates == null || conditions == null || temperatures == null) {
				responseMessage += "�d�L�������I";
				break;
			}
			//format�^�ǰT��
			for (int j = 0; j < dates.length; j++) {
				responseMessage += String.format("%s\t%s\t%s\r\n", dates[j], conditions[j], temperatures[j]);
			}
			break;
			
		case "AIR": //�d�ߤ���Ů�~��@(��ƨӷ��G��F�|���O�p���Ҹ�ƶ}�񥭻O�@https://data.epa.gov.tw/)
			//https://data.epa.gov.tw/api/v1/aqx_p_432?offset={���L����}&limit={��n�����}&api_key={�|��API���_}
			URL url = new URL("https://data.epa.gov.tw/api/v1/aqx_p_432?offset=0&limit=100&api_key={api_key}");
			URLConnection connection = url.openConnection(); //�ШD�s�u
			InputStream stream = connection.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			
			String jsonString = new String(), inputLine = new String();
			//���o�Ҧ����e
			while((inputLine = bufferedReader.readLine()) != null) {
				jsonString += inputLine;
			}
			
			JSONObject jsonObject = new JSONObject(jsonString);
			JSONArray airQualityIndexArray = new JSONArray(jsonObject.get("records").toString()); //���oAQI�Ҧ��˴����
			
			responseMessage = String.format("�i�d�߿����G%s�j\r\n", condition);
			for (int i = 0; i < airQualityIndexArray.length(); i++) {
				JSONObject airQualityIndexInfo = airQualityIndexArray.getJSONObject(i); //���XJSON����
				//�P�_�d�߿����O�_�۲�
				if (airQualityIndexInfo.getString("County").equals(condition)) {
					responseMessage += "�����W��:" + airQualityIndexInfo.getString("SiteName") + "�@�@";
					responseMessage += "����:" + airQualityIndexInfo.getString("County") + "�@�@";
					responseMessage += "�Ů�~�����:" + airQualityIndexInfo.getString("AQI") + "�@�@";
					responseMessage += "���A:" + airQualityIndexInfo.getString("Status") + "�@�@";
					responseMessage += "�Ů𦾬V���Ъ�:" + airQualityIndexInfo.getString("Pollutant") + "�@�@";
					responseMessage += "��ƫظm���:" + airQualityIndexInfo.getString("PublishTime") + "\r\n";
				}
			}
			break;
			
		case "NEWS": //�d�ߧY�ɷs�D �@(��ƨӷ��G�p�X�s�D���@https://udn.com/news/breaknews/1)
			Document newsDocument = Jsoup.connect("https://udn.com/news/breaknews/1").get(); //�ШD�s�u
			Elements newsInfo = newsDocument.select("main > div > section[class=wrapper-left] > section[id=breaknews]"
													+ " > div[class=context-box__content story-list__holder story-list__holder--full]"
													+ "> div[class=story-list__news] > div[class=story-list__image] > a"); //�ѪR
			for (Element element : newsInfo) {
				String newsTitle = element.attr("aria-label");
				String newsUrl = element.attr("href");
				responseMessage += String.format("�u%s�v\t�]https://udn.com/%s�^\r\n", newsTitle, newsUrl);
			}
			break;
			
		default:
			responseMessage = "��J�����O���~�I�Э��s��J�C";
			break;
		}

		return responseMessage;
	}
}
