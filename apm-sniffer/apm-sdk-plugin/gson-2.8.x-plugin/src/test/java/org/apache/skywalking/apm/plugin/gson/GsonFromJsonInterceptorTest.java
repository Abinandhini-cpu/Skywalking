package org.apache.skywalking.apm.plugin.gson;

import org.apache.skywalking.apm.agent.core.context.trace.AbstractTracingSpan;
import org.apache.skywalking.apm.agent.core.context.trace.TraceSegment;
import org.apache.skywalking.apm.agent.test.helper.SegmentHelper;
import org.apache.skywalking.apm.agent.test.tools.AgentServiceRule;
import org.apache.skywalking.apm.agent.test.tools.SegmentStorage;
import org.apache.skywalking.apm.agent.test.tools.SegmentStoragePoint;
import org.apache.skywalking.apm.agent.test.tools.TracingSegmentRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;



@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(TracingSegmentRunner.class)
public class GsonFromJsonInterceptorTest {

    private GsonFromJsonInterceptor gsonFromJsonInterceptor;

    @SegmentStoragePoint
    private SegmentStorage segmentStorage;

    @Rule
    public AgentServiceRule serviceRule = new AgentServiceRule();

    private Object[] arguments;

    private Class[] argumentType;




    @Before
    public void setUp() {
        gsonFromJsonInterceptor = new GsonFromJsonInterceptor();
        arguments = new Object[] {"100"};
    }

    @Test
    public void testSendMessage() throws Throwable {
        gsonFromJsonInterceptor.beforeMethod(null, null, arguments, null, null);
        gsonFromJsonInterceptor.afterMethod(null, null, arguments, null, null);

        List<TraceSegment> traceSegmentList = segmentStorage.getTraceSegments();
        assertThat(traceSegmentList.size(), is(1));

        TraceSegment segment = traceSegmentList.get(0);
        List<AbstractTracingSpan> spans = SegmentHelper.getSpans(segment);
        assertThat(spans.size(), is(1));
    }

}