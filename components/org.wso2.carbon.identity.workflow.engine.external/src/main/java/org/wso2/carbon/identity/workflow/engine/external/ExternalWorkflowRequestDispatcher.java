/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.workflow.engine.external;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Sends workflow requests to the external workflow system asynchronously, with retries.
 */
public class ExternalWorkflowRequestDispatcher {

    private static final Log LOG = LogFactory.getLog(ExternalWorkflowRequestDispatcher.class);
    private static final ExternalWorkflowRequestDispatcher INSTANCE = new ExternalWorkflowRequestDispatcher();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(ExternalWorkflowEngineConfig.getTimeoutMs()))
            .build();

    private ExternalWorkflowRequestDispatcher() {

    }

    public static ExternalWorkflowRequestDispatcher getInstance() {

        return INSTANCE;
    }

    /**
     * Schedule the payload to be sent to the configured endpoint.
     *
     * @param correlationId Workflow relationship id the external system must call back with.
     * @param payload       JSON payload.
     */
    public void dispatch(String correlationId, String payload) {

        scheduler.schedule(() -> send(correlationId, payload, 1),
                ExternalWorkflowEngineConfig.getDispatchDelayMs(), TimeUnit.MILLISECONDS);
    }

    public void shutdown() {

        scheduler.shutdownNow();
    }

    private void send(String correlationId, String payload, int attempt) {

        String endpoint = ExternalWorkflowEngineConfig.getEndpoint();
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofMillis(ExternalWorkflowEngineConfig.getTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));
            String authHeader = getAuthorizationHeader();
            if (authHeader != null) {
                builder.header("Authorization", authHeader);
            }
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Workflow request: " + correlationId + " delivered to external workflow engine. " +
                            "Status: " + status);
                }
                return;
            }
            LOG.warn("External workflow engine returned status: " + status + " for workflow request: " +
                    correlationId + " (attempt " + attempt + ").");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Interrupted while sending workflow request: " + correlationId, e);
            return;
        } catch (Exception e) {
            LOG.warn("Error while sending workflow request: " + correlationId + " to external workflow engine at: " +
                    endpoint + " (attempt " + attempt + ").", e);
        }
        if (attempt < ExternalWorkflowEngineConfig.getMaxRetries()) {
            long backoff = (long) Math.pow(2, attempt) * 1000L;
            scheduler.schedule(() -> send(correlationId, payload, attempt + 1), backoff, TimeUnit.MILLISECONDS);
        } else {
            LOG.error("Giving up delivering workflow request: " + correlationId + " to external workflow engine " +
                    "after " + attempt + " attempts. The request stays pending until aborted.");
        }
    }

    private String getAuthorizationHeader() {

        String authType = ExternalWorkflowEngineConfig.getAuthType();
        if (ExternalWorkflowEngineConfig.AUTH_TYPE_BASIC.equals(authType)) {
            String credentials = StringUtils.defaultString(ExternalWorkflowEngineConfig.getUsername()) + ":" +
                    StringUtils.defaultString(ExternalWorkflowEngineConfig.getPassword());
            return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        }
        if (ExternalWorkflowEngineConfig.AUTH_TYPE_BEARER.equals(authType)) {
            return "Bearer " + StringUtils.defaultString(ExternalWorkflowEngineConfig.getAccessToken());
        }
        return null;
    }
}
