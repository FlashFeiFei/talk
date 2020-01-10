package ly.talk.server.room;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ly.talk.server.user.User;

public class Room {
	// 用户列表
	private Map<String, User> userList = new ConcurrentHashMap<String, User>();
	// 1024个消息
	private volatile Queue<String> transpondMessage = new LinkedList<String>();
	// 锁
	private final Lock lock = new ReentrantLock();

	/**
	 * 加入房间 因为我的房间列表用的是map，可能会出现哈希冲突 如果出现哈希冲突，直接断开这个用户，不让它连接进来
	 * 
	 * @param u
	 */
	public boolean joinRoom(User u) {

		if (!u.isFristJoin() && !this.userList.containsKey(u.getUserId())) {
			// map key存在，不允许加入房间
			u.closeSocket();

			return false;
		}

		System.out.println(u.getUserId() + "加入房间");
		this.userList.put(u.getUserId(), u);

		return true;
	}

	/**
	 * 不会出现资源竞争，不需要锁
	 * 
	 * @param u
	 */
	public void leaveRoom(User u) {

		System.out.println("用户:" + u.getUserId() + "离开房间");
		if (!this.userList.containsKey(u.getUserId())) {
			// 用户已经离开
			return;
		}

		// 删除用户
		this.userList.remove(u.getUserId());
		u.closeSocket();
		System.out.println("用户:" + u.getUserId() + "成功离开房间");
		// 释放空间
		u = null;
		return;
	}

	/**
	 * 接受信息 多个线程同时对队列插入消息，为了防止消息丢失，加个锁
	 * 
	 * @param message
	 */
	public boolean receiveMessageFromUser(String message) {
		this.lock.lock();
		boolean receiveResule = false;
		try {
			this.transpondMessage.add(message);
			receiveResule = true;
		} catch (IllegalStateException e) {
			// TODO: handle exception
			System.out.println("队列满了");
		} finally {
			this.lock.unlock();
		}
		return receiveResule;
	}

	/**
	 * 消息通知给用户 只有一个线程操作，不会出现资源竞争，不需要加锁
	 */
	public void notifyMessage() {
		while (true) {
			
			String message = this.transpondMessage.poll();
//			System.out.println("11111111");
			if (message == null) {
				continue;
			}
//			System.out.println("22222222");
//			System.out.println(message);
//			System.out.println(this.userList.size());
			this.userList.forEach((uid, user) -> {
				user.receiveMessageFromRoom(message);
			});
		}
	}
}
