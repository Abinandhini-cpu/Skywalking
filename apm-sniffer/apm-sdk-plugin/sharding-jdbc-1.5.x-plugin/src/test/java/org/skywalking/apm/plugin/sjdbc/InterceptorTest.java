package org.skywalking.apm.plugin.sjdbc;

import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.sharding.executor.event.DMLExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.DQLExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.EventExecutionType;
import com.dangdang.ddframe.rdb.sharding.executor.threadlocal.ExecutorDataMap;
import com.dangdang.ddframe.rdb.sharding.util.EventBusInstance;
import com.google.common.base.Optional;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.hamcrest.core.Is;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.skywalking.apm.agent.core.context.trace.AbstractTracingSpan;
import org.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.skywalking.apm.agent.core.context.trace.TraceSegment;
import org.skywalking.apm.agent.test.helper.SegmentHelper;
import org.skywalking.apm.agent.test.tools.AgentServiceRule;
import org.skywalking.apm.agent.test.tools.SegmentStorage;
import org.skywalking.apm.agent.test.tools.SegmentStoragePoint;
import org.skywalking.apm.agent.test.tools.TracingSegmentRunner;
import org.skywalking.apm.network.trace.component.ComponentsDefine;
import org.skywalking.apm.plugin.sjdbc.define.AsyncExecuteInterceptor;
import org.skywalking.apm.plugin.sjdbc.define.ExecuteInterceptor;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.skywalking.apm.agent.test.tools.SpanAssert.assertComponent;
import static org.skywalking.apm.agent.test.tools.SpanAssert.assertLayer;
import static org.skywalking.apm.agent.test.tools.SpanAssert.assertOccurException;
import static org.skywalking.apm.agent.test.tools.SpanAssert.assertTag;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(TracingSegmentRunner.class)
public class InterceptorTest {
    
    private static ExecutorService ES;
    
    @SegmentStoragePoint
    private SegmentStorage segmentStorage;

    @Rule
    public AgentServiceRule serviceRule = new AgentServiceRule();
    
    private ExecuteInterceptor executeInterceptor;
    
    private AsyncExecuteInterceptor asyncExecuteInterceptor;

    private Object[] allArguments;
    
    @BeforeClass
    public static void init() {
        ExecuteEventListener.init();
        ES = Executors.newSingleThreadExecutor();
    }
    
    @AfterClass
    public static void finish() {
        ES.shutdown();
    }
    
    @Before
    public void setUp() throws SQLException {
        executeInterceptor = new ExecuteInterceptor();
        asyncExecuteInterceptor = new AsyncExecuteInterceptor();
        allArguments = new Object[]{SQLType.DQL, null};
    }
    
    @Test
    public void assertSyncExecute() throws Throwable {
        executeInterceptor.beforeMethod(null, null, allArguments, null, null);
        sendEvent("ds_0", "select * from t_order_0");
        executeInterceptor.afterMethod(null, null, allArguments, null, null);
        assertThat(segmentStorage.getTraceSegments().size(), is(1));
        TraceSegment segment = segmentStorage.getTraceSegments().get(0);
        List<AbstractTracingSpan> spans =  SegmentHelper.getSpans(segment);
        assertNotNull(spans);
        assertThat(spans.size(), is(2));
        assertSpan(spans.get(0), 0);
        assertThat(spans.get(1).getOperationName(), is("/SJDBC/TRUNK/DQL"));
    }

    @Test
    public void assertAsyncExecute() throws Throwable {
        executeInterceptor.beforeMethod(null, null, allArguments, null, null);
        asyncExecuteInterceptor.beforeMethod(null, null, null, null, null);
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        ES.submit(() -> {
            ExecutorDataMap.setDataMap(dataMap);
            sendEvent("ds_1", "select * from t_order_1");
        }).get();
        asyncExecuteInterceptor.afterMethod(null, null, null, null, null);
        sendEvent("ds_0", "select * from t_order_0");
        executeInterceptor.afterMethod(null, null, allArguments, null, null);
        assertThat(segmentStorage.getTraceSegments().size(), is(2));
        TraceSegment segment0 = segmentStorage.getTraceSegments().get(0);
        TraceSegment segment1 = segmentStorage.getTraceSegments().get(1);
        assertThat(segment0.getRefs().size(), is(1));
        List<AbstractTracingSpan> spans0 = SegmentHelper.getSpans(segment0);
        assertNotNull(spans0);
        assertThat(spans0.size(), is(1));
        assertSpan(spans0.get(0), 1);
        List<AbstractTracingSpan> spans1 = SegmentHelper.getSpans(segment1);
        assertNotNull(spans1);
        assertThat(spans1.size(), is(2));
        assertSpan(spans1.get(0), 0);
        assertThat(spans1.get(1).getOperationName(), is("/SJDBC/TRUNK/DQL"));
    }
    
    @Test
    public void assertAsyncContextHold() throws Throwable {
        ExecutorDataMap.getDataMap().put("FOO_KEY", "FOO_VALUE");
        executeInterceptor.beforeMethod(null, null, allArguments, null, null);
        asyncExecuteInterceptor.beforeMethod(null, null, null, null, null);
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        ES.submit(() -> {
            ExecutorDataMap.setDataMap(dataMap);
            sendEvent("ds_1", "select * from t_order_1");
        }).get();
        asyncExecuteInterceptor.afterMethod(null, null, null, null, null);
        executeInterceptor.afterMethod(null, null, allArguments, null, null);
        assertThat(ExecutorDataMap.getDataMap().size(), is(1));
        assertThat(ExecutorDataMap.getDataMap().get("FOO_KEY"), Is.<Object>is("FOO_VALUE"));
    }
    
    @Test
    public void assertExecuteError() throws Throwable {
        executeInterceptor.beforeMethod(null, null, allArguments, null, null);
        asyncExecuteInterceptor.beforeMethod(null, null, null, null, null);
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        ES.submit(() -> {
            ExecutorDataMap.setDataMap(dataMap);
            sendError();
        }).get();
        asyncExecuteInterceptor.handleMethodException(null, null, null, null, new SQLException("test"));
        asyncExecuteInterceptor.afterMethod(null, null, null, null, null);
        sendEvent("ds_0", "select * from t_order_0");
        executeInterceptor.handleMethodException(null, null, allArguments, null, new SQLException("Test"));
        executeInterceptor.afterMethod(null, null, allArguments, null, null);
        assertThat(segmentStorage.getTraceSegments().size(), is(2));
        TraceSegment segment0 = segmentStorage.getTraceSegments().get(0);
        TraceSegment segment1 = segmentStorage.getTraceSegments().get(1);
        List<AbstractTracingSpan> spans0 = SegmentHelper.getSpans(segment0);
        assertNotNull(spans0);
        assertThat(spans0.size(), is(1));
        assertErrorSpan(spans0.get(0));
        List<AbstractTracingSpan> spans1 = SegmentHelper.getSpans(segment1);
        assertNotNull(spans1);
        assertThat(spans1.size(), is(2));
        assertSpan(spans1.get(0), 0);
        assertErrorSpan(spans1.get(1));
    }

    private void assertSpan(AbstractTracingSpan span, int index) {
        assertComponent(span, ComponentsDefine.SHARDING_JDBC);
        assertLayer(span, SpanLayer.DB);
        assertTag(span, 0, "sql");
        assertTag(span, 1, "ds_" + index);
        assertTag(span, 2, "select * from t_order_" + index);
        assertThat(span.isExit(), is(true));
        assertThat(span.getOperationName(), is("/SJDBC/BRANCH/QUERY"));
    }
    
    private void assertErrorSpan(AbstractTracingSpan span) {
        assertOccurException(span, true);
    }
    
    private void sendEvent(String datasource, String sql) {
        DQLExecutionEvent event = new DQLExecutionEvent(datasource, sql, Arrays.asList("1", 100));
        EventBusInstance.getInstance().post(event);
        event.setEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        EventBusInstance.getInstance().post(event);
    }
    
    private void sendError() {
        DMLExecutionEvent event = new DMLExecutionEvent("", "", Collections.emptyList());
        EventBusInstance.getInstance().post(event);
        event.setEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        event.setException(Optional.of(new SQLException("Test")));
        EventBusInstance.getInstance().post(event);
    }
}
