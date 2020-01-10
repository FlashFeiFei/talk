package ly.talk.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ly.talk.server.room.Room;
import ly.talk.server.user.User;

public class Main {

	public static void main(String[] args) throws IOException {
		ServerSocket ss = new ServerSocket(6666); // 监听指定端口
		System.out.println("server is running...");
		// 创建一个房间，启动房间转发信息
		Room room = new Room();
		Thread roomt = new Thread() {
			@Override
			public void run() {
				room.notifyMessage();
			}

		};
		roomt.start();

		// 创建固定大小的线程池:
		ExecutorService executor = Executors.newFixedThreadPool(5);

		for (;;) {
			Socket sock = ss.accept();
			System.out.println("connected from " + sock.getRemoteSocketAddress());
			User user;
			try {

				user = new User(sock, room);
			} catch (IOException e) {
				sock.close();
				continue;
			}

			room.joinRoom(user);
			executor.submit(() -> {
				user.sendMessageToRoom();
			});

		}
	}

}
