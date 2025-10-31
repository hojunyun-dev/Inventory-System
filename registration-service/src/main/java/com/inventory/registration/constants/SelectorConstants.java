package com.inventory.registration.constants;

public class SelectorConstants {
    
            // 번개장터 셀렉터 - 실제 웹사이트 구조 기반
            public static class Bunjang {
                    // 네이버 OAuth 로그인 관련 - 안정적인 셀렉터 우선 사용
                    public static final String NAVER_LOGIN_BUTTON = "//button[contains(text(), '네이버로 이용하기')] | //a[contains(text(), '네이버')] | //button[contains(@class, 'naver')]";
                    public static final String NAVER_ID_INPUT = "#id, input#id, input[name='id'], input[name='loginId'], input[placeholder='아이디'], input[placeholder='아이디 또는 전화번호'], input[type='text'][name*='id'], input[id*='id']";
                    public static final String NAVER_PASSWORD_INPUT = "#pw, input#pw, input[name='pw'], input[name='password'], input[placeholder='비밀번호'], input[type='password'], input[name*='password']";
                    public static final String NAVER_LOGIN_SUBMIT = "//button[contains(text(), '로그인')] | //input[@type='submit'] | button[type='submit']";
                    public static final String LOGIN_SUCCESS_INDICATOR = ".header__user, .user-info, [data-testid='user-menu'], [aria-label*='사용자'], [data-testid*='user']";
        
        // 상품 등록 관련 - 실제 번개장터 구조
        public static final String PRODUCT_NAME_INPUT = "input[placeholder='상품명을 입력해 주세요.']";
        public static final String PRODUCT_PRICE_INPUT = "input[placeholder='가격을 입력해 주세요.']";
        public static final String PRODUCT_DESCRIPTION_TEXTAREA = "textarea";
        public static final String PRODUCT_TAG_INPUT = "input[placeholder='태그를 입력해 주세요. (최대 5개)']";
        public static final String PRODUCT_QUANTITY_INPUT = "input[placeholder='숫자만 입력해 주세요.']";
        public static final String PRODUCT_IMAGE_UPLOAD = "input[type='file']";
        
        // 데스크탑 홈 상단 로그인/회원가입 버튼
        public static final String HOME_LOGIN_BUTTON = "//button[contains(text(), '로그인/회원가입')] | //a[contains(text(),'로그인/회원가입')]";
        // 로그인 팝업 컨테이너 타이틀
        public static final String LOGIN_POPUP_CONTAINER = "//div[contains(., '번개장터로 중고거래 시작하기')]";

        // 카테고리 버튼들 (실제 번개장터 카테고리)
        public static final String CATEGORY_WOMEN_CLOTHING = "button:contains('여성의류')";
        public static final String CATEGORY_MEN_CLOTHING = "button:contains('남성의류')";
        public static final String CATEGORY_SHOES = "button:contains('신발')";
        public static final String CATEGORY_BAGS = "button:contains('가방/지갑')";
        public static final String CATEGORY_WATCHES = "button:contains('시계')";
        public static final String CATEGORY_JEWELRY = "button:contains('쥬얼리')";
        public static final String CATEGORY_FASHION_ACCESSORIES = "button:contains('패션 액세서리')";
        public static final String CATEGORY_DIGITAL = "button:contains('디지털')";
        public static final String CATEGORY_APPLIANCES = "button:contains('가전제품')";
        public static final String CATEGORY_SPORTS = "button:contains('스포츠/레저')";
        public static final String CATEGORY_VEHICLES = "button:contains('차량/오토바이')";
        public static final String CATEGORY_STAR_GOODS = "button:contains('스타굿즈')";
        public static final String CATEGORY_KIDULT = "button:contains('키덜트')";
        public static final String CATEGORY_ART = "button:contains('예술/희귀/수집품')";
        public static final String CATEGORY_MUSIC = "button:contains('음반/악기')";
        public static final String CATEGORY_BOOKS = "button:contains('도서/티켓/문구')";
        public static final String CATEGORY_BEAUTY = "button:contains('뷰티/미용')";
        public static final String CATEGORY_FURNITURE = "button:contains('가구/인테리어')";
        public static final String CATEGORY_LIVING = "button:contains('생활/주방용품')";
        public static final String CATEGORY_TOOLS = "button:contains('공구/산업용품')";
        public static final String CATEGORY_FOOD = "button:contains('식품')";
        public static final String CATEGORY_KIDS = "button:contains('유아동/출산')";
        public static final String CATEGORY_PETS = "button:contains('반려동물용품')";
        public static final String CATEGORY_ETC = "button:contains('기타')";
        public static final String CATEGORY_TALENT = "button:contains('재능')";
        
        // 상품 상태
        public static final String PRODUCT_CONDITION_NEW = "input[value='NEW']";
        public static final String PRODUCT_CONDITION_USED = "input[value='USED']";
        
        // 추가 옵션
        public static final String PRICE_NEGOTIATION_CHECKBOX = "input[name='price_negotiation']";
        
        // 배송 관련
        public static final String DELIVERY_FEE_INCLUDED = "span:contains('배송비포함')";
        public static final String DELIVERY_FEE_SEPARATE = "span:contains('배송비별도')";
        
        // 직거래 관련
        public static final String DIRECT_TRADE_YES = "span:contains('가능')";
        public static final String DIRECT_TRADE_NO = "span:contains('불가')";
        
        // 버튼들
        public static final String SUBMIT_BUTTON = "button:contains('등록하기')";
        public static final String TEMP_SAVE_BUTTON = "button:contains('임시저장')";
        public static final String SUCCESS_MESSAGE = ".success-message";
    }
    
    // 당근마켓 셀렉터
    public static class Danggeun {
        // 로그인 관련
        public static final String LOGIN_PHONE_INPUT = "input[name='phone']";
        public static final String LOGIN_VERIFICATION_INPUT = "input[name='verification']";
        public static final String LOGIN_BUTTON = "button[type='submit']";
        public static final String LOGIN_SUCCESS_INDICATOR = ".user-info";
        
        // 상품 등록 관련
        public static final String PRODUCT_TITLE_INPUT = "input[name='title']";
        public static final String PRODUCT_PRICE_INPUT = "input[name='price']";
        public static final String PRODUCT_CONTENT_TEXTAREA = "textarea[name='content']";
        public static final String PRODUCT_CATEGORY_SELECT = "select[name='category']";
        public static final String PRODUCT_LOCATION_SELECT = "select[name='location']";
        public static final String PRODUCT_IMAGE_UPLOAD = "input[type='file']";
        public static final String SUBMIT_BUTTON = "button[type='submit']";
        public static final String SUCCESS_MESSAGE = ".alert-success";
    }
    
    // 중고나라 셀렉터
    public static class Junggonara {
        // 로그인 관련
        public static final String LOGIN_ID_INPUT = "input[name='user_id']";
        public static final String LOGIN_PASSWORD_INPUT = "input[name='password']";
        public static final String LOGIN_BUTTON = "input[type='submit']";
        public static final String LOGIN_SUCCESS_INDICATOR = ".login-info";
        
        // 상품 등록 관련
        public static final String PRODUCT_SUBJECT_INPUT = "input[name='subject']";
        public static final String PRODUCT_CONTENT_TEXTAREA = "textarea[name='content']";
        public static final String PRODUCT_PRICE_INPUT = "input[name='price']";
        public static final String PRODUCT_LOCATION_INPUT = "input[name='location']";
        public static final String PRODUCT_CATEGORY_SELECT = "select[name='category']";
        public static final String PRODUCT_IMAGE_UPLOAD = "input[type='file']";
        public static final String SUBMIT_BUTTON = "input[type='submit']";
        public static final String SUCCESS_MESSAGE = ".notice";
    }
    
    // 공통 셀렉터
    public static class Common {
        public static final String LOADING_SPINNER = ".loading, .spinner";
        public static final String ERROR_MESSAGE = ".error, .alert-danger";
        public static final String SUCCESS_ALERT = ".success, .alert-success";
        public static final String CAPTCHA_IFRAME = "iframe[src*='captcha']";
        public static final String CAPTCHA_IMAGE = ".captcha-image";
        public static final String CAPTCHA_INPUT = "input[name='captcha']";
    }
}
