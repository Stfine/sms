package com.sinco.messager.sms;

public class SMSConstants {
	/**短信发送错误码*/
	//成功
	public final static String SMS_CODE_SUCCESS="0";
	//手机号为空
	public final static String SMS_CODE_MOBILE_NULL="1";
	//appid为空
	public final static String SMS_CODE_APPID_NULL="2";
	//appsecret为空
	public final static String SMS_CODE_APPSECRET_NULL="3";
	//签名为空
	public final static String SMS_CODE_SIGNATURE_NULL="4";
	//模板ID为空
	public final static String SMS_CODE_TEMPALTEID_NULL="5";
	//60秒内重复发送
	public final static String SMS_CODE_REPEAT_SEND="6";
	//调用api出错
	public final static String SMS_CODE_API_ERROR="7";
	//参数错误
	public final static String SMS_CODE_PARAM_ERROR="8";
	//系统错误
	public final static String SMS_CODE_SYSTEM_ERROR="999";
	
	/**短信发送错误描述*/
	//成功
	public final static String SMS_CODE_SUCCESS_DESC="发送成功";
	//手机号为空
	public final static String SMS_CODE_MOBILE_NULL_DESC="手机号为空";
	//appid为空
	public final static String SMS_CODE_APPID_NULL_DESC="appid为空";
	//appsecret为空
	public final static String SMS_CODE_APPSECRET_NULL_DESC="appsecret为空";
	//签名为空
	public final static String SMS_CODE_SIGNATURE_NULL_DESC="短信签名为空";
	//模板ID为空
	public final static String SMS_CODE_TEMPALTEID_NULL_DESC="短信模板ID为空";
	//60秒内重复发送
	public final static String SMS_CODE_REPEAT_SEND_DESC="60秒内重复发送";
	//调用api出错
	public final static String SMS_CODE_API_ERROR_DESC="调用API出错";
	//参数错误
	public final static String SMS_CODE_PARAM_ERROR_DESC="参数为空";
	//系统错误
	public final static String SMS_CODE_SYSTEM_ERROR_DESC="系统错误";
}
