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

package org.apache.skywalking.apm.plugin.grpc.v1.server;

import io.grpc.ForwardingServerCallListener;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.ContextSnapshot;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;
import org.apache.skywalking.apm.plugin.grpc.v1.OperationNameFormatUtil;

import static org.apache.skywalking.apm.plugin.grpc.v1.Constants.*;

/**
 * @author wang zheng, kanro
 */
public class TracingServerCallListener<REQUEST> extends ForwardingServerCallListener.SimpleForwardingServerCallListener<REQUEST> {

    private final ContextSnapshot contextSnapshot;
    private final MethodDescriptor.MethodType methodType;
    private final String operationPrefix;

    protected TracingServerCallListener(ServerCall.Listener<REQUEST> delegate, MethodDescriptor<REQUEST, ?> descriptor,
                                        ContextSnapshot contextSnapshot) {
        super(delegate);
        this.contextSnapshot = contextSnapshot;
        this.methodType = descriptor.getType();
        this.operationPrefix = OperationNameFormatUtil.formatOperationName(descriptor) + SERVER;
    }

    @Override
    public void onMessage(REQUEST message) {
        // We just create the request on message span for client stream calls.
        if (!methodType.clientSendsOneMessage()) {
            try {
                final AbstractSpan span = ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_MESSAGE_OPERATION_NAME);
                span.setComponent(ComponentsDefine.GRPC);
                span.setLayer(SpanLayer.RPC_FRAMEWORK);
                ContextManager.continued(contextSnapshot);
            } catch (Throwable t) {
                ContextManager.activeSpan().errorOccurred().log(t);
                throw t;
            } finally {
                super.onMessage(message);
                ContextManager.stopSpan();
            }
        } else {
            super.onMessage(message);
        }
    }

    @Override
    public void onCancel() {
        try {
            final AbstractSpan span = ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_CANCEL_OPERATION_NAME);
            span.setComponent(ComponentsDefine.GRPC);
            span.setLayer(SpanLayer.RPC_FRAMEWORK);
            ContextManager.continued(contextSnapshot);
        } catch (Throwable t) {
            ContextManager.activeSpan().errorOccurred().log(t);
            throw t;
        } finally {
            super.onCancel();
            ContextManager.stopSpan();
        }
    }

    @Override
    public void onHalfClose() {
        try {
            final AbstractSpan span = ContextManager.createLocalSpan(operationPrefix + REQUEST_ON_COMPLETE_OPERATION_NAME);
            span.setComponent(ComponentsDefine.GRPC);
            span.setLayer(SpanLayer.RPC_FRAMEWORK);
            ContextManager.continued(contextSnapshot);
        } catch (Throwable t) {
            ContextManager.activeSpan().errorOccurred().log(t);
            throw t;
        } finally {
            super.onHalfClose();
            ContextManager.stopSpan();
        }
    }
}
