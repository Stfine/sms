package com.sinco.messager.sms;

import com.sinco.messager.Message;

public class AlidayuMessage implements Message{
	
	public AlidayuMessage(){}
	
	public AlidayuMessage(String smsFreeSignName, String templateCode, String templateParam)
	{
		this.smsFreeSignName = smsFreeSignName;
		this.templateCode = templateCode;
		this.templateParam = templateParam;
	}
	
	public AlidayuMessage(String smsFreeSignName, String templateCode, String templateParam,String accountSid, String accountToken)
	{
		this.smsFreeSignName = smsFreeSignName;
		this.templateCode = templateCode;
		this.templateParam = templateParam;
		this.accountSid = accountSid;
		this.accountToken = accountToken;
	}
	
	/**
	 * json格式；{code:123456, userName: 章三}
	 */
	private String templateParam;
	
	/**
	 * 对应模版在阿里大鱼对应的模版代码
	 */
	private String templateCode;
	
	/**
	 * 短信签名，传入的短信签名必须是在阿里大鱼“管理中心-短信签名管理”中的可用签名。如“阿里大鱼”已在短信签名管理中通过审核，则可传入”阿里大鱼“（传参时去掉引号）作为短信签名。短信效果示例：【阿里大鱼】欢迎使用阿里大鱼服务。
	 */
	private String smsFreeSignName;
	
	/**
	 * 对应应用的appid
	 */
	private String accountSid;
	
	/**
	 * 对应应用的appsecret
	 */
	private String accountToken;
	
	public String getSmsFreeSignName() {
		return smsFreeSignName;
	}

	public void setSmsFreeSignName(String smsFreeSignName) {
		this.smsFreeSignName = smsFreeSignName;
	}

	public String getTemplateCode() {
		return templateCode;
	}

	public void setTemplateCode(String templateCode) {
		this.templateCode = templateCode;
	}

	public String getTemplateParam() {
		return templateParam;
	}

	public void setTemplateParam(String templateParam) {
		this.templateParam = templateParam;
	}

	public String getAccountSid() {
		return accountSid;
	}

	public void setAccountSid(String accountSid) {
		this.accountSid = accountSid;
	}

	public String getAccountToken() {
		return accountToken;
	}

	public void setAccountToken(String accountToken) {
		this.accountToken = accountToken;
	}

	@Override
	public String content() {
		return "";
	}
}
