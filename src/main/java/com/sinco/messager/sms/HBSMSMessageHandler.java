package com.sinco.messager.sms;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sinco.common.utils.MD5Util;
import com.sinco.messager.MessageDelegate;
import com.sinco.messager.MessageHandler;
import com.sinco.messager.util.WebUtils;

/**
 * 物联网短信发送
 * @author james
 *
 */
public class HBSMSMessageHandler  implements MessageHandler{

	private String userId;
	private String userPwd;
	
	private static final Logger log = LoggerFactory.getLogger(HBSMSMessageHandler.class);
	
	
	public HBSMSMessageHandler(String userId,String userPwd) {
		this.userId = userId;
		this.userPwd = userPwd;
	}
	
	private String url="http://iot.hbsmservice.com:8096/iot_platform_interface/iotCustomerSendSms.do";
	
	public static void main(String[] args) {
		System.out.println(MD5Util.getMD5String("123"));
	}
	
	public boolean sendMessage(String channel, String... message) {
		
		for (String m : message) {
			String transId=RandomStringUtils.randomNumeric(5);
			String column_name="iccid";
			String column_value=channel;
			String msg_content=m;
			String sign=MD5Util.getMD5String(column_name+column_value+msg_content+transId+userId+userPwd);
			
			Map<String, String> params=new HashMap<String, String>();
			params.put("userId", userId);
			params.put("transId", transId);
			params.put("column_name", column_name);
			params.put("column_value", column_value);
			params.put("msg_content", msg_content);
			params.put("sign", sign);
			
			 try {
				 String resp=WebUtils.doGet(url, params, null, 2000, 5000, "utf-8");
				 JSONObject result= JSON.parseObject(resp);
				 String code=result.getString("code");
				 if(!"0000".equals(code)){
					 log.error("物联平台返回短信失败 code:{} msg:{}",code,result.getString("description"));
					 return false;
				 }
				 return true;
			} catch (IOException e) {
				log.error("发送短信失败",e);
				return false;
			}
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

	@Override
	public boolean sendMessage(String channel, byte[] message) {
		return sendMessage(channel, new String(message));
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
