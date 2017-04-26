package com.a.eye.skywalking.collector.worker.globaltrace.analysis;

import com.a.eye.skywalking.collector.actor.AbstractLocalAsyncWorkerProvider;
import com.a.eye.skywalking.collector.actor.ClusterWorkerContext;
import com.a.eye.skywalking.collector.actor.LocalWorkerContext;
import com.a.eye.skywalking.collector.actor.selector.RollingSelector;
import com.a.eye.skywalking.collector.actor.selector.WorkerSelector;
import com.a.eye.skywalking.collector.worker.MergeAnalysisMember;
import com.a.eye.skywalking.collector.worker.config.WorkerConfig;
import com.a.eye.skywalking.collector.worker.globaltrace.GlobalTraceIndex;
import com.a.eye.skywalking.collector.worker.globaltrace.persistence.GlobalTraceAgg;
import com.a.eye.skywalking.collector.worker.segment.SegmentPost;
import com.a.eye.skywalking.collector.worker.segment.entity.GlobalTraceId;
import com.a.eye.skywalking.collector.worker.segment.entity.Segment;
import com.a.eye.skywalking.collector.worker.storage.MergeData;
import com.a.eye.skywalking.collector.worker.tools.CollectionTools;

import java.util.List;

/**
 * @author pengys5
 */
public class GlobalTraceAnalysis extends MergeAnalysisMember {

    GlobalTraceAnalysis(Role role, ClusterWorkerContext clusterContext, LocalWorkerContext selfContext) {
        super(role, clusterContext, selfContext);
    }

    @Override
    public void analyse(Object message) throws Exception {
        if (message instanceof SegmentPost.SegmentWithTimeSlice) {
            SegmentPost.SegmentWithTimeSlice segmentWithTimeSlice = (SegmentPost.SegmentWithTimeSlice) message;
            Segment segment = segmentWithTimeSlice.getSegment();
            String subSegmentId = segment.getTraceSegmentId();
            List<GlobalTraceId> globalTraceIdList = segment.getRelatedGlobalTraces();
            if (CollectionTools.isNotEmpty(globalTraceIdList)) {
                for (GlobalTraceId disTraceId : globalTraceIdList) {
                    String traceId = disTraceId.get();
                    setMergeData(traceId, GlobalTraceIndex.SUB_SEG_IDS, subSegmentId);
                }
            }
        }
    }

    @Override
    protected void aggregation() throws Exception {
        MergeData oneRecord;
        while ((oneRecord = pushOne()) != null) {
            getClusterContext().lookup(GlobalTraceAgg.Role.INSTANCE).tell(oneRecord);
        }
    }

    public static class Factory extends AbstractLocalAsyncWorkerProvider<GlobalTraceAnalysis> {
        public static Factory INSTANCE = new Factory();

        @Override
        public Role role() {
            return Role.INSTANCE;
        }

        @Override
        public GlobalTraceAnalysis workerInstance(ClusterWorkerContext clusterContext) {
            return new GlobalTraceAnalysis(role(), clusterContext, new LocalWorkerContext());
        }

        @Override
        public int queueSize() {
            return WorkerConfig.Queue.GlobalTrace.GlobalTraceAnalysis.SIZE;
        }
    }

    public enum Role implements com.a.eye.skywalking.collector.actor.Role {
        INSTANCE;

        @Override
        public String roleName() {
            return GlobalTraceAnalysis.class.getSimpleName();
        }

        @Override
        public WorkerSelector workerSelector() {
            return new RollingSelector();
        }
    }
}
