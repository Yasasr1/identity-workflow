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

package org.wso2.carbon.identity.workflow.engine.external.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.identity.workflow.engine.external.ExternalApprovalWorkflow;
import org.wso2.carbon.identity.workflow.engine.external.ExternalWorkflowEngineConfig;
import org.wso2.carbon.identity.workflow.engine.external.ExternalWorkflowRequestDispatcher;
import org.wso2.carbon.identity.workflow.engine.external.ExternalWorkflowRequestExecutor;
import org.wso2.carbon.identity.workflow.mgt.workflow.AbstractWorkflow;

/**
 * Registers the external workflow engine as a workflow implementation.
 */
@Component(
        name = "workflow.engine.external",
        immediate = true)
public class ExternalWorkflowEngineServiceComponent {

    private static final Log LOG = LogFactory.getLog(ExternalWorkflowEngineServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        context.getBundleContext().registerService(AbstractWorkflow.class,
                new ExternalApprovalWorkflow(ExternalApprovalWorkflow.ExternalTemplateInitializer.class,
                        ExternalWorkflowRequestExecutor.class, getMetaDataXML()), null);
        LOG.info("External workflow engine: " + ExternalWorkflowEngineConfig.ENGINE_ID + " registered.");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        ExternalWorkflowRequestDispatcher.getInstance().shutdown();
    }

    private String getMetaDataXML() {

        return "<met:MetaData xmlns:met=\"http://metadata.bean.mgt.workflow.identity.carbon.wso2.org\">\n" +
                "<met:WorkflowImpl>\n" +
                "    <met:WorkflowImplId>" + ExternalWorkflowEngineConfig.ENGINE_ID + "</met:WorkflowImplId>\n" +
                "    <met:WorkflowImplName>" + ExternalWorkflowEngineConfig.ENGINE_ID + "</met:WorkflowImplName>\n" +
                "    <met:WorkflowImplDescription>Delegates approvals to an external workflow system" +
                "</met:WorkflowImplDescription>\n" +
                "    <met:TemplateId>" + ExternalWorkflowEngineConfig.TEMPLATE_ID + "</met:TemplateId>\n" +
                "    <met:ParametersMetaData/>\n" +
                "</met:WorkflowImpl>\n" +
                "</met:MetaData>";
    }
}
