package com.sinco.messager.sms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

import com.sinco.messager.MessageDelegate;
import com.sinco.messager.MessageHandler;
import com.sinco.messager.redis.JedisHolder;
import com.sinco.messager.redis.JedisHolder.RedisCallback;
import com.sinco.messager.util.WebUtils;

public class SMSMessage2Handler  implements MessageHandler{

	private static String url = "http://106.ihuyi.cn/webservice/sms.php?method=Submit";

	private static String SMS_KEY_PREFIX="SMS";

	private static final String CHARSET = "utf-8";
	
	private static final int dbIndex=8;
	
	private static final Logger log = LoggerFactory.getLogger(SMSMessage2Handler.class);
	
	private JedisHolder jedisHolder=JedisHolder.singleton();
	
	private JedisPool jp;
	
	private String userName;
	
	private String pwd;
	
	public SMSMessage2Handler(String redisHost, Integer redisPort,String userName,String pwd) {
		this.userName=userName;
		this.pwd=pwd;
		jp =jedisHolder.getJedisPoolInstance(redisHost, redisPort, Protocol.DEFAULT_TIMEOUT, null, dbIndex);
	}

	private static final char hexDigits[] = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	
	/**
	 * 记录sms send 数量 
	 * @return
	 */
	private Integer incrInt() {
		return JedisHolder.execute(jp, new RedisCallback<Integer>() {
			@Override
			public Integer doInRedis(Jedis jedis) {
				 jedis.incr(makeKey("SMS_SNED_TOTAL")).intValue();
				return null;
			}
		});
	}
	
	private String makeKey(String key){
		return new StringBuilder(SMS_KEY_PREFIX).append(":").append(key).toString();
	}
	
	/**
	 * put message channel
	 * 过期60秒
	 * @param channel
	 */
	private void putCache(final String channel){
		 JedisHolder.execute(jp, new RedisCallback<String>() {
			@Override
			public String doInRedis(Jedis jedis) {
				return  jedis.setex(makeKey(channel), 60, "60");
			}
		});
	}
	
	/**
	 * 是否存在这个key
	 * @param channel
	 * @return
	 */
	private Boolean isExistChannel(final String channel){
		return JedisHolder.execute(jp, new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(Jedis jedis) {
				return  jedis.exists(makeKey(channel));
			}
		});
	}

	/**
	 * MD5加密
	 * 
	 * @param oldPassword
	 * @return
	 * @throws IOException
	 */
	public static String encryptPassword(String oldPassword) throws IOException {
		byte[] md = SMSMessage2Handler.encryptMD5(oldPassword);
		int j = md.length;
		char str[] = new char[j * 2];
		int k = 0;
		for (int i = 0; i < j; i++) {
			byte byte0 = md[i];
			str[k++] = hexDigits[byte0 >>> 4 & 0xf];
			str[k++] = hexDigits[byte0 & 0xf];
		}
		return new String(str);
	}

	private static byte[] encryptMD5(String data) throws IOException {
		return encryptMD5(data.getBytes(CHARSET));
	}

	private static byte[] encryptMD5(byte[] data) throws IOException {
		byte[] bytes = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			bytes = md.digest(data);
		} catch (GeneralSecurityException gse) {
			String msg = getStringFromException(gse);
			throw new IOException(msg);
		}
		return bytes;
	}

	private static String getStringFromException(Throwable e) {
		String result = "";
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(bos);
		e.printStackTrace(ps);
		try {
			result = bos.toString(CHARSET);
		} catch (IOException ioe) {
		}
		return result;
	}

	@Override
	public boolean sendMessage(String channel, String... message) {
		if (StringUtils.isNotBlank(channel) && message.length > 0 && StringUtils.isNotBlank(message[0])) {
			
			//是否存在，如果存在表示60秒内发过
			if(isExistChannel(channel)){
				return false;
			}
			
			Map<String, String> params = new HashMap<String, String>();
			params.put("account", userName);
			params.put("password", pwd);
			params.put("mobile", channel);
			params.put("content", message[0]);
			try {
				String result = WebUtils.doGet(url, params, 3000, 10000);
				Elements e = Jsoup.parse(result).getElementsByTag("code");
				String code = e.first().html();
				if("2".equals(code)){
					incrInt();
					putCache(channel);
					return true;
				}else{
					log.error("SMS Send error:{}",result);
					return false;
				}
			} catch (IOException e) {
				log.error("", e);
				return false;
			}
		}
		return false;
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println(SMSMessage2Handler.encryptPassword("5nvevbhr"));
//		MessageHandler handler=new SMSMessage2Handler("120.24.68.189", 6380
//				,"cf_jgyes","2330989BD7EA70B23E58D4563A3133C4");
//		
//		handler.sendMessage("18575541919", "您的验证码是：1233456。请不要把验证码泄露给其他人。");
//		System.out.println(SMSMessage2Handler.encryptPassword("9KnkZA"));
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
		// TODO Auto-generated method stub
	}

	@Override
	public boolean sendMessage(String channel, byte[] message) {
		return  sendMessage(channel, new String(message));
	}
}
