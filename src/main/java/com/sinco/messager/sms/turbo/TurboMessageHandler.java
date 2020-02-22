package com.sinco.messager.sms.turbo;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinco.messager.CustomMessageHandler;
import com.sinco.messager.MessageDelegate;
import com.sinco.messager.sms.SMSResult;
import com.sinco.messager.util.SoapUtils;

/**
 * 乌克兰本地短信(turbo)
 * 
 * @author ZhangGaoXiang
 * @time Dec 14, 20192:44:57 PM
 */
public class TurboMessageHandler implements CustomMessageHandler<TurboMessage> {

	private static final Logger logger = LoggerFactory.getLogger(TurboMessageHandler.class);

	private String username;

	private String password;

	private String url;

	private String namespace;

	public TurboMessageHandler(String username, String password) {
		this.username = username;
		this.password = password;
		this.namespace = "http://turbosms.in.ua/api/Turbo";
		this.url = "http://turbosms.in.ua/api/soap.html";
	}

//	private final Set<String> validMessageStatusResponses = new HashSet<String>() {
//		{
//			add("Отправлено");
//			add("В очереди");
//			add("Сообщение передано в мобильную сеть");
//			add("Сообщение доставлено получателю");
//			add("Истек срок сообщения");
//			add("Удалено оператором");
//			add("Не доставлено");
//			add("Сообщение доставлено на сервер");
//			add("Неизвестный статус");
//			add("Не достаточно кредитов на счете");
//			add("Удалено пользователем");
//			add("Ошибка, сообщение не отправлено");
//		}
//	};
	private final Set<String> successSendSMSStatusResponses = new HashSet<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("Сообщения успешно отправлены");
		}
	};

	@Override
	public boolean sendMessage(String channel, String... message) {
		return false;
	}

	@Override
	public boolean sendMessage(String channel, byte[] message) {
		return false;
	}

	@Override
	public void listenerMessage(MessageDelegate delegate, String channel, int threadNum) {

	}

	@Override
	public void listenerMessage(MessageDelegate delegate, String channel) {

	}

	@Override
	public void shutdownListenerAll() {

	}

	@Override
	public void shutdownListener(String channel) {

	}

	@Override
	public boolean sendMessage(String channel, TurboMessage message) {
		try {

			logger.info("Turbo SMS mobile: {}", channel);
			logger.info("Turbo SMS message: {}", message.getMessage());

			Double balance = getBalance();
			logger.info("剩余余额: {}", balance);
			
			if (balance <= 0) {
				logger.info("余额不足");
				return false;
			}

			logger.info("已经删除wappush");
			
			SOAPMessage response = call(
					"SendSMS", 
					new Parameter("sender", message.getSignature()),
					new Parameter("destination", channel), 
					new Parameter("text", message.getMessage())
//					new Parameter("wappush", "wappush")
				);
			
			String responseValue = getResponseValue(response.getSOAPBody().getChildElements());
			if (!successSendSMSStatusResponses.contains(responseValue) && !responseValue.contains("-")) { // response as
				logger.error("Turbo SMS has not been sent. Reason:[{}]", responseValue);
				return false;
			}

			logger.info("Turbo SMS send Success");
			return true;
		} catch (SOAPException e) {
			logger.error("Turbo SMS send fail :[{}]", e.getMessage());
			return false;
		}
	}

	public Double getBalance() throws SOAPException {
		SOAPMessage balanceResp = call("GetCreditBalance");
		String responseValue = getResponseValue(balanceResp.getSOAPBody().getChildElements());
		if (!responseValue.matches("-?\\d+(\\.\\d+)?")) { // is response value numeric
			logger.error("Turbo SMS Error[{}]", responseValue);
			return Double.valueOf("0");
		}
		return Double.valueOf(responseValue);
	}

	@Override
	public SMSResult sendTemplateMessage(String channel, TurboMessage message) {
		return null;
	}

	public Double balance() {
		try {
			SOAPMessage response = call("GetCreditBalance");
			String responseValue = getResponseValue(response.getSOAPBody().getChildElements());
			if (!responseValue.matches("-?\\d+(\\.\\d+)?")) { // is response value numeric
				logger.error("TurboMessageHandler exception:[{}]", responseValue);
				return Double.valueOf("0");
			}
			return Double.valueOf(responseValue);
		} catch (SOAPException e) {
			logger.error("TurboMessageHandler SOAPException:[{}]", e.getMessage());
			return Double.valueOf("0");
		}
	}

	public SOAPMessage call(String methodName, Parameter... parameters) {
		try {
			SOAPMessage soapMessage = initSOAPMessage();
			SOAPBody body = soapMessage.getSOAPBody();
			SOAPElement parent = body.addChildElement(methodName, "ns1");
			for (Parameter parameter : parameters) {
				parent.addChildElement(parameter.getName(), "ns1").addTextNode(parameter.getValue());
			}
			soapMessage.getMimeHeaders().addHeader("Cookie", getSessionId());
			soapMessage.saveChanges();
			return SoapUtils.call(soapMessage, url);
		} catch (SOAPException e) {
			e.printStackTrace();
		}
		return null;
	}

	private SOAPMessage initSOAPMessage() throws SOAPException {
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessage = messageFactory.createMessage();
		SOAPPart soapPart = soapMessage.getSOAPPart();
		SOAPEnvelope envelope = soapPart.getEnvelope();
		envelope.addNamespaceDeclaration("ns1", namespace);
		MimeHeaders headers = soapMessage.getMimeHeaders();
		headers.addHeader("SOAPAction", url);
		soapMessage.saveChanges();
		return soapMessage;
	}

	private String getSessionId() throws SOAPException {
		SOAPMessage soapMessage = initSOAPMessage();
		SOAPBody soapBody = soapMessage.getSOAPBody();
		soapBody.addChildElement("Auth", "ns1").addChildElement("login", "ns1").addTextNode(username).getParentElement()
				.addChildElement("password", "ns1").addTextNode(password);
		soapMessage.saveChanges();

		SOAPMessage response = SoapUtils.call(soapMessage, url);

		if (response == null) {
			logger.error("Soap.call 没有返回");
			return null;
		}
		String[] headers = response.getMimeHeaders().getHeader("Set-Cookie");
		if (headers != null && headers.length != 0) {
			return headers[0];
		}
		return null;
	}

	private String getResponseValue(Iterator childElements) throws SOAPException {
		SOAPBodyElement bodyElement = (SOAPBodyElement) childElements.next();
		return bodyElement.getValue() == null ? getResponseValue(bodyElement.getChildElements())
				: bodyElement.getValue();
	}
}
