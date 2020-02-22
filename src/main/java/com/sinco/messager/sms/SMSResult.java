package com.sinco.messager.sms;

/**
 * 阿里大鱼短信发送返回信息bean
 * @author Kevin
 *
 */
public class SMSResult {
	//返回码
	private String code;
	//返回信息
	private String msg;
	//错误码
	private String subCode;
	//错误信息
	private String subMsg;
	//业务ID（阿里大鱼流水号）
	private String requestId;
	//发送成功的model
	private String model;
	
	public SMSResult() {}
	
	public SMSResult(String code, String msg) {
		super();
		this.code = code;
		this.msg = msg;
	}

	public SMSResult(String code, String msg, String requestId, String model) {
		super();
		this.code = code;
		this.msg = msg;
		this.requestId = requestId;
		this.model = model;
	}

	public SMSResult(String code, String msg, String subCode, String subMsg,
			String requestId) {
		super();
		this.code = code;
		this.msg = msg;
		this.subCode = subCode;
		this.subMsg = subMsg;
		this.requestId = requestId;
	}

	public SMSResult(String code, String msg, String subCode, String subMsg,String requestId, String model) {
		this.code = code;
		this.msg = msg;
		this.subCode = subCode;
		this.subMsg = subMsg;
		this.requestId = requestId;
		this.model = model;
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getSubCode() {
		return subCode;
	}
	public void setSubCode(String subCode) {
		this.subCode = subCode;
	}
	public String getSubMsg() {
		return subMsg;
	}
	public void setSubMsg(String subMsg) {
		this.subMsg = subMsg;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}

	@Override
	public String toString() {
		return "SMSResult [code=" + code + ", msg=" + msg + ", subCode="
				+ subCode + ", subMsg=" + subMsg + ", requestId=" + requestId
				+ ", model=" + model + "]";
	}
	
}
