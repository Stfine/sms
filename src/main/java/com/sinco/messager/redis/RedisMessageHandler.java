package com.sinco.messager.redis;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinco.messager.MessageDelegate;
import com.sinco.messager.MessageHandler;
import com.sinco.messager.redis.JedisHolder.RedisCallback;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisMessageHandler implements MessageHandler{

	private JedisHolder jedisHolder=JedisHolder.singleton();
	
	private JedisPool jp;
	
	private Map<String, ExecutorService> pools=new Hashtable<>();
	
	
	private final Logger log=LoggerFactory.getLogger(this.getClass());
	
	public RedisMessageHandler(String redisHost, Integer redisPort,Integer dbIndex) {
		jp =jedisHolder.getJedisPoolInstance(redisHost, redisPort, Protocol.DEFAULT_TIMEOUT, null, dbIndex);
	}
	
	public RedisMessageHandler(String redisHost,Integer redisPort,String password) {
		jedisHolder.getJedisPoolInstance(redisHost, redisPort, Protocol.DEFAULT_TIMEOUT, password);
	}
	
	public RedisMessageHandler(JedisPool jp) {
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
				
				jedis.lpush(channel, message);
				
				return null;
			}
		});
		return true;
	}
	
	@Override
	public void listenerMessage( MessageDelegate delegate, String channel,int threadNum){
		
		ExecutorService pool =pools.get(channel);
		
		if(pool == null){
			pool=Executors.newSingleThreadExecutor();
			pools.put(channel, pool);
		}
		
		//设置用于拉取消息的线程
		for (int i = 0; i < threadNum; i++) {
			Thread t=new MyThread(delegate,channel);
			t.setName("listenerMessage-"+channel+i);
			pool.execute(t);
		}
	}

	public class MyThread extends Thread{
		private MessageDelegate delegate;
		private String channel;
		private Jedis jedis;
		
		public MyThread( MessageDelegate delegate, String channel){
			this.delegate=delegate;
			this.channel=channel;
			jedis=jp.getResource();
		}
		
	    @Override
	    public void run() {
	    	
	    	log.debug("{} channel listener run,thread:{}",channel,Thread.currentThread().getName());
			while (true) {
				if(jedis == null){
					try {
						jedis=jp.getResource();
					} catch (Exception e) {
						log.error("Message JedisPool error",e);
					}
				}
				if(jedis == null){
					//5秒后重连
					try {
						Thread.sleep(5000);
						continue;
					} catch (InterruptedException e) {
						log.error("Message Thread.sleep error",e);
					}
				}
				
				try {
					List<String> msgs= jedis.brpop(0,channel);
					delegate.handleMessage(msgs.get(1), msgs.get(0));
				} catch (JedisConnectionException e) {
					log.error("Message listener error",e);
					//5秒后重试
					try {
						if(jedis != null){
							jp.returnBrokenResource(jedis);
						}
					}catch (JedisConnectionException e1) {
						log.error("Message redis reconnect JedisConnectionException",e1);
					}finally {
						jedis=null;
					}
				}catch (Exception e) {
					log.error("Message listener error",e);
				}
			}
	    }
	    
	    public void interrupt() {
	    	super.interrupt();
	    	jp.returnResource(jedis);
	    	jedis=null;
	    }
	}
	
	@Override
	public void listenerMessage(MessageDelegate delegate, String channel) {
		listenerMessage(delegate, channel, 1);
	}

	@Override
	public void shutdownListenerAll() {
		for (ExecutorService executor : pools.values()) {
			executor.shutdown();
		}
	}
	
	public void shutdownListener(String channel) {
		ExecutorService executor=pools.get(channel);
		if(executor != null){
			executor.shutdown();
			pools.remove(channel);
		}
		
	}

	@Override
	public boolean sendMessage(String channel, byte[] message) {
		return sendMessage(channel, new String(message));
	}

}
