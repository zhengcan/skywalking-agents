/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.skywalking.apm.plugin.play.v2x;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;
import play.mvc.Http;
import play.mvc.Result;

import java.lang.reflect.Method;

/**
 * @author Zheng Can
 * 2019-12-11
 */
public class ControllerInterceptor extends AbstractActionInterceptor {
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        String controllerName = Helper.substringBefore(objInst.getClass().getSimpleName(), "$$");
        String operationName = controllerName + "." + method.getName();
        AbstractSpan span = ContextManager.createLocalSpan(operationName);
        span.setComponent(ComponentsDefine.PLAY);
        span.tag(Helper.TAG_ACTION_METHOD, method.toGenericString());

        Http.Request request = null;
        for (Object arg : allArguments) {
            if (arg instanceof Http.Request) {
                request = (Http.Request) arg;
            }
        }
        if (request != null) {
            span.tag(Helper.TAG_REQUEST_ATTR, request.attrs().toString());
            request.attrs().getOptional(Helper.KEY_SNAPSHOT)
                .ifPresent(ContextManager::continued);
        }

        if (method.getReturnType().equals(Result.class)) {
            // Ignore
        } else {
            span.prepareForAsync();
        }
    }
}
