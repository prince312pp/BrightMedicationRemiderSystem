package com.example.util;

// SMS removed by request – keep a no-op service to avoid references elsewhere
public class SmsService {
    public boolean isEnabled() { return false; }
    public boolean send(String to, String body) { return false; }
}
