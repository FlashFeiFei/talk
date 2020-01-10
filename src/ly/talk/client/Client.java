package ly.talk.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
	// 读取的buffersize大小
	static final int ReaderWriterBufferSize = 1024;
	// 读写的io的buffersize大小
	static final int IOBufferSize = ReaderWriterBufferSize * 4;

	public static void main(String[] args) throws IOException {
		Socket sock = new Socket("localhost", 6666); // 连接指定服务器和端口
		try (InputStream input = sock.getInputStream()) {
			try (OutputStream output = sock.getOutputStream()) {
				handle(input, output);
			}
		}
		sock.close();
		System.out.println("disconnected.");
	}

	private static void handle(InputStream input, OutputStream output) throws IOException {
		var writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
		var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
		Thread rt = new Thread() {
			@Override
			public void run() {
				char[] rb = new char[ReaderWriterBufferSize];
				StringBuilder sb = new StringBuilder(ReaderWriterBufferSize);
				while (true) {
					String resp;
					int n;
					try {
						n = reader.read(rb);
						if (n == -1) {
							continue;
						}

						for (int i = 0; i < n; i++) {
							sb.append(rb[i]);
						}

					} catch (IOException e) {
						return;
					}

					System.out.println("<<<服务器回复您 " + sb.toString());
					sb.delete(0, n);
				}
			}

		};
		rt.start();

		Scanner scanner = new Scanner(System.in);
		for (;;) {
			System.out.print(">>> "); // 打印提示
			String s = scanner.nextLine(); // 读取一行输入
			writer.write(s);
			writer.newLine();
			writer.flush();
		}
	}
}