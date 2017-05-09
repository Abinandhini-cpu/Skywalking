package org.skywalking.apm.plugin.resin.v3;

import com.caucho.server.http.HttpResponse;
import com.caucho.server.http.HttpRequest;
import org.skywalking.apm.agent.core.context.ContextCarrier;
import org.skywalking.apm.agent.core.context.ContextManager;
import org.skywalking.apm.agent.core.plugin.interceptor.EnhancedClassInstanceContext;
import org.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodInvokeContext;
import org.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.skywalking.apm.trace.Span;
import org.skywalking.apm.trace.tag.Tags;
import org.skywalking.apm.util.StringUtil;

/**
 * {@link ResinV3Interceptor} intercept method of{@link com.caucho.server.dispatch.ServletInvocation#service(javax.servlet.ServletRequest,
 * javax.servlet.ServletResponse)} record the resin host, port ,url.
 *
 * @author baiyang
 */
public class ResinV3Interceptor implements InstanceMethodsAroundInterceptor {
    /**
     * Header name that the serialized context data stored in
     * {@link HttpRequest#getHeader(String)}.
     */
    public static final String HEADER_NAME_OF_CONTEXT_DATA = "SWTraceContext";
    /**
     * Resin component.
     */
    public static final String RESIN_COMPONENT = "Resin";

    @Override
    public void beforeMethod(EnhancedClassInstanceContext context, InstanceMethodInvokeContext interceptorContext,
        MethodInterceptResult result) {
        Object[] args = interceptorContext.allArguments();
        HttpRequest request = (HttpRequest)args[0];
        Span span = ContextManager.createSpan(request.getRequestURI());
        Tags.COMPONENT.set(span, RESIN_COMPONENT);
        Tags.PEER_HOST.set(span, request.getServerName());
        Tags.PEER_PORT.set(span, request.getServerPort());
        Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_SERVER);
        Tags.URL.set(span, request.getRequestURL().toString());
        Tags.SPAN_LAYER.asHttp(span);

        String tracingHeaderValue = request.getHeader(HEADER_NAME_OF_CONTEXT_DATA);
        if (!StringUtil.isEmpty(tracingHeaderValue)) {
            ContextManager.extract(new ContextCarrier().deserialize(tracingHeaderValue));
        }
    }

    @Override
    public Object afterMethod(EnhancedClassInstanceContext context, InstanceMethodInvokeContext interceptorContext,
        Object ret) {
        HttpResponse response = (HttpResponse)interceptorContext.allArguments()[1];
        Span span = ContextManager.activeSpan();
        Tags.STATUS_CODE.set(span, response.getStatusCode());

        if (response.getStatusCode() != 200) {
            Tags.ERROR.set(span, true);
        }
        ContextManager.stopSpan();
        return ret;
    }

    @Override
    public void handleMethodException(Throwable t, EnhancedClassInstanceContext context,
        InstanceMethodInvokeContext interceptorContext) {
        Span span = ContextManager.activeSpan();
        span.log(t);
        Tags.ERROR.set(span, true);
    }
}
