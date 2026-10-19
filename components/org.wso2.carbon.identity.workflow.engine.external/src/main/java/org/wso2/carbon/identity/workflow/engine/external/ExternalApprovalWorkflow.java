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

import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.bean.metadata.InputData;
import org.wso2.carbon.identity.workflow.mgt.bean.metadata.ParameterMetaData;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowRuntimeException;
import org.wso2.carbon.identity.workflow.mgt.workflow.AbstractWorkflow;
import org.wso2.carbon.identity.workflow.mgt.workflow.TemplateInitializer;
import org.wso2.carbon.identity.workflow.mgt.workflow.WorkFlowExecutor;

import java.util.List;

/**
 * Workflow implementation that delegates the approval to an external workflow system.
 */
public class ExternalApprovalWorkflow extends AbstractWorkflow {

    public ExternalApprovalWorkflow(Class<? extends TemplateInitializer> templateInitializerClass,
                                    Class<? extends WorkFlowExecutor> workFlowExecutorClass, String metaDataXML)
            throws WorkflowRuntimeException {

        super(templateInitializerClass, workFlowExecutorClass, metaDataXML);
    }

    @Override
    protected InputData getInputData(ParameterMetaData parameterMetaData) {

        return null;
    }

    /**
     * No-op template initializer: nothing has to be deployed for the external engine.
     */
    public static class ExternalTemplateInitializer implements TemplateInitializer {

        @Override
        public boolean initNeededAtStartUp() {

            return false;
        }

        @Override
        public void initialize(List<Parameter> parameterList) {

        }
    }
}
