package com.sinco.messager.sms.turbo;

import com.sinco.messager.Message;

/**
 *
 * @author ZhangGaoXiang
 * @time Dec 14, 20192:45:58 PM
 */
public class TurboMessage implements Message{

	private String message;
	
	private String signature;
	
	
	public TurboMessage() {
		super();
	}

	public TurboMessage(String message, String signature) {
		this.message = message;
		this.signature = signature;
	}
	@Override
	public String content() {
		return null;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}

}
