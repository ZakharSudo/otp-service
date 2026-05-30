package com.otp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Service
public class TelegramNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationService.class);
    private final String botToken;
    private final String defaultChatId;
    private final String telegramApiUrl;
    private boolean configured = false;
    
    public TelegramNotificationService() {
        Properties config = loadConfig();
        this.botToken = config.getProperty("telegram.bot.token", "");
        this.defaultChatId = config.getProperty("telegram.chat.id", "");
        this.telegramApiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        
        if (!botToken.isEmpty() && !botToken.equals("YOUR_BOT_TOKEN_HERE")) {
            this.configured = true;
            logger.info("Telegram notification service configured");
            testConnection();
        } else {
            logger.warn("Telegram notification service not configured - update telegram.properties");
        }
    }
    
    private Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("telegram.properties")) {
            if (input != null) {
                props.load(input);
            } else {
                logger.warn("telegram.properties not found");
            }
        } catch (IOException e) {
            logger.warn("Failed to load telegram configuration: {}", e.getMessage());
        }
        return props;
    }
    
    private void testConnection() {
        try {
            String url = telegramApiUrl + "?chat_id=" + defaultChatId + "&text=OTP%20Service%20started";
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                logger.info("Telegram bot connected successfully");
            } else {
                logger.warn("Telegram bot test failed: {}", response.body());
            }
        } catch (Exception e) {
            logger.warn("Telegram bot test failed: {}", e.getMessage());
        }
    }
    
    public void sendCode(String chatId, String code) {
        if (!configured) {
            logger.warn("Telegram not configured - would send code {} to {}", code, chatId);
            return;
        }
        
        String targetChatId = (chatId != null && !chatId.isEmpty()) ? chatId : defaultChatId;
        String message = String.format("🔐 Your verification code is: %s\n\nThis code will expire in 5 minutes.\n\nIf you didn't request this, please ignore this message.", code);
        String url = String.format("%s?chat_id=%s&text=%s", telegramApiUrl, targetChatId, urlEncode(message));
        sendTelegramRequest(url);
    }
    
    private void sendTelegramRequest(String url) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.error("Telegram API error: {}", response.body());
            } else {
                logger.info("Telegram message sent successfully");
            }
        } catch (InterruptedException e) {
            logger.error("Error sending Telegram message: {}", e.getMessage());
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            logger.error("Error sending Telegram message: {}", e.getMessage());
        }
    }
    
    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
    
    public boolean isConfigured() { return configured; }
    public String getDefaultChatId() { return defaultChatId; }
}