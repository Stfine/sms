package com.sinco.messager.util;

import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * Created by yuriy_gorbylov on 01.12.2015.
 */
public class SoapUtils {
    private static SOAPConnectionFactory factory;

    public static SOAPConnection getSoapConnection() {
        try {
            if (factory == null) factory = SOAPConnectionFactory.newInstance();
            return factory.createConnection();
        } catch (SOAPException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void closeSoapConnection(SOAPConnection connection){
        try {
            if (connection != null) connection.close();
        } catch (SOAPException e) {
            e.printStackTrace();
        }
    }

    public static SOAPMessage call(SOAPMessage soapMessage, String URL){
        try {
            SOAPConnection soapConnection = getSoapConnection();
            if (soapConnection != null) {
                SOAPMessage response = soapConnection.call(soapMessage, URL);
                closeSoapConnection(soapConnection);
                return response;
            }
        } catch (SOAPException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SOAPConnectionFactory getSOAPConnectionFactory() {
        return factory;
    }
}
