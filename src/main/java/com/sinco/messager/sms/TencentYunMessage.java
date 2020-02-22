package com.sinco.messager.sms;

import com.sinco.messager.Message;

/**
 * 腾讯云短息对象
 * @author ZhangGaoXiang
 * @date   2018年12月13日 下午6:24:31
 */
public class TencentYunMessage implements Message{

	private String smsSign;
	private Integer templateId;
	// 默认中国
	private String countryCode; 
	private String[] params;
	
	public TencentYunMessage( String smsSign , Integer templateId,String [] params) {
		 
		this.smsSign = smsSign;
		this.templateId = templateId;
		this.params = params;
	}
	
	public TencentYunMessage( String smsSign , Integer templateId, String countryCode, String [] params) {
		 
		this.smsSign = smsSign;
		this.templateId = templateId;
		this.countryCode = countryCode;
		this.params = params;
	}


	public String getSmsSign() {
		return smsSign;
	}

	public void setSmsSign(String smsSign) {
		this.smsSign = smsSign;
	}

	public Integer getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Integer templateId) {
		this.templateId = templateId;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	
	public String[] getParams() {
		return params;
	}

	public void setParams(String[] params) {
		this.params = params;
	}

	@Override
	public String content() {
		return null;
	}

}
