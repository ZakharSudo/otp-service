package com.otp.service;

import org.jsmpp.bean.*;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Service
public class SmsNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationService.class);
    private final String host;
    private final int port;
    private final String systemId;
    private final String password;
    private final String systemType;
    private final String sourceAddress;
    private boolean configured = false;
    
    public SmsNotificationService() {
        Properties config = loadConfig();
        this.host = config.getProperty("smpp.host", "localhost");
        this.port = Integer.parseInt(config.getProperty("smpp.port", "2775"));
        this.systemId = config.getProperty("smpp.system_id", "smppclient1");
        this.password = config.getProperty("smpp.password", "password");
        this.systemType = config.getProperty("smpp.system_type", "OTP");
        this.sourceAddress = config.getProperty("smpp.source_addr", "OTPService");
        
        if (!systemId.equals("smppclient1") || !host.equals("localhost")) {
            this.configured = true;
            logger.info("SMS notification service configured for: {}:{}", host, port);
        } else {
            logger.warn("SMS notification service using default config - update sms.properties for real SMPP server");
        }
    }
    
    private Properties loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("sms.properties")) {
            Properties props = new Properties();
            if (input != null) {
                props.load(input);
            } else {
                logger.warn("sms.properties not found, using defaults");
            }
            return props;
        } catch (Exception e) {
            logger.error("Failed to load SMS configuration", e);
            throw new RuntimeException("Failed to load SMS configuration", e);
        }
    }
    
    public void sendCode(String phoneNumber, String code) {
        if (!configured) {
            logger.info("SMS emulator mode - would send code {} to {}", code, phoneNumber);
            return;
        }
        
        SMPPSession session = new SMPPSession();
        try {
            BindParameter bindParameter = new BindParameter(
                BindType.BIND_TX,
                systemId,
                password,
                systemType,
                TypeOfNumber.UNKNOWN,
                NumberingPlanIndicator.UNKNOWN,
                sourceAddress
            );
            
            session.connectAndBind(host, port, bindParameter);
            
            String message = String.format("Your OTP code: %s", code);
            
            session.submitShortMessage(
                systemType,
                TypeOfNumber.UNKNOWN,
                NumberingPlanIndicator.UNKNOWN,
                sourceAddress,
                TypeOfNumber.UNKNOWN,
                NumberingPlanIndicator.UNKNOWN,
                phoneNumber,
                new ESMClass(),
                (byte) 0,
                (byte) 1,
                null,
                null,
                new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT),
                (byte) 0,
                new GeneralDataCoding(Alphabet.ALPHA_DEFAULT),
                (byte) 0,
                message.getBytes(StandardCharsets.UTF_8)
            );
            
            logger.info("SMS sent successfully to: {}", phoneNumber);
            
        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
            throw new RuntimeException("Failed to send SMS", e);
        } finally {
            if (session != null && session.getSessionState().isBound()) {
                session.unbindAndClose();
            }
        }
    }
    
    public boolean isConfigured() { return configured; }
}