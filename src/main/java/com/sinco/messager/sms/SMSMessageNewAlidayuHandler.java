package com.sinco.messager.sms;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.sinco.messager.CustomMessageHandler;
import com.sinco.messager.MessageDelegate;
import com.sinco.messager.redis.JedisHolder;
import com.sinco.messager.redis.JedisHolder.RedisCallback;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

public class SMSMessageNewAlidayuHandler implements CustomMessageHandler<AlidayuNewMessage>{
	
	private Logger logger = LoggerFactory.getLogger(SMSMessageNewAlidayuHandler.class);

	final String product = "Dysmsapi";//短信API产品名称（短信产品名固定，无需修改）
	
	final String domain = "dysmsapi.aliyuncs.com";//短信API产品域名（接口地址固定，无需修改）
	
	private static String SMS_KEY_PREFIX="SMS";
	
	private static final int dbIndex=8;
	
	private JedisHolder jedisHolder = JedisHolder.singleton();
	
	private JedisPool jp;
	
	private String accessKeyId;
	
	private String accessKeySecret;
	
	public SMSMessageNewAlidayuHandler(String redisHost, Integer redisPort,String accessKeyId, String accessKeySecret) {
		this.accessKeyId=accessKeyId;
		this.accessKeySecret=accessKeySecret;
		jp =jedisHolder.getJedisPoolInstance(redisHost, redisPort, Protocol.DEFAULT_TIMEOUT, null, dbIndex);
	}
	
	public SMSMessageNewAlidayuHandler(JedisPool jp,String accessKeyId, String accessKeySecret) {
		this.accessKeyId=accessKeyId;
		this.accessKeySecret=accessKeySecret;
		this.jp =jp;
	}
	
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
		throw new RuntimeException(this.getClass().getName() + "==该平台不支持此方法调用！");
	}

	@Override
	public boolean sendMessage(String channel, byte[] message) {
		return sendMessage(channel, new String(message));
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
	
	
	private String makeKey(String key){
		return new StringBuilder(SMS_KEY_PREFIX).append(":").append(key).toString();
	}
	
	public boolean sendMessage(String channel,AlidayuNewMessage message){
		
		if(isExistChannel(channel)){
			return false;
		}
		
		try{
			
			IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId,accessKeySecret);
			DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
			IAcsClient acsClient = new DefaultAcsClient(profile);
			
			
			//组装请求对象
			SendSmsRequest request = new SendSmsRequest();
			request.setMethod(MethodType.POST);
			request.setPhoneNumbers(channel);
			request.setSignName(message.getSmsFreeSignName());
			request.setTemplateCode(message.getTemplateCode());
			request.setTemplateParam(message.getTemplateParam());
			
			SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
			if(sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals("OK")) {
				logger.info("恭喜短信发送成功！！");
				incrInt();
				putCache(channel);
				return true;
			}else {
				logger.info("很遗憾，短信发送失败！！");
				logger.info(this.getClass().getName() + "错误码：" 
									+ sendSmsResponse.getMessage()+""+sendSmsResponse.getCode());
				return false;
			}
		}catch (ClientException e) {
			logger.error(e.getMessage());
			logger.error(e.getErrMsg());
			return false;
		}
	}

	@Override
	public SMSResult sendTemplateMessage(String channel, AlidayuNewMessage message) {
		return null;
	}
}
