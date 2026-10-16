package com.smartwater.billing.common.ml;

import com.smartwater.billing.common.dto.LeakDTOs.MlScoreRequest;
import com.smartwater.billing.common.dto.LeakDTOs.MlScoreResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
public class MlClient {

    private static final Logger log = LoggerFactory.getLogger(MlClient.class);
    private static final String API_KEY_HEADER = "X-API-KEY";

    private final MlProperties mlProperties;
    private RestClient restClient;
    private boolean clientOperational = false;

    public MlClient(MlProperties mlProperties) {
        this.mlProperties = mlProperties;
    }

    @PostConstruct
    public void init() {
        if (!mlProperties.isEnabled()) {
            log.info("ML leak detection is disabled (ml.enabled=false). Legacy statistical fallback will be used.");
            this.clientOperational = false;
            return;
        }

        String apiKey = mlProperties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.error("ML leak detection is enabled (ml.enabled=true) but ML_API_KEY is not configured! " +
                      "Failing ML client startup. All leak scoring will fall back gracefully to the statistical mean + 2*SD rule.");
            this.clientOperational = false;
            return;
        }

        try {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(mlProperties.getTimeoutMs());
            requestFactory.setReadTimeout(mlProperties.getTimeoutMs());

            this.restClient = RestClient.builder()
                    .baseUrl(mlProperties.getBaseUrl())
                    .requestFactory(requestFactory)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(API_KEY_HEADER, apiKey.trim())
                    .build();

            this.clientOperational = true;
            log.info("ML client successfully initialized. Target endpoint: {}/v1/leak/score (timeout: {}ms)",
                    mlProperties.getBaseUrl(), mlProperties.getTimeoutMs());
        } catch (Exception ex) {
            log.error("Failed to initialize ML RestClient: {}. Falling back to statistical rule.", ex.getMessage(), ex);
            this.clientOperational = false;
        }
    }

    public boolean isOperational() {
        return mlProperties.isEnabled() && clientOperational && restClient != null;
    }

    /**
     * Sends batch of flat usage features to ml-service.
     * Fail-safe: Returns Optional.empty() on any connection failure, timeout, or 4xx/5xx response.
     */
    public Optional<MlScoreResponse> scoreBatch(MlScoreRequest request) {
        if (!isOperational()) {
            log.warn("ML client is not operational or disabled. Skipping remote ML call.");
            return Optional.empty();
        }

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            log.debug("Empty ML request received, returning empty response.");
            return Optional.empty();
        }

        try {
            log.info("Sending batch of {} flats to ML service ({}/v1/leak/score)...",
                    request.getItems().size(), mlProperties.getBaseUrl());

            MlScoreResponse response = restClient.post()
                    .uri("/v1/leak/score")
                    .body(request)
                    .retrieve()
                    .body(MlScoreResponse.class);

            if (response != null && response.getResults() != null) {
                log.info("ML service returned {} scored results (model version: {})",
                        response.getResults().size(), response.getModelVersion());
                return Optional.of(response);
            } else {
                log.warn("ML service returned null or empty result list. Triggering fallback.");
                return Optional.empty();
            }
        } catch (RestClientException rce) {
            log.warn("ML service call failed or timed out ({}ms): {}. Falling back to statistical rule.",
                    mlProperties.getTimeoutMs(), rce.getMessage());
            return Optional.empty();
        } catch (Exception ex) {
            log.error("Unexpected error during ML service call: {}. Falling back to statistical rule.",
                    ex.getMessage(), ex);
            return Optional.empty();
        }
    }
}
