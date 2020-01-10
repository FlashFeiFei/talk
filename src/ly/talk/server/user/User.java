package ly.talk.server.user;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import ly.talk.server.room.Room;

public class User {
	// 用户uid
	private String uid;
	private boolean fristJoin;
	// 用户的socket
	private Socket socket;
	private BufferedWriter writer;
	private BufferedReader reader;
	// 读取的buffersize大小
	static final int ReaderWriterBufferSize = 1024;
	// 读写的io的buffersize大小
	static final int IOBufferSize = ReaderWriterBufferSize * 4;
	// 用户所属房间
	private Room room;

	public User(Socket socket, Room room) throws IOException {
		this.socket = socket;
		this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8),
				IOBufferSize);
		this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8),
				IOBufferSize);
		this.room = room;
		this.uid = this.socket.toString();
		this.fristJoin = true;
	}

	public boolean isFristJoin() {
		return this.fristJoin;
	}

	public void closeSocket() {
		try {

			this.writer.close();
			this.reader.close();
			this.socket.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * 用户发送过来的信息
	 */
	public void sendMessageToRoom() {
		this.fristJoin = false;
		char[] rb = new char[ReaderWriterBufferSize];
		StringBuilder sb = new StringBuilder(ReaderWriterBufferSize);
		while (true) {
			try {
				int n = this.reader.read(rb); 
				if (n == -1) {
					continue;
				}
				
				for(int i = 0; i < n; i++) {
					sb.append(rb[i]);
				}
				this.room.receiveMessageFromUser(sb.toString());
				
				sb.delete(0, n);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				this.room.leaveRoom(this);
				this.closeSocket();
				//结束函数，为了线程能关闭
				return;
			}
		}

	}

	public void receiveMessageFromRoom(String message) {
		try {
			this.writer.write(message);
			this.writer.flush();
		} catch (IOException e) {
			// io连接断开了
			// 离开房间
			this.room.leaveRoom(this);
			// 关闭socket
			this.closeSocket();
		}
	}

	public String getUserId() {
		return this.uid;
	}
}
