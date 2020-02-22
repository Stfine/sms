package com.sinco.messager;


public interface MessageHandler {
	
	/**
	 * 发送一个消息到通道
	 * @param channel
	 * @param message
	 */
	public boolean sendMessage(final String channel,final String ... message);
	
	/**
	 * 发送一个消息到通道
	 * @param channel
	 * @param message
	 */
	public boolean sendMessage(final String channel,final byte []  message);
	
	/**
	 * 监听一个通道的消息
	 * @param channel
	 * @param message
	 */
	void listenerMessage(MessageDelegate delegate, String channel, int threadNum);
	
	/**
	 * 监听一个通道的消息
	 * @param channel
	 * @param message
	 */
	void listenerMessage(MessageDelegate delegate, String channel);
	
	/**
	 * 停止所有消息监听
	 */
	void shutdownListenerAll();

	void shutdownListener(String channel);
}
