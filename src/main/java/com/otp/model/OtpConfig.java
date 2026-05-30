package com.otp.model;

public class OtpConfig {
    private Long id;
    private Integer codeLength;
    private Integer ttlSeconds;
    
    public OtpConfig() {
        this.codeLength = 6;
        this.ttlSeconds = 300;
    }
    
    public OtpConfig(Integer codeLength, Integer ttlSeconds) {
        this.codeLength = codeLength;
        this.ttlSeconds = ttlSeconds;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getCodeLength() { return codeLength; }
    public void setCodeLength(Integer codeLength) { this.codeLength = codeLength; }
    public Integer getTtlSeconds() { return ttlSeconds; }
    public void setTtlSeconds(Integer ttlSeconds) { this.ttlSeconds = ttlSeconds; }
}