package com.sinco.messager.sms;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.nutz.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinco.messager.CustomMessageHandler;
import com.sinco.messager.MessageDelegate;
import com.sinco.messager.redis.JedisHolder;
import com.sinco.messager.redis.JedisHolder.RedisCallback;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.AlibabaAliqinFcSmsNumSendRequest;
import com.taobao.api.response.AlibabaAliqinFcSmsNumSendResponse;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

public class SMSMessageAlidayuHandler implements CustomMessageHandler<AlidayuMessage>{

	private static String url = "http://gw.api.taobao.com/router/rest";

	private static String SMS_KEY_PREFIX="SMS";

	private static final int dbIndex=8;
	
	private static final Logger log = LoggerFactory.getLogger(SMSMessageAlidayuHandler.class);
	
	private JedisHolder jedisHolder=JedisHolder.singleton();
	
	private JedisPool jp;
	
	private String accountSid;
	
	private String accountToken;
	
	
	public SMSMessageAlidayuHandler(String redisHost, Integer redisPort,String accountSid, String accountToken) {
		this.accountSid=accountSid;
		this.accountToken=accountToken;
		jp =jedisHolder.getJedisPoolInstance(redisHost, redisPort, Protocol.DEFAULT_TIMEOUT, null, dbIndex);
	}
	public SMSMessageAlidayuHandler(JedisPool jp,String accountSid, String accountToken) {
		this.accountSid=accountSid;
		this.accountToken=accountToken;
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

	@Override
	public boolean sendMessage(String channel,AlidayuMessage message) {
		if (StringUtils.isNotBlank(channel) && StringUtils.isNotEmpty(message.getTemplateCode())) {
			//是否存在，如果存在表示60秒内发过
			if(isExistChannel(channel)){
				return false;
			}
			
			TaobaoClient client = new DefaultTaobaoClient(url, accountSid, accountToken);
			AlibabaAliqinFcSmsNumSendRequest req = new AlibabaAliqinFcSmsNumSendRequest();
			req.setExtend("");
			req.setSmsType("normal");
			req.setSmsFreeSignName(message.getSmsFreeSignName());
			req.setSmsParamString("");
			req.setSmsParam(message.getTemplateParam());
			req.setRecNum(channel);
			req.setSmsTemplateCode(message.getTemplateCode());
			try {
				AlibabaAliqinFcSmsNumSendResponse rsp = client.execute(req);
				String result = rsp.getBody();
				
				log.info(this.getClass().getName() + " SMS Send result; ", rsp.getBody());
				
				if (StringUtils.isNotEmpty(result) && result.contains("\"success\":true")) 
				{
					incrInt();
					putCache(channel);
					return true;
				}else{
					//异常返回输出错误码和错误信息
					log.error(this.getClass().getName() + "错误码：" + result);
					return false;
				}
			} catch (ApiException e) {
				log.error(this.getClass().getName() + "短信发送失败，请求错误！");
			}
		}
		return false;
	}
	
	@Override
	public SMSResult sendTemplateMessage(String channel, AlidayuMessage message) {
		SMSResult smsResult=null;
		if(StringUtils.isBlank(channel)){
			smsResult=new SMSResult(SMSConstants.SMS_CODE_MOBILE_NULL, SMSConstants.SMS_CODE_MOBILE_NULL_DESC);
			log.error(this.getClass().getName() + "错误码：" + SMSConstants.SMS_CODE_MOBILE_NULL+ "错误描述：" +SMSConstants.SMS_CODE_MOBILE_NULL_DESC);
			return smsResult;
		}
		if(StringUtils.isBlank(message.getAccountSid())){
			smsResult=new SMSResult(SMSConstants.SMS_CODE_APPID_NULL, SMSConstants.SMS_CODE_APPID_NULL_DESC);
			log.error(this.getClass().getName() + "错误码：" + SMSConstants.SMS_CODE_APPID_NULL+ "错误描述：" +SMSConstants.SMS_CODE_APPID_NULL_DESC);
			return smsResult;
		}
		if(StringUtils.isBlank(message.getAccountToken())){
			smsResult=new SMSResult(SMSConstants.SMS_CODE_APPSECRET_NULL, SMSConstants.SMS_CODE_APPSECRET_NULL_DESC);
			log.error(this.getClass().getName() + "错误码：" + SMSConstants.SMS_CODE_APPSECRET_NULL+ "错误描述：" +SMSConstants.SMS_CODE_APPSECRET_NULL_DESC);
			return smsResult;
		}
		if(StringUtils.isBlank(message.getSmsFreeSignName())){
			smsResult=new SMSResult(SMSConstants.SMS_CODE_SIGNATURE_NULL, SMSConstants.SMS_CODE_SIGNATURE_NULL_DESC);
			log.error(this.getClass().getName() + "错误码：" + SMSConstants.SMS_CODE_SIGNATURE_NULL+ "错误描述：" +SMSConstants.SMS_CODE_SIGNATURE_NULL_DESC);
			return smsResult;
		}
		if(StringUtils.isBlank(message.getTemplateCode())){
			smsResult=new SMSResult(SMSConstants.SMS_CODE_TEMPALTEID_NULL, SMSConstants.SMS_CODE_TEMPALTEID_NULL_DESC);
			log.error(this.getClass().getName() + "错误码：" + SMSConstants.SMS_CODE_TEMPALTEID_NULL+ "错误描述：" +SMSConstants.SMS_CODE_TEMPALTEID_NULL_DESC);
			return smsResult;
		}
		//是否存在，如果存在表示60秒内发过
		if(isExistChannel(channel)){
			smsResult=new SMSResult(SMSConstants.SMS_CODE_REPEAT_SEND, SMSConstants.SMS_CODE_REPEAT_SEND_DESC);
			log.error(this.getClass().getName() + "错误码：" + SMSConstants.SMS_CODE_REPEAT_SEND+ "错误描述：" +SMSConstants.SMS_CODE_REPEAT_SEND_DESC);
			return smsResult;
		}
		try {
			log.info(this.getClass().getName() + " SMS Send param; ====>", "mobile:"+channel+" appid:"+message.getAccountSid()+" appsecret:"+message.getAccountToken()+" signature:"+message.getSmsFreeSignName()+" sms_id:"+message.getTemplateCode()+" param:"+message.getTemplateParam());
			
			TaobaoClient client = new DefaultTaobaoClient(url, message.getAccountSid(), message.getAccountToken());
			AlibabaAliqinFcSmsNumSendRequest req = new AlibabaAliqinFcSmsNumSendRequest();
			req.setExtend("");
			req.setSmsType("normal");
			req.setSmsFreeSignName(message.getSmsFreeSignName());
			req.setSmsParamString("");
			req.setSmsParam(message.getTemplateParam());
			req.setRecNum(channel);
			req.setSmsTemplateCode(message.getTemplateCode());
		
			AlibabaAliqinFcSmsNumSendResponse rsp = client.execute(req);
			if(rsp == null || rsp.getBody() == null){
				smsResult=new SMSResult(SMSConstants.SMS_CODE_API_ERROR, SMSConstants.SMS_CODE_API_ERROR_DESC);
				log.error(this.getClass().getName() + "错误码：" + SMSConstants.SMS_CODE_API_ERROR+ "错误描述：" +SMSConstants.SMS_CODE_API_ERROR_DESC);
				return smsResult;
			}
			
			/**
			 * 
			 * 返回示例
			 * 
			 * 失败：
			 * {
				    "error_response":{
				        "code":15,
				        "msg":"Remote service error",
				        "sub_code":"isv.OUT_OF_SERVICE",
				        "sub_msg":"业务停机",
				        "request_id":"1475yyty3lpgs"
				    }
				}
			 * 
			 * 成功：
			 * {
				    "alibaba_aliqin_fc_sms_num_send_response":{
				        "result":{
				            "err_code":"0",
				            "model":"100896738280^1101347131111",
				            "success":true
				        },
				        "request_id":"1476r12h2najv"
				    }
				}
			 * 
			 */
			
			String result = rsp.getBody();
			log.info(this.getClass().getName() + " SMS Send result; ====>", rsp.getBody());
			
			Map map=(Map) Json.fromJson(result);
			if(result.contains("\"error_response\"")){
				Map errorResponseMap=(Map) map.get("error_response");
				smsResult=new SMSResult(String.valueOf(errorResponseMap.get("code")), String.valueOf(errorResponseMap.get("msg")), String.valueOf(errorResponseMap.get("sub_code")), String.valueOf(errorResponseMap.get("sub_msg")), String.valueOf(errorResponseMap.get("request_id")));
				log.error(this.getClass().getName() + "错误码：" + String.valueOf(errorResponseMap.get("sub_code"))+ "错误描述：" +String.valueOf(errorResponseMap.get("sub_msg")));
				return smsResult;
			}else{
				if(result.contains("\"success\":true")){
					Map successResponseMap=(Map) map.get("alibaba_aliqin_fc_sms_num_send_response");
					Map resultMap=(Map) successResponseMap.get("result");
					smsResult=new SMSResult(String.valueOf(resultMap.get("err_code")), SMSConstants.SMS_CODE_SUCCESS_DESC, String.valueOf(resultMap.get("model")), String.valueOf(successResponseMap.get("request_id")));
					log.error(this.getClass().getName() + "发送成功");
					incrInt();
					putCache(channel);
					return smsResult;
				}else{
					smsResult=new SMSResult(SMSConstants.SMS_CODE_SYSTEM_ERROR, SMSConstants.SMS_CODE_SYSTEM_ERROR_DESC);
					log.error(this.getClass().getName() + "错误码：" + SMSConstants.SMS_CODE_SYSTEM_ERROR+ "错误描述：" +SMSConstants.SMS_CODE_SYSTEM_ERROR_DESC);
					return smsResult;
				}
			}
		} catch (ApiException e) {
			smsResult=new SMSResult(SMSConstants.SMS_CODE_API_ERROR, SMSConstants.SMS_CODE_API_ERROR_DESC);
			log.error(this.getClass().getName() + "错误码：" + SMSConstants.SMS_CODE_API_ERROR+ "错误描述：" +SMSConstants.SMS_CODE_API_ERROR_DESC);
			log.error("e:"+e);
			return smsResult;
		}catch (Exception e) {
			smsResult=new SMSResult(SMSConstants.SMS_CODE_SYSTEM_ERROR, SMSConstants.SMS_CODE_SYSTEM_ERROR_DESC);
			log.error(this.getClass().getName() + "错误码：" + SMSConstants.SMS_CODE_SYSTEM_ERROR+ "错误描述：" +SMSConstants.SMS_CODE_SYSTEM_ERROR_DESC);
			log.error("e:"+e);
			return smsResult;
		}
	}
	
	public static void main(String[] args){
//		SMSMessageAlidayuHandler handler=new SMSMessageAlidayuHandler("127.0.0.1", 6379, "23301596", "50bd0f1e8790f4aa81fbb46bf4b1753e");
		SMSMessageAlidayuHandler handler=new SMSMessageAlidayuHandler("127.0.0.1", 6379, "23315902", "5d5897d109aadc4f9ef8f45fa482f8e9");
		AlidayuMessage ali = new AlidayuMessage("深港专车", "SMS_4999636", "{\"code\":\"123456\",\"product\":\"唯秘\"}");
		handler.sendMessage("13480816563", ali);
//		ObjectMapper mapper = new ObjectMapper();  
//		String str="{\"alibaba_aliqin_fc_sms_num_send_response\":{\"result\":{\"err_code\":\"0\",\"model\":\"100896738280^1101347131111\",\"success\":true},\"request_id\":\"1476r12h2najv\"}}";
//		Map map=(Map) Json.fromJson(str);
//		System.err.println(map.get("alibaba_aliqin_fc_sms_num_send_response"));
	}
	
}