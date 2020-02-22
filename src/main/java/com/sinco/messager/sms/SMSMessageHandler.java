package com.sinco.messager.sms;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

import com.sinco.messager.MessageDelegate;
import com.sinco.messager.MessageHandler;
import com.sinco.messager.redis.JedisHolder;
import com.sinco.messager.redis.JedisHolder.RedisCallback;

public class SMSMessageHandler  implements MessageHandler{

	private String sn;
	
	private  String pwd;
	
	
	private static String SMS_SNED_TOTAL="SMS_SNED_TOTAL";

	private static String td="【尚初.美奂生活】";

	private static final String CHARSET = "utf-8";
	
	private static final int dbIndex=8;
	
	private static final Logger log = LoggerFactory.getLogger(SMSMessageHandler.class);
	
	private JedisHolder jedisHolder=JedisHolder.singleton();
	
	private JedisPool jp;
	
	
	public SMSMessageHandler(String redisHost, Integer redisPort,String sn,String pwd) {
		jp =jedisHolder.getJedisPoolInstance(redisHost, redisPort, Protocol.DEFAULT_TIMEOUT, null, dbIndex);
		this.sn=sn;
		this.pwd=pwd;
	}

	/**
	 * 记录sms send 数量 
	 * @return
	 */
	private Integer incrInt() {
		return JedisHolder.execute(jp, new RedisCallback<Integer>() {
			@Override
			public Integer doInRedis(Jedis jedis) {
				 jedis.incr(SMS_SNED_TOTAL).intValue();
				return null;
			}
		});
	}
	
	public boolean sendMessage(String channel, String... message) {
		Client client;
		try {
			client = new Client(sn,pwd);
			if (StringUtils.isNotBlank(channel) && message.length > 0 && StringUtils.isNotBlank(message[0])) {
				String content = message[0]+"【尚初.美奂生活】";
				//我们的Demo最后是拼成xml了，所以要按照xml的语法来转义
				if(content.indexOf("&")>=0) {
					content=	content.replace("&","&amp;");
				}
				
				if(content.indexOf("<")>=0) {
					
				content=	content.replace("<","&lt;");
					
				}
				if(content.indexOf(">")>=0) {
					content=	content.replace(">","&gt;");
				}
				
				//短信发送		
				String result_mt = client.mt(channel, content, "", "", "");
				if(result_mt.startsWith("-")||result_mt.equals(""))//以负号判断是否发送成功
				{
					log.error("发送失败！返回值为："+result_mt+"。请查看webservice返回值对照表");
					return false;
				}
				//输出返回标识，为小于19位的正数，String类型的，记录您发送的批次
				else{
					log.error("发送成功，返回值为："+result_mt);
					return true;
				}
			}
		} catch (UnsupportedEncodingException e) {
			log.error("",e);
		}
		return false;
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
		// TODO Auto-generated method stub
	}

	@Override
	public void shutdownListener(String channel) {
		
	}
		// TODO Auto-generated method stub
	
	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	@Override
	public boolean sendMessage(String channel, byte[] message) {
		return sendMessage(channel, new String(message));
	}

}
