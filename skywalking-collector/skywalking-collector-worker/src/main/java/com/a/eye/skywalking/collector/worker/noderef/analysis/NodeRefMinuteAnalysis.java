package com.a.eye.skywalking.collector.worker.noderef.analysis;

import com.a.eye.skywalking.collector.actor.AbstractLocalAsyncWorkerProvider;
import com.a.eye.skywalking.collector.actor.ClusterWorkerContext;
import com.a.eye.skywalking.collector.actor.LocalWorkerContext;
import com.a.eye.skywalking.collector.actor.ProviderNotFoundException;
import com.a.eye.skywalking.collector.actor.selector.RollingSelector;
import com.a.eye.skywalking.collector.actor.selector.WorkerSelector;
import com.a.eye.skywalking.collector.worker.WorkerConfig;
import com.a.eye.skywalking.collector.worker.noderef.persistence.NodeRefMinuteAgg;
import com.a.eye.skywalking.collector.worker.segment.SegmentPost;
import com.a.eye.skywalking.collector.worker.storage.RecordData;
import com.a.eye.skywalking.trace.TraceSegment;

/**
 * @author pengys5
 */
public class NodeRefMinuteAnalysis extends AbstractNodeRefAnalysis {

    protected NodeRefMinuteAnalysis(com.a.eye.skywalking.collector.actor.Role role, ClusterWorkerContext clusterContext, LocalWorkerContext selfContext) {
        super(role, clusterContext, selfContext);
    }

    @Override
    public void preStart() throws ProviderNotFoundException {
        super.preStart();
        getClusterContext().findProvider(NodeRefResSumMinuteAnalysis.Role.INSTANCE).create(this);
    }

    @Override
    public void analyse(Object message) throws Exception {
        if (message instanceof SegmentPost.SegmentWithTimeSlice) {
            SegmentPost.SegmentWithTimeSlice segmentWithTimeSlice = (SegmentPost.SegmentWithTimeSlice) message;
            TraceSegment segment = segmentWithTimeSlice.getTraceSegment();
            long minute = segmentWithTimeSlice.getMinute();
            long hour = segmentWithTimeSlice.getHour();
            long day = segmentWithTimeSlice.getDay();
            int second = segmentWithTimeSlice.getSecond();
            analyseNodeRef(segment, segmentWithTimeSlice.getMinute(), minute, hour, day, second);
        }
    }

    @Override
    protected void sendToResSumAnalysis(AbstractNodeRefResSumAnalysis.NodeRefResRecord refResRecord) throws Exception {
        getSelfContext().lookup(NodeRefResSumMinuteAnalysis.Role.INSTANCE).tell(refResRecord);
    }

    @Override
    protected void aggregation() throws Exception {
        RecordData oneRecord;
        while ((oneRecord = pushOne()) != null) {
            getClusterContext().lookup(NodeRefMinuteAgg.Role.INSTANCE).tell(oneRecord);
        }
    }


    public static class Factory extends AbstractLocalAsyncWorkerProvider<NodeRefMinuteAnalysis> {

        public static Factory INSTANCE = new Factory();

        @Override
        public Role role() {
            return Role.INSTANCE;
        }

        @Override
        public NodeRefMinuteAnalysis workerInstance(ClusterWorkerContext clusterContext) {
            return new NodeRefMinuteAnalysis(role(), clusterContext, new LocalWorkerContext());
        }

        @Override
        public int queueSize() {
            return WorkerConfig.Queue.Node.NodeRefMinuteAnalysis.Size;
        }
    }

    public enum Role implements com.a.eye.skywalking.collector.actor.Role {
        INSTANCE;

        @Override
        public String roleName() {
            return NodeRefMinuteAnalysis.class.getSimpleName();
        }

        @Override
        public WorkerSelector workerSelector() {
            return new RollingSelector();
        }
    }
}
