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
import org.json.JSONObject;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.bean.RequestParameter;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.util.WFConstant;
import org.wso2.carbon.identity.workflow.mgt.workflow.WorkFlowExecutor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Workflow executor that hands the request over to an external workflow system (e.g. WSO2 Integrator).
 * The external system decides on the request and reports the decision back through the workflow callback API,
 * using the correlation id sent in the payload.
 */
public class ExternalWorkflowRequestExecutor implements WorkFlowExecutor {

    private static final Log LOG = LogFactory.getLog(ExternalWorkflowRequestExecutor.class);
    private static final String EXECUTOR_NAME = "External Workflow Engine";

    private List<Parameter> parameterList = new ArrayList<>();

    @Override
    public boolean canHandle(WorkflowRequest workflowRequest) {

        return true;
    }

    @Override
    public void initialize(List<Parameter> parameterList) {

        if (parameterList != null) {
            this.parameterList = parameterList;
        }
    }

    @Override
    public void execute(WorkflowRequest request) throws WorkflowException {

        if (StringUtils.isBlank(ExternalWorkflowEngineConfig.getEndpoint())) {
            throw new WorkflowException("External workflow engine endpoint is not configured. Configure " +
                    "[workflow_engine.external] endpoint in deployment.toml.");
        }

        // The uuid of the request handed to the executor is the workflow relationship id, which is what the
        // workflow framework expects in the callback.
        String correlationId = request.getUuid();
        JSONObject payload = new JSONObject();
        payload.put("correlationId", correlationId);
        payload.put("eventType", request.getEventType());
        payload.put("tenantDomain", IdentityTenantUtil.getTenantDomain(request.getTenantId()));
        payload.put("initiatedBy", CarbonContext.getThreadLocalCarbonContext().getUsername());
        payload.put("createdAt", Instant.now().toString());

        JSONObject parameters = new JSONObject();
        for (RequestParameter parameter : request.getRequestParameters()) {
            if (WorkflowRequest.CREDENTIAL.equals(parameter.getName()) || parameter.getValue() == null) {
                continue;
            }
            if (WFConstant.REQUEST_ID.equals(parameter.getName())) {
                payload.put("requestId", parameter.getValue());
                continue;
            }
            parameters.put(parameter.getName(), JSONObject.wrap(parameter.getValue()));
        }
        payload.put("parameters", parameters);

        JSONObject workflowParameters = new JSONObject();
        for (Parameter parameter : parameterList) {
            String key = StringUtils.isNotBlank(parameter.getqName()) ? parameter.getqName() :
                    parameter.getParamName();
            workflowParameters.put(key, parameter.getParamValue());
        }
        payload.put("workflowParameters", workflowParameters);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Dispatching workflow request: " + correlationId + " of type: " + request.getEventType() +
                    " to the external workflow engine.");
        }
        ExternalWorkflowRequestDispatcher.getInstance().dispatch(correlationId, payload.toString());
    }

    @Override
    public String getName() {

        return EXECUTOR_NAME;
    }
}
