package com.a.eye.skywalking.trace;

import com.a.eye.skywalking.trace.tag.Tags;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by wusheng on 2017/2/18.
 */
public class SpanTestCase {
    @Test
    public void testConstructors() {
        Span span1 = new Span(0, "serviceA");
        Span span2 = new Span(2, span1, "serviceA");

        Assert.assertEquals(-1, span1.getParentSpanId());
        Assert.assertEquals(0, span2.getParentSpanId());
        Assert.assertTrue(span1.getStartTime() > 0);
        Assert.assertTrue(span2.getStartTime() > 0);
    }

    @Test
    public void testFinish() {
        TraceSegment owner = new TraceSegment("trace_1");

        Span span1 = new Span(0, "serviceA");

        Assert.assertTrue(span1.getEndTime() == 0);

        span1.finish(owner);
        Assert.assertEquals(span1, owner.getSpans().get(0));
        Assert.assertTrue(span1.getEndTime() > 0);
    }

    @Test
    public void testSetTag() {
        Span span1 = new Span(0, "serviceA");
        Tags.SPAN_LAYER.asHttp(span1);
        Tags.COMPONENT.set(span1, "Spring");
        Tags.PEER_HOST.set(span1, ipToInt("127.0.0.1"));
        Tags.ERROR.set(span1, true);
        Tags.STATUS_CODE.set(span1, 302);
        Tags.URL.set(span1, "http://127.0.0.1/serviceA");
        Tags.DB_URL.set(span1, "jdbc:127.0.0.1:user");
        Tags.DB_STATEMENT.set(span1, "select * from users");

        Map<String, Object> tags = span1.getTags();
        Assert.assertEquals(8, tags.size());
        Assert.assertTrue(Tags.SPAN_LAYER.isHttp(span1));
        Assert.assertEquals("127.0.0.1", intToIp(Tags.PEER_HOST.get(span1)));
        Assert.assertTrue(Tags.ERROR.get(span1));
    }

    private int ipToInt(String ipAddress) {
        int result = 0;
        String[] ipAddressInArray = ipAddress.split("\\.");

        for (int i = 3; i >= 0; i--) {

            int ip = Integer.parseInt(ipAddressInArray[3 - i]);

            //left shifting 24,16,8,0 and bitwise OR
            //1. 192 << 24
            //1. 168 << 16
            //1. 1   << 8
            //1. 2   << 0
            result |= ip << (i * 8);
        }
        return result;
    }

    private static String intToIp(int longIp) {
        StringBuffer sb = new StringBuffer("");
        sb.append(String.valueOf((longIp >>> 24)));
        sb.append(".");
        sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
        sb.append(".");
        sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
        sb.append(".");
        sb.append(String.valueOf((longIp & 0x000000FF)));
        return sb.toString();
    }

    @Test
    public void testLogException(){
        Span span1 = new Span(0, "serviceA");
        Exception exp = new Exception("exception msg");
        span1.log(exp);
        List<LogData> logs = span1.getLogs();

        Assert.assertEquals("java.lang.Exception", logs.get(0).getFields().get("error.kind"));
        Assert.assertEquals("exception msg", logs.get(0).getFields().get("message"));
        Assert.assertNotNull(logs.get(0).getFields().get("stack"));
    }
}
