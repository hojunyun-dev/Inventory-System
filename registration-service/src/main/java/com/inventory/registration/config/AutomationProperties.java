package com.inventory.registration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "automation")
public class AutomationProperties {
    
    private Browser browser = new Browser();
    private TestAccounts testAccounts = new TestAccounts();
    
    @Data
    public static class Browser {
        private boolean headless = true;
        private long timeout = 30000;
        private String windowSize = "1920,1080";
        private String remoteUrl = "";
    }
    
    @Data
    public static class TestAccounts {
        private Naver naver = new Naver();
        
        @Data
        public static class Naver {
            private String username = "";
            private String password = "";
        }
    }
}



