package com.example.common.dto;

public class CookieEntry {
    public String name;
    public String value;
    public String domain;
    public String path;
    public Long expiryEpochSec; // nullable
    public Boolean httpOnly;
    public Boolean secure;

    public CookieEntry() {}
    
    public CookieEntry(String name, String value) { 
        this.name = name; 
        this.value = value; 
    }
    
    public CookieEntry(String name, String value, String domain, String path, Long expiryEpochSec, Boolean httpOnly, Boolean secure) {
        this.name = name;
        this.value = value;
        this.domain = domain;
        this.path = path;
        this.expiryEpochSec = expiryEpochSec;
        this.httpOnly = httpOnly;
        this.secure = secure;
    }
}


