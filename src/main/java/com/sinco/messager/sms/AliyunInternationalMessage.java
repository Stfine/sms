package com.sinco.messager.sms;

import com.sinco.messager.Message;

public class AliyunInternationalMessage implements Message {

	@Override
	public String content() {
		return null;
	}

	public AliyunInternationalMessage() {
	}

	public AliyunInternationalMessage(String templateCode, String templateParam) {
		this.templateCode = templateCode;
		this.templateParam = templateParam;
	}

	public AliyunInternationalMessage(String templateCode, String templateParam, String accessKeyId, String accessKeySecret) {
		this.templateCode = templateCode;
		this.templateParam = templateParam;
		this.accessKeyId = accessKeyId;
		this.accessKeySecret = accessKeySecret;
	}

	/**
	 * json格式；{code:123456, userName: 章三}
	 */
	private String templateParam;

	private String templateCode;

	private String accessKeyId;

	private String accessKeySecret;

	
	public String getTemplateParam() {
		return templateParam;
	}

	
	public void setTemplateParam(String templateParam) {
		this.templateParam = templateParam;
	}

	
	public String getTemplateCode() {
		return templateCode;
	}

	
	public void setTemplateCode(String templateCode) {
		this.templateCode = templateCode;
	}

	
	public String getAccessKeyId() {
		return accessKeyId;
	}

	
	public void setAccessKeyId(String accessKeyId) {
		this.accessKeyId = accessKeyId;
	}

	
	public String getAccessKeySecret() {
		return accessKeySecret;
	}


	public void setAccessKeySecret(String accessKeySecret) {
		this.accessKeySecret = accessKeySecret;
	}
	
	
}
