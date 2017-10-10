package org.skywalking.apm.plugin.sjdbc;

import com.dangdang.ddframe.rdb.sharding.executor.event.AbstractExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.DMLExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.DQLExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.threadlocal.ExecutorDataMap;
import com.dangdang.ddframe.rdb.sharding.util.EventBusInstance;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import java.util.stream.Collectors;
import org.skywalking.apm.agent.core.context.ContextManager;
import org.skywalking.apm.agent.core.context.ContextSnapshot;
import org.skywalking.apm.agent.core.context.tag.Tags;
import org.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.skywalking.apm.network.trace.component.ComponentsDefine;
import org.skywalking.apm.plugin.sjdbc.define.AsyncExecuteInterceptor;

/**
 * Sharding-jdbc provides {@link EventBusInstance} to help external systems get events about sql execution.
 * {@link ExecuteEventListener} can get sql statement start and end events, resulting in db span.
 * 
 * @author gaohongtao
 */
public class ExecuteEventListener {

    public static void init() {
        EventBusInstance.getInstance().register(new ExecuteEventListener());
    }

    @Subscribe
    @AllowConcurrentEvents
    public void listenDML(DMLExecutionEvent event) {
        handle(event, "MODIFY");
    }

    @Subscribe
    @AllowConcurrentEvents
    public void listenDQL(DQLExecutionEvent event) {
        handle(event, "QUERY");
    }
    
    private void handle(AbstractExecutionEvent event, String operation) {
        switch (event.getEventExecutionType()) {
            case BEFORE_EXECUTE:
                AbstractSpan span = ContextManager.createExitSpan("/SJDBC/BRANCH/" + operation, event.getDataSource());
                if (ExecutorDataMap.getDataMap().containsKey(AsyncExecuteInterceptor.SNAPSHOT_DATA_KEY)) {
                    ContextManager.continued((ContextSnapshot)ExecutorDataMap.getDataMap().get(AsyncExecuteInterceptor.SNAPSHOT_DATA_KEY));
                }
                Tags.DB_TYPE.set(span, "sql");
                Tags.DB_INSTANCE.set(span, event.getDataSource());
                Tags.DB_STATEMENT.set(span, event.getSql());
                if (!event.getParameters().isEmpty()) {
                    Tags.DB_BIND_VARIABLES.set(span, event.getParameters().stream().map(Object::toString).collect(Collectors.joining(",")));
                }
                span.setComponent(ComponentsDefine.SHARDING_JDBC);
                SpanLayer.asDB(span);
                break;
            case EXECUTE_FAILURE:
                span = ContextManager.activeSpan();
                span.errorOccurred();
                if (event.getException().isPresent()) {
                    span.log(event.getException().get());
                }
            case EXECUTE_SUCCESS:
                ContextManager.stopSpan();
        }
    }
}
