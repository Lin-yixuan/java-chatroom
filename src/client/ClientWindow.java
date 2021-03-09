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
	
	/* �z�w�w�w�w�w�w�w�w�w�w�w�w�w�w�w�w�w�w�w�w�w�w�w�{
	 * �x�@�@           TopPanel�@�@               �x
	 * �x-----------------------�x
	 * �x                       �x
	 * �x     ControlPanel      �x
	 * �x                       �x
	 * �x-----------------------�x
	 * �x�@�@     BottomPanel�@�@             �x
	 * �|�w�w�w�w�w�w�w�w�w�w�w�w�w�w�w�w�w�w�w�w�w�w�w�}
	 */

	private JPanel controlPanel;
	private JTextArea chatTextArea, queryTextArea;
	private JTextField inputField;
	Boolean isChatting = true; //�w�]����ѫ�
	
	private static Client client;
	
	public static void main(String[] args) {
		//�}�ҵ���
		ClientWindow clientFrame = new ClientWindow();
		clientFrame.setTitle("��ѫǻP��Ƭd��");
		clientFrame.setSize(800, 600);
		clientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		clientFrame.setVisible(true);
		//�}�ҳs�u
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
	 * ���o�W��϶��e��
	 */
	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel();
		topPanel.setBorder(new EmptyBorder(15, 15, 0, 15));
		
		JLabel modelLabel = new JLabel("�Ҧ��G");
		
		JRadioButton chatRoomRadioBtn = new JRadioButton("��Ѱ�", true);
		chatRoomRadioBtn.setName("CHATROOM");
		JRadioButton queryDataRadioBtn = new JRadioButton("��Ƭd��", false);
		queryDataRadioBtn.setName("QUERYDATA");
		//���URadio Button��ť�ƥ�
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
	 * ���o�U��϶��e��
	 */
	private JPanel getBottomPanel() {
		JPanel bottomPanel = new JPanel();
		bottomPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		JButton sendBtn = new JButton("�ǰe");
		sendBtn.addActionListener(new SendBtnHandler());
		inputField.setColumns(60);
		
		bottomPanel.add(inputField, BorderLayout.CENTER);
		bottomPanel.add(sendBtn, BorderLayout.EAST);
		
		return bottomPanel;
	}
	
	/**
	 * ���o��ѫǵe�� 
	 */
	private JPanel getChatRoomPanel() {		
		chatTextArea.setLineWrap(true); 		//�۰ʴ���
		chatTextArea.setWrapStyleWord(true); 	//���椣�_�r
		chatTextArea.setEditable(false); 		//����s��
		
		JPanel chatRoomPanel = new JPanel();
		chatRoomPanel.setLayout(new BorderLayout());
		chatRoomPanel.add(new JScrollPane(chatTextArea), BorderLayout.CENTER);
		
		return chatRoomPanel;
	}
	
	/**
	 * �]�w��Ƭd�ߵe�� 
	 */
	private JPanel getQueryDataPanel() {
		queryTextArea.setLineWrap(true); 		//�۰ʴ���
		queryTextArea.setWrapStyleWord(true); 	//���椣�_�r
		queryTextArea.setEditable(false); 		// ����s��
		
		JPanel queryDataPanel = new JPanel();
		queryDataPanel.setLayout(new BorderLayout());
		queryDataPanel.add(new JScrollPane(queryTextArea), BorderLayout.CENTER);
		
		return queryDataPanel;
	}
	
	/**
	 * ��ѰϡB��Ƭd�ߵe������ 
	 */
	private void switchPanel(JPanel showPanel) {
		controlPanel.removeAll();
		controlPanel.add(showPanel);
		controlPanel.validate();
		controlPanel.repaint();
	}
	
	/**
	 * �[�J�T����TextArea
	 * @param isChat �O�_���
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
	 * �����ѫǡB��Ƭd��JTextArea Panel�洫�ƥ�
	 */
	private class SwitchPanelRadioBtnHandler implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			JRadioButton tmpRadioBtn = (JRadioButton) e.getSource();
			if(tmpRadioBtn.isSelected()) {
				switch(tmpRadioBtn.getName()) {
				case "CHATROOM":  //��ѫ�
					switchPanel(getChatRoomPanel());
					isChatting = true;
					break;
				case "QUERYDATA": //��Ƭd��
					switchPanel(getQueryDataPanel());
					isChatting = false;
					break;
				}
			}
		}
	}
	
	/**
	 * �ǰe���s��ť�ƥ�
	 */
	private class SendBtnHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String inputMessage = inputField.getText();
			appendToTextArea(isChatting, String.format("�]�ڡ^�G%s", inputMessage));
			client.sendMessage(isChatting, inputMessage); //�N�T���ǰe��Server
			inputField.setText(null); //�M�ſ�J���
		}
	}
}
