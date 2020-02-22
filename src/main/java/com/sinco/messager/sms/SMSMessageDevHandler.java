package com.sinco.messager.sms;

import org.nutz.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinco.messager.CustomMessageHandler;
import com.sinco.messager.MessageDelegate;

/**
 * 开发调用，不真实发送短信
 * @author james
 *
 */
public class SMSMessageDevHandler implements CustomMessageHandler<AlidayuMessage>{
	private static final Logger logger = LoggerFactory.getLogger(SMSMessageDevHandler.class);
	
	@Override
	public void listenerMessage(MessageDelegate delegate, String channel, int threadNum) {}
	@Override
	public void listenerMessage(MessageDelegate delegate, String channel) {}
	@Override
	public void shutdownListenerAll() {}
	@Override
	public void shutdownListener(String channel) {}

	@Override
	public boolean sendMessage(String channel, String... message) {
		logger.info("dev send message channel[{}],message[{}]",channel,message);
		return true;
	}

	@Override
	public boolean sendMessage(String channel, byte[] message) {
		logger.info("dev send message channel[{}],message[{}]",channel,new String(message));
		return true;
	}

	@Override
	public boolean sendMessage(String channel,AlidayuMessage message) {
		logger.info("dev send message channel[{}],message[{}]",channel,Json.toJson(message));
		return true;
	}
	
	@Override
	public SMSResult sendTemplateMessage(String channel, AlidayuMessage message) {
		logger.info("dev send message channel[{}],message[{}]",channel,Json.toJson(message));
		return new SMSResult();
	}
}