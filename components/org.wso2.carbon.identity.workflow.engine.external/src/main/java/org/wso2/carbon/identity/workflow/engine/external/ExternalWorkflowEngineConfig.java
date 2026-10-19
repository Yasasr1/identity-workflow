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
import org.wso2.carbon.identity.core.util.IdentityUtil;

/**
 * Configuration of the external workflow engine, read from identity.xml
 * ({@code [workflow_engine.external]} in deployment.toml).
 */
public class ExternalWorkflowEngineConfig {

    public static final String ENGINE_ID = "ExternalWorkflowEngine";
    public static final String TEMPLATE_ID = "MultiStepApprovalTemplate";

    private static final String PREFIX = "WorkflowEngine.ExternalEngine.";
    private static final String ENDPOINT = PREFIX + "Endpoint";
    private static final String AUTH_TYPE = PREFIX + "AuthType";
    private static final String USERNAME = PREFIX + "Username";
    private static final String PASSWORD = PREFIX + "Password";
    private static final String ACCESS_TOKEN = PREFIX + "AccessToken";
    private static final String TIMEOUT = PREFIX + "TimeoutMs";
    private static final String MAX_RETRIES = PREFIX + "MaxRetries";
    private static final String DISPATCH_DELAY = PREFIX + "DispatchDelayMs";

    public static final String AUTH_TYPE_BASIC = "BASIC";
    public static final String AUTH_TYPE_BEARER = "BEARER";
    public static final String AUTH_TYPE_NONE = "NONE";

    private ExternalWorkflowEngineConfig() {

    }

    public static String getEndpoint() {

        return IdentityUtil.getProperty(ENDPOINT);
    }

    public static String getAuthType() {

        String authType = IdentityUtil.getProperty(AUTH_TYPE);
        return StringUtils.isBlank(authType) ? AUTH_TYPE_NONE : authType.trim().toUpperCase();
    }

    public static String getUsername() {

        return IdentityUtil.getProperty(USERNAME);
    }

    public static String getPassword() {

        return IdentityUtil.getProperty(PASSWORD);
    }

    public static String getAccessToken() {

        return IdentityUtil.getProperty(ACCESS_TOKEN);
    }

    public static int getTimeoutMs() {

        return getIntProperty(TIMEOUT, 10000);
    }

    public static int getMaxRetries() {

        return getIntProperty(MAX_RETRIES, 3);
    }

    /**
     * The workflow framework persists the request-workflow relationship only after the executor returns, so the
     * dispatch is delayed slightly to make sure an immediate (e.g. auto-approval) callback can find it.
     */
    public static int getDispatchDelayMs() {

        return getIntProperty(DISPATCH_DELAY, 1000);
    }

    private static int getIntProperty(String key, int defaultValue) {

        String value = IdentityUtil.getProperty(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
