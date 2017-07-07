package org.skywalking.apm.agent.core.context.trace;

import org.skywalking.apm.network.proto.SpanObject;

/**
 * The <code>LocalSpan</code> represents a normal tracing point, such as a local method.
 *
 * @author wusheng
 */
public class LocalSpan extends AbstractTracingSpan {

    public LocalSpan(int spanId, int parentSpanId, int operationId) {
        super(spanId, parentSpanId, operationId);
    }

    public LocalSpan(int spanId, int parentSpanId, String operationName) {
        super(spanId, parentSpanId, operationName);
    }

    @Override
    public LocalSpan tag(String key, String value) {
        super.tag(key, value);
        return this;
    }

    @Override
    public LocalSpan log(Throwable t) {
        super.log(t);
        return this;
    }

    @Override public SpanObject.Builder transform() {
        return null;
    }

    @Override public boolean isEntry() {
        return false;
    }

    @Override public boolean isLocal() {
        return true;
    }

    @Override public boolean isExit() {
        return false;
    }
}
