package com.sinco.messager.redis;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinco.messager.MessageDelegate;
import com.sinco.messager.PubsubMessageHandler;
import com.sinco.messager.redis.JedisHolder.RedisCallback;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Protocol;

/**
 * redis 广播消息
 * @author james
 *
 */
public class RedisPubsubMessageHandler implements PubsubMessageHandler{

	private JedisHolder jedisHolder=JedisHolder.singleton();
	
	private JedisPool jp;
	
	private final Logger log=LoggerFactory.getLogger(this.getClass());
	
	private Map<String, JedisPubSub> channelMap=new HashMap<>();
	
	public RedisPubsubMessageHandler(String redisHost, Integer redisPort,Integer dbIndex) {
		jp =jedisHolder.getJedisPoolInstance(redisHost, redisPort, Protocol.DEFAULT_TIMEOUT, null, dbIndex);
	}
	
	public RedisPubsubMessageHandler(JedisPool jp) {
		this.jp = jp;
		if(jp == null){
			throw new RuntimeException("JedisPool is null");
		}
	}
	
	@Override
	public boolean sendMessage(final String channel,final String ... message){
		JedisHolder.execute(jp, new RedisCallback<String>() {
			@Override
			public String doInRedis(Jedis jedis) {
				for (String msg : message) {
					jedis.publish(channel, msg);
				}
				return null;
			}
		});
		return true;
	}
	
	
	@Override
	public void listenerMessage(final MessageDelegate delegate, String channel) {
		if(channelMap.containsKey(channel)){
			log.error("channel [{}] 已经添加监听，无法重复监听",channel);
			return;
		}
		JedisPubSub pubsub=new JedisPubSub() {
			@Override
			public void onUnsubscribe(String channel, int subscribedChannels) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onSubscribe(String channel, int subscribedChannels) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onPUnsubscribe(String pattern, int subscribedChannels) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onPSubscribe(String pattern, int subscribedChannels) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onPMessage(String pattern, String channel, String message) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onMessage(String channel, String message) {
				delegate.handleMessage(message, channel);
			}
		};
		Thread thread=new MyThread(pubsub,channel);
		channelMap.put(channel, pubsub);
		thread.start();
	}
	
	public class MyThread extends Thread{
		private JedisPubSub pubsub;
		private String channel;
		private Jedis jedis;
		
		public MyThread( JedisPubSub pubsub, String channel){
			this.pubsub=pubsub;
			this.channel=channel;
			jedis=jp.getResource();
		}
		
	    @Override
	    public void run() {
	    	
	    	log.debug("{} channel listener run,thread:{}",channel,Thread.currentThread().getName());
	    	
	    	jedis.subscribe(pubsub, channel);
	    }
	    
	    public void interrupt() {
	    	super.interrupt();
	    	jp.returnResource(jedis);
	    }
	}
	

	@Override
	public void shutdownListenerAll() {
		for (Map.Entry<String, JedisPubSub> entry : channelMap.entrySet()) {
			entry.getValue().unsubscribe(entry.getKey());
		}
		channelMap.clear();
	}
	
	public void shutdownListener(String channel) {
		JedisPubSub pubsub=channelMap.remove(channel);
		if(pubsub != null){
			pubsub.unsubscribe(channel);
		}
	}

	@Override
	public boolean sendMessage(String channel, byte[] message) {
		return sendMessage(channel, new String(message));
	}

}
