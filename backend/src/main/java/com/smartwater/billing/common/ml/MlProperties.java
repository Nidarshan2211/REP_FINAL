package com.smartwater.billing.common.ml;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ml")
public class MlProperties {

    private boolean enabled = true;
    private String baseUrl = "http://localhost:8000";
    private String apiKey = "";
    private int timeoutMs = 5000;
    private int batchSize = 500;
    private Thresholds thresholds = new Thresholds();

    public static class Thresholds {
        private double low = 0.50;
        private double high = 0.75;

        public double getLow() { return low; }
        public void setLow(double low) { this.low = low; }

        public double getHigh() { return high; }
        public void setHigh(double high) { this.high = high; }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }

    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }

    public Thresholds getThresholds() { return thresholds; }
    public void setThresholds(Thresholds thresholds) { this.thresholds = thresholds; }
}
