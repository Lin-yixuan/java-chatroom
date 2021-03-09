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
	private boolean isExistingName = false; //預設使用者名稱不存在
	private boolean userWantToLeave = false; //預設使用者不離開

	public Connection(SuperSocket superSocket, ConnectionService connectionService) {
		this.superSocket = superSocket;
		this.connectionService = connectionService;
	}

	@Override
	public void run() {
		String newUsername = new String(), connectedMsg = new String();

		while (!isExistingName) {
			try {
				//接收使用者名稱
				newUsername = (String) superSocket.getObjectInputStream().readObject();

				//判斷使用者名稱是否已重複
				if (!connectionService.addConnection(newUsername, superSocket)) {
					connectedMsg = "DUPLICATENAME"; // 使用者名稱已重複
				} else {
					connectedMsg = "【" + newUsername + "已連線成功！】";
					username = newUsername;
					isExistingName = true;
				}
				superSocket.getObjectOutputStream().writeObject(connectedMsg);
				superSocket.getObjectOutputStream().flush();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				close(); //關閉連線
				break;
			}
		}

		//廣播給所有在線使用者有新使用者加入的訊息
		String newUserMessage = String.format("【%s已加入聊天室】", newUsername);
		broadcast(newUsername, newUserMessage);

		while (!userWantToLeave) {
			try {
				String receivedMessage = (String) superSocket.getObjectInputStream().readObject();
				String[] splitMessage = receivedMessage.split("#"); //訊息格式：狀態#訊息
				processReceivedMessage(splitMessage[0], newUsername, splitMessage[1]);
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				//使用者離開聊天室
				userWantToLeave = true;
				String leaveMessage = String.format("【%s已離開聊天室】", username);
				broadcast(username, leaveMessage); //廣播離開訊息
				close(); //關閉連線
				connectionService.deleteConnection(username); //移除使用者
			}
		}

	}

	/**
	 * 關閉連線
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
	 * 廣播聊天室訊息
	 * @param senderName 寄件者名稱
	 * @param sendMessage 寄送訊息
	 */
	private void broadcast(String senderName, String sendMessage) {
		String message = "CHAT#" + sendMessage;
		
		//重新更新一次連線清單
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
	 * 依據狀態處理收到的訊息
	 * @param state 狀態 (CHAT-聊天, QUERY-資料查詢)
	 * @param senderName 寄送者名稱
	 * @param message 寄送訊息
	 * @throws IOException
	 */
	private void processReceivedMessage(String state, String senderName, String message) throws IOException {
		switch (state) {
		case "CHAT": //聊天
			broadcast(senderName, String.format("%s：%s", senderName, message));
			break;
		case "QUERY": //資料查詢
			String responseMessage;

			//若message中包含@則表示有查詢條件
			if (message.contains("@")) {
				String[] splitMessage = message.split("@");
				responseMessage = String.format("QUERY#Server：\r\n%s", processQueryData(splitMessage[0], splitMessage[1]));
			} else {
				responseMessage = String.format("QUERY#Server：\r\n%s", processQueryData(message, null));
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
	 * 依據使用者輸入的指令，回傳查詢結果
	 * @param command 指令
	 * @param condition 查詢條件
	 * @return responseMessage 查詢結果
	 * @throws IOException
	 */
	private String processQueryData(String command, String condition) throws IOException {
		String responseMessage = new String();
		
		switch (command.toUpperCase()) {
		case "?": //查詢操作明細
			responseMessage = "　- 查詢某縣市未來一週天氣，請輸入[weather@欲查詢縣市]\r\n" +
							  "　- 查詢某縣市今日空氣品質，請輸入[air@欲查詢縣市]\r\n" +
							  "　- 查詢即時新聞，請輸入[news]\r\n" +
							  "　範例：輸入「weather#臺北市」即可查詢臺北市未來一週的天氣";
			break;
			
		case "WEATHER": //查詢未來一週天氣　(資料來源：PChome新聞　http://news.pchome.com.tw/weather/taiwan)
			Document document = Jsoup.connect("http://news.pchome.com.tw/weather/taiwan").get(); //請求連線
			Elements content = document.select("div#container > div#cont-area > div > div.wth_chart_area > div > section"); //解析
			String[] dates = null, conditions = null, temperatures = null;
			
			for (int i = 0; i < content.size(); i++) {
				if (content.get(i).select("div").text().equals(condition)) {
					dates = content.get(i).select("p[class=day] > span").text().replace("/ ", "/").split(" ");			 //日期
					conditions = content.get(i).select("p[class=temp_s] > em[class=cond]").text().split(" ");			 //天氣狀況
					temperatures = content.get(i).select("p[class=temp_s] > span").text().replace(" /", "/").split(" "); //氣溫
					break;
				}
			}
			
			responseMessage = String.format("【查詢縣市：%s】\r\n", condition);
			//查詢的縣市不存在
			if(dates == null || conditions == null || temperatures == null) {
				responseMessage += "查無此縣市！";
				break;
			}
			//format回傳訊息
			for (int j = 0; j < dates.length; j++) {
				responseMessage += String.format("%s\t%s\t%s\r\n", dates[j], conditions[j], temperatures[j]);
			}
			break;
			
		case "AIR": //查詢今日空氣品質　(資料來源：行政院環保署環境資料開放平臺　https://data.epa.gov.tw/)
			//https://data.epa.gov.tw/api/v1/aqx_p_432?offset={跳過筆數}&limit={取n筆資料}&api_key={會員API金鑰}
			URL url = new URL("https://data.epa.gov.tw/api/v1/aqx_p_432?offset=0&limit=100&api_key={api_key}");
			URLConnection connection = url.openConnection(); //請求連線
			InputStream stream = connection.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			
			String jsonString = new String(), inputLine = new String();
			//取得所有內容
			while((inputLine = bufferedReader.readLine()) != null) {
				jsonString += inputLine;
			}
			
			JSONObject jsonObject = new JSONObject(jsonString);
			JSONArray airQualityIndexArray = new JSONArray(jsonObject.get("records").toString()); //取得AQI所有檢測資料
			
			responseMessage = String.format("【查詢縣市：%s】\r\n", condition);
			for (int i = 0; i < airQualityIndexArray.length(); i++) {
				JSONObject airQualityIndexInfo = airQualityIndexArray.getJSONObject(i); //取出JSON物件
				//判斷查詢縣市是否相符
				if (airQualityIndexInfo.getString("County").equals(condition)) {
					responseMessage += "測站名稱:" + airQualityIndexInfo.getString("SiteName") + "　　";
					responseMessage += "縣市:" + airQualityIndexInfo.getString("County") + "　　";
					responseMessage += "空氣品質指數:" + airQualityIndexInfo.getString("AQI") + "　　";
					responseMessage += "狀態:" + airQualityIndexInfo.getString("Status") + "　　";
					responseMessage += "空氣汙染指標物:" + airQualityIndexInfo.getString("Pollutant") + "　　";
					responseMessage += "資料建置日期:" + airQualityIndexInfo.getString("PublishTime") + "\r\n";
				}
			}
			break;
			
		case "NEWS": //查詢即時新聞 　(資料來源：聯合新聞網　https://udn.com/news/breaknews/1)
			Document newsDocument = Jsoup.connect("https://udn.com/news/breaknews/1").get(); //請求連線
			Elements newsInfo = newsDocument.select("main > div > section[class=wrapper-left] > section[id=breaknews]"
													+ " > div[class=context-box__content story-list__holder story-list__holder--full]"
													+ "> div[class=story-list__news] > div[class=story-list__image] > a"); //解析
			for (Element element : newsInfo) {
				String newsTitle = element.attr("aria-label");
				String newsUrl = element.attr("href");
				responseMessage += String.format("「%s」\t（https://udn.com/%s）\r\n", newsTitle, newsUrl);
			}
			break;
			
		default:
			responseMessage = "輸入的指令錯誤！請重新輸入。";
			break;
		}

		return responseMessage;
	}
}
