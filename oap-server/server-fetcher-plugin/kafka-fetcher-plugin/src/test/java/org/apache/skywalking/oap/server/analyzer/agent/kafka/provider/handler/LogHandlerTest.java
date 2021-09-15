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

package org.apache.skywalking.oap.server.analyzer.agent.kafka.provider.handler;

import org.apache.skywalking.oap.log.analyzer.module.LogAnalyzerModule;
import org.apache.skywalking.oap.log.analyzer.provider.log.ILogAnalyzerService;
import org.apache.skywalking.oap.server.analyzer.agent.kafka.mock.MockModuleManager;
import org.apache.skywalking.oap.server.analyzer.agent.kafka.mock.MockModuleProvider;
import org.apache.skywalking.oap.server.analyzer.agent.kafka.module.KafkaFetcherConfig;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.telemetry.TelemetryModule;
import org.apache.skywalking.oap.server.telemetry.api.CounterMetrics;
import org.apache.skywalking.oap.server.telemetry.api.GaugeMetrics;
import org.apache.skywalking.oap.server.telemetry.api.HistogramMetrics;
import org.apache.skywalking.oap.server.telemetry.api.MetricsCreator;
import org.apache.skywalking.oap.server.telemetry.api.MetricsTag;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LogHandlerTest {
    private static final String TOPIC_NAME = "skywalking-logs";
    private LogHandler handler = null;
    private KafkaFetcherConfig config = new KafkaFetcherConfig();

    private ModuleManager manager;

    @Before
    public void setup() {
        manager = new MockModuleManager() {
            @Override
            protected void init() {
                register(LogAnalyzerModule.NAME, () -> new MockModuleProvider() {
                    @Override
                    protected void register() {
                        registerServiceImplementation(ILogAnalyzerService.class, (ILogAnalyzerService) (log, extraLog) -> {

                        });
                    }
                });
                register(TelemetryModule.NAME, () -> new MockModuleProvider() {
                    @Override
                    protected void register() {
                        registerServiceImplementation(MetricsCreator.class, new MetricsCreator() {
                            @Override
                            public CounterMetrics createCounter(String name, String tips, MetricsTag.Keys tagKeys, MetricsTag.Values tagValues) {
                                return null;
                            }

                            @Override
                            public GaugeMetrics createGauge(String name, String tips, MetricsTag.Keys tagKeys, MetricsTag.Values tagValues) {
                                return null;
                            }

                            @Override
                            public HistogramMetrics createHistogramMetric(String name, String tips, MetricsTag.Keys tagKeys, MetricsTag.Values tagValues, double... buckets) {
                                return null;
                            }
                        });
                    }
                });
            }
        };
        handler = new LogHandler(manager, config);
    }

    @Test
    public void testGetTopic() {
        Assert.assertEquals(handler.getTopic(), TOPIC_NAME);

        String namespace = "product";
        config.setNamespace(namespace);
        assertEquals(namespace + "-" + TOPIC_NAME, handler.getTopic());
    }
}