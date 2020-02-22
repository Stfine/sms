package com.sinco.messager;

import com.sinco.messager.sms.SMSResult;

/**
 * 自定义消息体接口
 * @author james
 *
 */
public interface CustomMessageHandler<T extends Message> extends MessageHandler{
	
	/**
	 * 发送一个消息到通道
	 * @param channel
	 * @param message
	 */
	public boolean sendMessage(final String channel,final T message);
	
	/**
	 * 发送一个消息到通道
	 * @param channel
	 * @param message
	 */
	public SMSResult sendTemplateMessage(final String channel,final T message);

}
