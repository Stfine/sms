package com.sinco.messager.sms;


import java.io.IOException;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.github.qcloudsms.httpclient.HTTPException;
import com.sinco.messager.CustomMessageHandler;
import com.sinco.messager.MessageDelegate;

/**
 * 腾讯云短信验证码发送
 * @author ZhangGaoXiang
 * @date   2018年12月12日 上午11:43:40
 */
public class TencentYunMessageHandler implements  CustomMessageHandler<TencentYunMessage>{

	private final static Logger logger = LoggerFactory.getLogger(TencentYunMessageHandler.class);
	
	private String appid;
	
	private String appKey;
	
	
	public TencentYunMessageHandler(String accessKeyId, String accessKeySecret) {
		this.appid=accessKeyId;
		this.appKey=accessKeySecret;
	}
	
	@Override
	public void listenerMessage(MessageDelegate delegate, String channel, int threadNum) { }

	@Override
	public void listenerMessage(MessageDelegate delegate, String channel) { }

	@Override
	public void shutdownListenerAll() { }

	@Override
	public void shutdownListener(String channel) { }

	@Override
	public SMSResult sendTemplateMessage(String channel, TencentYunMessage message) {
		return null;
	}
	
	@Override
	public boolean sendMessage(String channel, String... message) {
		throw new RuntimeException(this.getClass().getName() + "==该平台不支持此方法调用！");
	}

	@Override
	public boolean sendMessage(String channel, byte[] message) {
		return sendMessage(channel, new String(message));
	}
	
	@Override
	public boolean sendMessage(String mobile, TencentYunMessage message) {
		
		try {
			
			logger.info("Tencent SMS mobile: {}", mobile);
			logger.info("Tencent SMS message: {}", message.getParams()[0]);
			
			SmsSingleSender ssender = new SmsSingleSender(Integer.parseInt(appid), appKey);
					SmsSingleSenderResult result = ssender.sendWithParam(message.getCountryCode(), mobile, 
					message.getTemplateId(), message.getParams(), message.getSmsSign(), "", "");
			
			logger.info("返回报文:[{}]",ssender.toString());
			
			JSONObject object = JSON.parseObject(result.toString());
			
			logger.info("返回报文:[{}]",object.toString());
			
			if("0".equals(object.getString("result")) && "OK".equals(object.getString("errmsg"))){
				return true;
			}else {
				logger.info("发送腾讯短信异常:[{}]",object.getString("errmsg"));
				return false;
			}
		} catch (NumberFormatException | JSONException | HTTPException | IOException e) {
			logger.error("发送腾讯短信异常",e.getMessage());
			logger.error("发送腾讯短信异常",e.getLocalizedMessage());
			return false;
		}
	}

}
