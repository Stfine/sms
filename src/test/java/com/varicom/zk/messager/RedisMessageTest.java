//package com.varicom.zk.messager;
//
//import org.junit.Test;
//
//import com.sinco.messager.MessageDelegate;
//import com.sinco.messager.MessageHandler;
//import com.sinco.messager.redis.RedisMessageHandler;
//
//public class RedisMessageTest {
//
//	private MessageHandler messageHandler=new RedisMessageHandler("172.16.1.2", 6379, 8);
//	
//	@Test
//	public void test() throws InterruptedException{
//		messageHandler.listenerMessage(new MessageDelegate() {
//			@Override
//			public void handleMessage(String message, String channel) {
//				System.out.println(message);
//			}
//		}, "redis-message",5);
//		
//		for (int i = 0; i < 2; i++) {
//			messageHandler.sendMessage("redis-message", "test 11111");
//			messageHandler.sendMessage("redis-message", "test 22222");
//			messageHandler.sendMessage("redis-message", "test 333333");
//			messageHandler.sendMessage("redis-message", "test 4444444");
//			messageHandler.sendMessage("redis-message", "test 6777777");
//			Thread.currentThread().sleep(1);
//		}
//
//		messageHandler.shutdownListenerAll();
//		
//	}
//	
//}
