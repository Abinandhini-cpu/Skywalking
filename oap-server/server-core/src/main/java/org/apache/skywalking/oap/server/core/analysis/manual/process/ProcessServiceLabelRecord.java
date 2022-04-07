/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.skywalking.oap.server.core.analysis.manual.process;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.analysis.MetricsExtension;
import org.apache.skywalking.oap.server.core.analysis.Stream;
import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.core.analysis.worker.MetricsStreamProcessor;
import org.apache.skywalking.oap.server.core.remote.grpc.proto.RemoteData;
import org.apache.skywalking.oap.server.core.storage.annotation.Column;
import org.apache.skywalking.oap.server.core.storage.type.Convert2Entity;
import org.apache.skywalking.oap.server.core.storage.type.Convert2Storage;
import org.apache.skywalking.oap.server.core.storage.type.StorageBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.apache.skywalking.oap.server.core.source.DefaultScopeDefine.PROCESS_SERVICE_LABEL;

/**
 * Process have multiple labels, such as tag.
 * {@link ProcessServiceLabelRecord} could combine them in the service level.
 * It could help to quickly locate the similar process by the service and label.
 */
@Setter
@Getter
@Stream(name = ProcessServiceLabelRecord.INDEX_NAME, scopeId = PROCESS_SERVICE_LABEL,
        builder = ProcessServiceLabelRecord.Builder.class, processor = MetricsStreamProcessor.class)
@MetricsExtension(supportDownSampling = false, supportUpdate = false)
@EqualsAndHashCode(of = {
        "serviceId",
        "label"
})
public class ProcessServiceLabelRecord extends Metrics {

    public static final String INDEX_NAME = "process_service_label";
    public static final String SERVICE_ID = "service_id";
    public static final String LABEL = "label";

    @Column(columnName = SERVICE_ID)
    private String serviceId;
    @Column(columnName = LABEL)
    private String label;

    @Override
    public boolean combine(Metrics metrics) {
        return true;
    }

    @Override
    public void calculate() {
    }

    @Override
    public Metrics toHour() {
        return null;
    }

    @Override
    public Metrics toDay() {
        return null;
    }

    @Override
    protected String id0() {
        return this.serviceId + Const.ID_CONNECTOR + new String(Base64.getEncoder()
                .encode(label.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    @Override
    public void deserialize(RemoteData remoteData) {
        setServiceId(remoteData.getDataStrings(0));
        setLabel(remoteData.getDataStrings(1));
        setTimeBucket(remoteData.getDataLongs(0));
    }

    @Override
    public RemoteData.Builder serialize() {
        final RemoteData.Builder builder = RemoteData.newBuilder();
        builder.addDataStrings(serviceId);
        builder.addDataStrings(label);
        builder.addDataLongs(getTimeBucket());
        return builder;
    }

    @Override
    public int remoteHashCode() {
        return this.hashCode();
    }

    public static class Builder implements StorageBuilder<ProcessServiceLabelRecord> {

        @Override
        public ProcessServiceLabelRecord storage2Entity(Convert2Entity converter) {
            final ProcessServiceLabelRecord record = new ProcessServiceLabelRecord();
            record.setServiceId((String) converter.get(SERVICE_ID));
            record.setLabel((String) converter.get(LABEL));
            return record;
        }

        @Override
        public void entity2Storage(ProcessServiceLabelRecord entity, Convert2Storage converter) {
            converter.accept(SERVICE_ID, entity.getServiceId());
            converter.accept(LABEL, entity.getLabel());
        }
    }
}