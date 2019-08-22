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

package org.apache.skywalking.apm.plugin.spring.resttemplate.async;

import java.lang.reflect.Method;
import java.net.URI;
import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import org.apache.skywalking.apm.agent.core.conf.Config;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.ContextSnapshot;
import org.apache.skywalking.apm.agent.core.context.OperationNameFormatService;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;
import org.apache.skywalking.apm.plugin.spring.commons.EnhanceCacheObjects;
import org.springframework.http.HttpMethod;

public class RestExecuteInterceptor implements InstanceMethodsAroundInterceptor {
    private OperationNameFormatService nameFormatService;

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
        MethodInterceptResult result) throws Throwable {
        final URI requestURL = (URI)allArguments[0];
        final HttpMethod httpMethod = (HttpMethod)allArguments[1];
        final ContextCarrier contextCarrier = new ContextCarrier();

        if (nameFormatService == null) {
            nameFormatService = ServiceManager.INSTANCE.findService(OperationNameFormatService.class);
        }

        String remotePeer = requestURL.getHost() + ":" + (requestURL.getPort() > 0 ? requestURL.getPort() : "https".equalsIgnoreCase(requestURL.getScheme()) ? 443 : 80);

        String formatURIPath = nameFormatService.formatOperationName(Config.Plugin.OPGroup.RestTemplate.class, requestURL.getPath());
        AbstractSpan span = ContextManager.createExitSpan(
            formatURIPath,
            contextCarrier, remotePeer);

        span.setComponent(ComponentsDefine.SPRING_REST_TEMPLATE);
        Tags.URL.set(span, requestURL.getScheme() + "://" + requestURL.getHost() + ":" + requestURL.getPort() + requestURL.getPath());
        Tags.HTTP.METHOD.set(span, httpMethod.toString());
        SpanLayer.asHttp(span);
        Object[] cacheValues = new Object[3];
        cacheValues[0] = formatURIPath;
        cacheValues[1] = contextCarrier;
        objInst.setSkyWalkingDynamicField(cacheValues);
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
        Object ret) throws Throwable {
        Object[] cacheValues = (Object[])objInst.getSkyWalkingDynamicField();
        cacheValues[2] = ContextManager.capture();
        if (ret != null) {
            URI uri = (URI)cacheValues[0];
            ((EnhancedInstance)ret).setSkyWalkingDynamicField(new EnhanceCacheObjects(uri.getPath(), ComponentsDefine.SPRING_REST_TEMPLATE, SpanLayer.HTTP, (ContextSnapshot)cacheValues[2]));
        }
        ContextManager.stopSpan();
        return ret;
    }

    @Override public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments,
        Class<?>[] argumentsTypes, Throwable t) {
        ContextManager.activeSpan().errorOccurred().log(t);
    }
}
