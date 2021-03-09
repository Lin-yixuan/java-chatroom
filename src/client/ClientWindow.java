package client;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class ClientWindow extends JFrame {
	
	/* ┌───────────────────────┐
	 * │　　           TopPanel　　               │
	 * │-----------------------│
	 * │                       │
	 * │     ControlPanel      │
	 * │                       │
	 * │-----------------------│
	 * │　　     BottomPanel　　             │
	 * └───────────────────────┘
	 */

	private JPanel controlPanel;
	private JTextArea chatTextArea, queryTextArea;
	private JTextField inputField;
	Boolean isChatting = true; //預設為聊天室
	
	private static Client client;
	
	public static void main(String[] args) {
		//開啟視窗
		ClientWindow clientFrame = new ClientWindow();
		clientFrame.setTitle("聊天室與資料查詢");
		clientFrame.setSize(800, 600);
		clientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		clientFrame.setVisible(true);
		//開啟連線
		client = new Client(clientFrame);
		client.start();
	}
	
	public ClientWindow() {
		init(); 
		add(getTopPanel(), BorderLayout.NORTH);
		add(getBottomPanel(), BorderLayout.SOUTH);
		controlPanel.setLayout(new BorderLayout());
		controlPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		controlPanel.add(getChatRoomPanel(), BorderLayout.CENTER);
		add(controlPanel);	
	}
	
	/** 
	 * Initialize
	 */
	private void init() {
		controlPanel = new JPanel();
		inputField = new JTextField();
		chatTextArea = new JTextArea();
		queryTextArea = new JTextArea();
		chatTextArea.setMargin(new Insets(5, 5, 5, 5));
		queryTextArea.setMargin(new Insets(5, 5, 5, 5));
	}
	
	/**
	 * 取得上方區塊畫面
	 */
	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel();
		topPanel.setBorder(new EmptyBorder(15, 15, 0, 15));
		
		JLabel modelLabel = new JLabel("模式：");
		
		JRadioButton chatRoomRadioBtn = new JRadioButton("聊天區", true);
		chatRoomRadioBtn.setName("CHATROOM");
		JRadioButton queryDataRadioBtn = new JRadioButton("資料查詢", false);
		queryDataRadioBtn.setName("QUERYDATA");
		//註冊Radio Button監聽事件
		chatRoomRadioBtn.addItemListener(new SwitchPanelRadioBtnHandler());
		queryDataRadioBtn.addItemListener(new SwitchPanelRadioBtnHandler());
		
		ButtonGroup modelGroupBtn = new ButtonGroup();
		modelGroupBtn.add(chatRoomRadioBtn);
		modelGroupBtn.add(queryDataRadioBtn);
		
		topPanel.add(modelLabel);
		topPanel.add(chatRoomRadioBtn);
		topPanel.add(queryDataRadioBtn);
		
		return topPanel;
	}
	
	/**
	 * 取得下方區塊畫面
	 */
	private JPanel getBottomPanel() {
		JPanel bottomPanel = new JPanel();
		bottomPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		JButton sendBtn = new JButton("傳送");
		sendBtn.addActionListener(new SendBtnHandler());
		inputField.setColumns(60);
		
		bottomPanel.add(inputField, BorderLayout.CENTER);
		bottomPanel.add(sendBtn, BorderLayout.EAST);
		
		return bottomPanel;
	}
	
	/**
	 * 取得聊天室畫面 
	 */
	private JPanel getChatRoomPanel() {		
		chatTextArea.setLineWrap(true); 		//自動換行
		chatTextArea.setWrapStyleWord(true); 	//換行不斷字
		chatTextArea.setEditable(false); 		//不能編輯
		
		JPanel chatRoomPanel = new JPanel();
		chatRoomPanel.setLayout(new BorderLayout());
		chatRoomPanel.add(new JScrollPane(chatTextArea), BorderLayout.CENTER);
		
		return chatRoomPanel;
	}
	
	/**
	 * 設定資料查詢畫面 
	 */
	private JPanel getQueryDataPanel() {
		queryTextArea.setLineWrap(true); 		//自動換行
		queryTextArea.setWrapStyleWord(true); 	//換行不斷字
		queryTextArea.setEditable(false); 		// 不能編輯
		
		JPanel queryDataPanel = new JPanel();
		queryDataPanel.setLayout(new BorderLayout());
		queryDataPanel.add(new JScrollPane(queryTextArea), BorderLayout.CENTER);
		
		return queryDataPanel;
	}
	
	/**
	 * 聊天區、資料查詢畫面切換 
	 */
	private void switchPanel(JPanel showPanel) {
		controlPanel.removeAll();
		controlPanel.add(showPanel);
		controlPanel.validate();
		controlPanel.repaint();
	}
	
	/**
	 * 加入訊息至TextArea
	 * @param isChat 是否聊天
	 * @param message
	 */
	public void appendToTextArea(Boolean isChat, String message) {
		if(isChat) {
			chatTextArea.append(message + "\r\n");
		} else {
			queryTextArea.append(message + "\r\n");
		}
	}
	
	/**
	 * 控制聊天室、資料查詢JTextArea Panel交換事件
	 */
	private class SwitchPanelRadioBtnHandler implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			JRadioButton tmpRadioBtn = (JRadioButton) e.getSource();
			if(tmpRadioBtn.isSelected()) {
				switch(tmpRadioBtn.getName()) {
				case "CHATROOM":  //聊天室
					switchPanel(getChatRoomPanel());
					isChatting = true;
					break;
				case "QUERYDATA": //資料查詢
					switchPanel(getQueryDataPanel());
					isChatting = false;
					break;
				}
			}
		}
	}
	
	/**
	 * 傳送按鈕監聽事件
	 */
	private class SendBtnHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String inputMessage = inputField.getText();
			appendToTextArea(isChatting, String.format("（我）：%s", inputMessage));
			client.sendMessage(isChatting, inputMessage); //將訊息傳送給Server
			inputField.setText(null); //清空輸入欄位
		}
	}
}
