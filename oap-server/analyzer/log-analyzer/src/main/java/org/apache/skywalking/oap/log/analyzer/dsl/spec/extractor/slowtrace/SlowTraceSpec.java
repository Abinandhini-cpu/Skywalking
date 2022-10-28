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

package org.apache.skywalking.oap.log.analyzer.dsl.spec.extractor.slowtrace;

import org.apache.skywalking.oap.log.analyzer.dsl.spec.AbstractSpec;
import org.apache.skywalking.oap.log.analyzer.provider.LogAnalyzerModuleConfig;
import org.apache.skywalking.oap.server.analyzer.provider.trace.parser.listener.SlowTraceBuilder;
import org.apache.skywalking.oap.server.core.source.DetectPoint;
import org.apache.skywalking.oap.server.library.module.ModuleManager;

import static java.util.Objects.nonNull;

public class SlowTraceSpec extends AbstractSpec {
    public SlowTraceSpec(ModuleManager moduleManager, LogAnalyzerModuleConfig moduleConfig) {
        super(moduleManager, moduleConfig);
    }

    public void latency(final Long latency) {
        if (BINDING.get().shouldAbort()) {
            return;
        }
        if (nonNull(latency)) {
            final SlowTraceBuilder slowTraceBuilder = BINDING.get().slowTraceBuilder();
            slowTraceBuilder.setLatency(latency);
        }
    }

    public void uri(final String uri) {
        if (BINDING.get().shouldAbort()) {
            return;
        }
        if (nonNull(uri)) {
            final SlowTraceBuilder slowTraceBuilder = BINDING.get().slowTraceBuilder();
            slowTraceBuilder.setUri(uri);
        }
    }

    public void processId(final String id) {
        if (BINDING.get().shouldAbort()) {
            return;
        }
        if (nonNull(id)) {
            final SlowTraceBuilder slowTraceBuilder = BINDING.get().slowTraceBuilder();
            slowTraceBuilder.setProcessId(id);
        }
    }

    public void destProcessId(final String id) {
        if (BINDING.get().shouldAbort()) {
            return;
        }
        if (nonNull(id)) {
            final SlowTraceBuilder slowTraceBuilder = BINDING.get().slowTraceBuilder();
            slowTraceBuilder.setDestProcessId(id);
        }
    }

    public void detectPoint(String detectPoint) {
        if (BINDING.get().shouldAbort()) {
            return;
        }
        if (nonNull(detectPoint)) {
            final DetectPoint point = DetectPoint.valueOf(detectPoint.toUpperCase());
            final SlowTraceBuilder slowTraceBuilder = BINDING.get().slowTraceBuilder();
            slowTraceBuilder.setDetectPoint(point);
        }
    }

    public void componentId(final int id) {
        if (BINDING.get().shouldAbort()) {
            return;
        }
        if (id > 0) {
            final SlowTraceBuilder slowTraceBuilder = BINDING.get().slowTraceBuilder();
            slowTraceBuilder.setComponentId(id);
        }
    }

}