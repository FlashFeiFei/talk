# talk
用java实现socket聊天室


# 遇到多线程变量共享问题

代码在Room.java
这个属性的定义
```
private volatile Queue<String> transpondMessage = new LinkedList<String>();
```

以及这里的应用

```
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
```


总结一下 ： volatile

volatile修饰的变量，可以再多线程之间，立刻更新，但是不能保证原子性。

如果volatile修饰的变量，再加上get方法和set锁的应用，可以保证原子性，并且多个线程可以立马看到，但是这样做的性能那就更更更低了
还不如直接用线程安全的库。

那为什么我这次编写还volatile和锁都用上呢？
 因为我编写的这个聊天室模型有点特殊， set方法保证多线程原子性操作就行，
 而对于get方法，只有一个线程读取这个队列，没有出现资源竞争，
 所以再get方法不需要加上锁，也能保证原子性（头部弹出），只需要保证set再多线程添加的数据能立马更新就行，
 所以get方法不需要加锁，但是要有volatile修饰