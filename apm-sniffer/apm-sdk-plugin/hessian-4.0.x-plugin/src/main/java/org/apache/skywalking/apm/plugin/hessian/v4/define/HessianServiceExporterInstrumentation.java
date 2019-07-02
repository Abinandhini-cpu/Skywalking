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

package org.apache.skywalking.apm.plugin.hessian.v4.define;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.apache.skywalking.apm.agent.core.plugin.match.NameMatch.byName;
import static org.apache.skywalking.apm.plugin.hessian.v4.Constants.HESSIAN_SERVICE_EXPORTER_CLASS;
import static org.apache.skywalking.apm.plugin.hessian.v4.Constants.HESSIAN_SERVICE_EXPORTER_INTERCEOTPOR;

/**
 * @author Alan Lau
 */
public class HessianServiceExporterInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {
    @Override protected ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return null;
    }

    @Override protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[] {

            new InstanceMethodsInterceptPoint() {
                @Override public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named("handleRequest");
                }

                @Override public String getMethodsInterceptor() {
                    return HESSIAN_SERVICE_EXPORTER_INTERCEOTPOR;
                }

                @Override public boolean isOverrideArgs() {
                    return false;
                }
            },

        };
    }

    @Override protected ClassMatch enhanceClass() {
        return byName(HESSIAN_SERVICE_EXPORTER_CLASS);
    }
}
