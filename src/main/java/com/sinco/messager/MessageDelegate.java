package com.sinco.messager;

/**
 * 消息的代理
 * @author dengwei
 * @date 2014年8月9日 下午4:38:54 
 *
 */
public interface MessageDelegate {

	void handleMessage(String message, String channel);
}
