package com.inventory.registration.constants;

public class PlatformConstants {
    
    // 플랫폼 이름
    public static final String BUNJANG = "bunjang";
    public static final String DANGGEUN = "danggeun";
    public static final String JUNGGONARA = "junggonara";
    
    // 번개장터 URL
    public static final String BUNJANG_BASE_URL = "https://m.bunjang.co.kr";
    public static final String BUNJANG_LOGIN_URL = BUNJANG_BASE_URL + "/login";
    public static final String BUNJANG_REGISTER_URL = BUNJANG_BASE_URL + "/products/new";
    
    // 당근마켓 URL (웹 버전)
    public static final String DANGGEUN_BASE_URL = "https://www.daangn.com";
    public static final String DANGGEUN_LOGIN_URL = DANGGEUN_BASE_URL + "/login";
    public static final String DANGGEUN_REGISTER_URL = DANGGEUN_BASE_URL + "/products/new";
    
    // 중고나라 URL
    public static final String JUNGGONARA_BASE_URL = "https://www.joonggonara.co.kr";
    public static final String JUNGGONARA_LOGIN_URL = JUNGGONARA_BASE_URL + "/login";
    public static final String JUNGGONARA_REGISTER_URL = JUNGGONARA_BASE_URL + "/write";
    
    // 공통 타임아웃 설정 (밀리초)
    public static final long DEFAULT_TIMEOUT = 10000;
    public static final long LONG_TIMEOUT = 30000;
    public static final long SHORT_TIMEOUT = 5000;
    
    // 재시도 설정
    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final long RETRY_DELAY_MS = 2000;
    
    // 대기 시간
    public static final long IMPLICIT_WAIT = 10;
    public static final long PAGE_LOAD_WAIT = 30;
}
