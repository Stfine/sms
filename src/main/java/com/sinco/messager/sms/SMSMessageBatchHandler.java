package com.sinco.messager.sms;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinco.messager.MessageDelegate;
import com.sinco.messager.MessageHandler;
import com.sinco.messager.redis.RedisMessageHandler;

public class SMSMessageBatchHandler  implements MessageHandler{
	
	private static final int dbIndex=8;
	
	private static final Logger log = LoggerFactory.getLogger(SMSMessageBatchHandler.class);
	
	private static final String MESSAGE_LIST_KEY="MESSAGE_LIST_KEY";
	
	private MessageHandler redisMessageHandler=null;
	
	private static final String datePattern="yyyyMMddHHmmss";
	
	
	public SMSMessageBatchHandler(List<MessageHandler> messageHandlers,
			int threadNum,String redisHost, Integer redisPort) {
		redisMessageHandler=new RedisMessageHandler(redisHost, redisPort, dbIndex);
		
		for (final MessageHandler handler : messageHandlers) {
			redisMessageHandler.listenerMessage(new MessageDelegate() {
				@Override
				public void handleMessage(String message, String channel) {
					
					String [] strArray=message.split(":");
					if(strArray.length < 3){
						return;
					}
					
					try {
						//抛弃 4分钟之前的
						Date date=DateUtils.parseDate( strArray[1],datePattern);
						Calendar now=Calendar.getInstance();
						now.add(Calendar.MINUTE, -4);
						
						if(date.before(now.getTime())){
							return;
						}
						
					} catch (ParseException e) {
						//如果时间出错，抛弃
						return;
					}
					String mobile=strArray[0];
					
					handler.sendMessage(mobile, StringUtils.join(Arrays.
							copyOfRange(strArray, 2, strArray.length),":"));
				}
			},MESSAGE_LIST_KEY,threadNum);
		}
	}
	
	public boolean sendMessage(String channel, String... message) {
		StringBuilder value=new StringBuilder().append(channel).
				append(":").append(DateFormatUtils.format(new Date(), datePattern)).
				append(":").append(message[0]);
		
		redisMessageHandler.sendMessage(MESSAGE_LIST_KEY, value.toString());
		
		return true;
	}
	
	public static void main(String[] args) {
		List<MessageHandler> handlers=new ArrayList<MessageHandler>();
		handlers.add(new SMSMessage2Handler
				("120.24.68.189", 6380,"",""));
		SMSMessageBatchHandler sms=new SMSMessageBatchHandler(handlers, 1,
				"120.24.68.189", 6380);
		sms.sendMessage("18575541919", "您的验证码是：121312。请不要把验证码泄露给其他人。");
	}
	
	@Override
	public void listenerMessage(MessageDelegate delegate, String channel,
			int threadNum) {
		// TODO Auto-generated method stub
	}

	@Override
	public void listenerMessage(MessageDelegate delegate, String channel) {
		// TODO Auto-generated method stub
	}

	@Override
	public void shutdownListenerAll() {
	}

	@Override
	public void shutdownListener(String channel) {
		
	}

	@Override
	public boolean sendMessage(String channel, byte[] message) {
		return sendMessage(channel, new String(message));
	}
}
