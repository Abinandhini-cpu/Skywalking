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

package org.apache.skywalking.oap.server.core.profile.analyze;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import org.apache.skywalking.oap.server.core.analysis.manual.segment.SegmentRecord;
import org.apache.skywalking.oap.server.core.profile.ProfileThreadSnapshotRecord;
import org.apache.skywalking.oap.server.core.query.entity.BasicTrace;
import org.apache.skywalking.oap.server.core.query.entity.ProfileAnalyzeTimeRange;
import org.apache.skywalking.oap.server.core.query.entity.ProfileStackTree;
import org.apache.skywalking.oap.server.core.storage.profile.IProfileThreadSnapshotQueryDAO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Data
public class ProfileStackAnalyze {

    private ProfileStackData data;
    private List<ProfileStackElementMatcher> expected;

    public void analyzeAndAssert(int maxAnalyzeCount) throws IOException {
        List<ProfileThreadSnapshotRecord> stacks = data.transform();

        final ProfileAnalyzeTimeRange range = new ProfileAnalyzeTimeRange();
        range.setStart(0);
        range.setEnd(0);
        List<ProfileStackTree> trees = buildAnalyzer(stacks, maxAnalyzeCount).analyze(null, Collections.singletonList(range)).getTrees();

        assertNotNull(trees);
        assertEquals(trees.size(), expected.size());
        for (int i = 0; i < trees.size(); i++) {
            expected.get(i).verify(trees.get(i));
        }
    }

    private ProfileAnalyzer buildAnalyzer(List<ProfileThreadSnapshotRecord> stacks, int maxAnalyzeCount) throws IOException {
        ProfileAnalyzer analyzer = new ProfileAnalyzer(null, 2, maxAnalyzeCount);
        analyzer.profileThreadSnapshotQueryDAO = new ThreadSnapshotDAO(stacks);
        return analyzer;
    }

    static class ThreadSnapshotDAO implements IProfileThreadSnapshotQueryDAO {

        private final List<ProfileThreadSnapshotRecord> stacks;

        public ThreadSnapshotDAO(List<ProfileThreadSnapshotRecord> stacks) {
            this.stacks = stacks;
        }

        @Override
        public List<BasicTrace> queryProfiledSegments(String taskId) throws IOException {
            return null;
        }

        @Override
        public int queryMinSequence(String segmentId, long start, long end) throws IOException {
            return 0;
        }

        @Override
        public int queryMaxSequence(String segmentId, long start, long end) throws IOException {
            return stacks.size();
        }

        @Override
        public List<ProfileThreadSnapshotRecord> queryRecords(String segmentId, int minSequence, int maxSequence) throws IOException {
            return stacks.stream()
                    .filter(s -> s.getSequence() >= minSequence)
                    .filter(s -> s.getSequence() < maxSequence)
                    .collect(Collectors.toList());
        }

        @Override
        public SegmentRecord getProfiledSegment(String segmentId) throws IOException {
            return null;
        }

    }

}
