package com.sinco.messager.sms;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20180501.SendSmsResponse;
import com.aliyuncs.dysmsapi.model.v20180501.SendSmsRequest;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.sinco.messager.CustomMessageHandler;
import com.sinco.messager.MessageDelegate;
import com.sinco.messager.redis.JedisHolder;
import com.sinco.messager.redis.JedisHolder.RedisCallback;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

public class SmsMessageAliyunInternationalHandler implements CustomMessageHandler<AliyunInternationalMessage> {

	private Logger logger = LoggerFactory.getLogger(SmsMessageAliyunInternationalHandler.class);

	private JedisPool jp;
	
	private String accessKeyId;
	
	private String accessKeySecret;
	
	private static String SMS_KEY_PREFIX="SMS";
	
	private static final int dbIndex=8;
	
	private JedisHolder jedisHolder = JedisHolder.singleton();
	
	// product name， please remain unchanged
    static final String product = "Dysmsapi";
    // product domain, please remain unchanged
    static final String domain = "dysmsapi.ap-southeast-1.aliyuncs.com";
    
    
    public SmsMessageAliyunInternationalHandler(String redisHost, Integer redisPort,String accessKeyId, String accessKeySecret) {
		this.accessKeyId=accessKeyId;
		this.accessKeySecret=accessKeySecret;
		jp =jedisHolder.getJedisPoolInstance(redisHost, redisPort, Protocol.DEFAULT_TIMEOUT, null, dbIndex);
	}
	
	public SmsMessageAliyunInternationalHandler(JedisPool jp,String accessKeyId, String accessKeySecret) {
		this.accessKeyId=accessKeyId;
		this.accessKeySecret=accessKeySecret;
		this.jp =jp;
	}

	@Override
	public boolean sendMessage(String channel, String... message) {
		return false;
	}

	@Override
	public boolean sendMessage(String channel, byte[] message) {
		return false;
	}

	@Override
	public void listenerMessage(MessageDelegate delegate, String channel, int threadNum) {

	}

	@Override
	public void listenerMessage(MessageDelegate delegate, String channel) {

	}

	@Override
	public void shutdownListenerAll() {

	}

	@Override
	public void shutdownListener(String channel) {

	}

	@Override
	public boolean sendMessage(String channel, AliyunInternationalMessage message) {
		
		if(isExistChannel(channel)){
			return false;
		}
		
		try {
            System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
            System.setProperty("sun.net.client.defaultReadTimeout", "10000");

            IClientProfile profile = DefaultProfile.getProfile("ap-southeast-1", accessKeyId, accessKeySecret);
            DefaultProfile.addEndpoint("ap-southeast-1", "ap-southeast-1", product, domain);
            IAcsClient acsClient = new DefaultAcsClient(profile);

            // initiate the SendSmsRequest, read help documents for more parameters instructions
            SendSmsRequest request = new SendSmsRequest();
            // send to
            request.setPhoneNumbers(channel);
            // ContentCode , you can login sms console and find it in Content Management
            request.setContentCode(message.getTemplateCode());
            // set the value for parameters in sms Content with JSON format. For example, the content is "Your Verification Code : ${code}, will be expired 5 minutes later"
            request.setContentParam(message.getTemplateParam());
            // Optional，custom field, this value will be returned in the sms delivery report.
//            request.setExternalId("E0012033");

            SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
            if(StringUtils.isNotBlank(sendSmsResponse.getResultCode()) && "OK".equals(sendSmsResponse.getResultCode())) {
            	logger.info("恭喜短信发送成功！！");
				incrInt();
				putCache(channel);
            	return true;
            }else {
            	logger.info("很遗憾，短信发送失败！！");
            	logger.info("错误原因:{}",sendSmsResponse.getResultMessage());
            	return false;
            }
            
        } catch (Exception e) {
        	logger.error(e.getMessage());
			return false;
        }
	}

	@Override
	public SMSResult sendTemplateMessage(String channel, AliyunInternationalMessage message) {
		return null;
	}

	/**
	 * 是否存在这个key
	 * 
	 * @param channel
	 * @return
	 */
	private Boolean isExistChannel(final String channel) {
		return JedisHolder.execute(jp, new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(Jedis jedis) {
				return jedis.exists(makeKey(channel));
			}
		});

	}

	private String makeKey(String key) {
		return new StringBuilder(SMS_KEY_PREFIX).append(":").append(key).toString();
	}
	
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
}
