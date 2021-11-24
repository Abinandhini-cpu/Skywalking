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

package org.apache.skywalking.oap.server.storage.plugin.iotdb.query;

import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.network.logging.v3.LogTags;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.manual.log.AbstractLogRecord;
import org.apache.skywalking.oap.server.core.analysis.manual.log.LogRecord;
import org.apache.skywalking.oap.server.core.analysis.manual.searchtag.Tag;
import org.apache.skywalking.oap.server.core.query.enumeration.Order;
import org.apache.skywalking.oap.server.core.query.input.TraceScopeCondition;
import org.apache.skywalking.oap.server.core.query.type.ContentType;
import org.apache.skywalking.oap.server.core.query.type.KeyValue;
import org.apache.skywalking.oap.server.core.query.type.Log;
import org.apache.skywalking.oap.server.core.query.type.Logs;
import org.apache.skywalking.oap.server.core.storage.StorageData;
import org.apache.skywalking.oap.server.core.storage.StorageHashMapBuilder;
import org.apache.skywalking.oap.server.core.storage.query.ILogQueryDAO;
import org.apache.skywalking.oap.server.library.util.CollectionUtils;
import org.apache.skywalking.oap.server.library.util.StringUtil;
import org.apache.skywalking.oap.server.storage.plugin.iotdb.IoTDBClient;

@Slf4j
@RequiredArgsConstructor
public class IoTDBLogQueryDAO implements ILogQueryDAO {
    private final IoTDBClient client;
    private final StorageHashMapBuilder<LogRecord> storageBuilder = new LogRecord.Builder();

    @Override
    public Logs queryLogs(String serviceId, String serviceInstanceId, String endpointId,
                          TraceScopeCondition relatedTrace, Order queryOrder, int from, int limit, long startTB,
                          long endTB, List<Tag> tags, List<String> keywordsOfContent,
                          List<String> excludingKeywordsOfContent) throws IOException {
        StringBuilder query = new StringBuilder();
        // This method maybe have poor efficiency. It queries all data which meets a condition without select function.
        // https://github.com/apache/iotdb/discussions/3888
        query.append("select * from ");
        query = client.addModelPath(query, LogRecord.INDEX_NAME);
        Map<String, String> indexAndValueMap = new HashMap<>();
        if (StringUtil.isNotEmpty(serviceId)) {
            indexAndValueMap.put(IoTDBClient.SERVICE_ID_IDX, serviceId);
        }
        if (Objects.nonNull(relatedTrace) && StringUtil.isNotEmpty(relatedTrace.getTraceId())) {
            indexAndValueMap.put(IoTDBClient.TRACE_ID_IDX, relatedTrace.getTraceId());
        }
        query = client.addQueryIndexValue(LogRecord.INDEX_NAME, query, indexAndValueMap);
        query.append(" where 1=1");
        if (startTB != 0 && endTB != 0) {
            query.append(" and ").append(IoTDBClient.TIME).append(" >= ").append(TimeBucket.getTimestamp(startTB));
            query.append(" and ").append(IoTDBClient.TIME).append(" <= ").append(TimeBucket.getTimestamp(endTB));
        }
        if (StringUtil.isNotEmpty(serviceInstanceId)) {
            query.append(" and ").append(AbstractLogRecord.SERVICE_INSTANCE_ID).append(" = \"").append(serviceInstanceId).append("\"");
        }
        if (StringUtil.isNotEmpty(endpointId)) {
            query.append(" and ").append(AbstractLogRecord.ENDPOINT_ID).append(" = \"").append(endpointId).append("\"");
        }
        if (Objects.nonNull(relatedTrace)) {
            if (StringUtil.isNotEmpty(relatedTrace.getSegmentId())) {
                query.append(" and ").append(AbstractLogRecord.TRACE_SEGMENT_ID).append(" = \"").append(relatedTrace.getSegmentId()).append("\"");
            }
            if (Objects.nonNull(relatedTrace.getSpanId())) {
                query.append(" and ").append(AbstractLogRecord.SPAN_ID).append(" = ").append(relatedTrace.getSpanId());
            }
        }
        if (CollectionUtils.isNotEmpty(tags)) {
            for (final Tag tag : tags) {
                query.append(" and ").append(tag.getKey()).append(" = \"").append(tag.getValue()).append("\"");
            }
        }
        // IoTDB doesn't support the query contains "1=1" and "*" at the meantime.
        String queryString = query.toString().replace("1=1 and ", "");
        queryString = queryString + IoTDBClient.ALIGN_BY_DEVICE;

        Logs logs = new Logs();
        List<? super StorageData> storageDataList = client.filterQuery(LogRecord.INDEX_NAME, queryString, storageBuilder);
        int limitCount = 0;
        for (int i = from; i < storageDataList.size(); i++) {
            if (limitCount < limit) {
                limitCount++;
                LogRecord logRecord = (LogRecord) storageDataList.get(i);
                Log log = new Log();
                log.setServiceId(logRecord.getServiceId());
                log.setServiceInstanceId(logRecord.getServiceInstanceId());
                log.setEndpointId(logRecord.getEndpointId());
                log.setTraceId(logRecord.getTraceId());
                log.setTimestamp(logRecord.getTimestamp());
                log.setContentType(ContentType.instanceOf(logRecord.getContentType()));
                log.setContent(logRecord.getContent());
                if (CollectionUtils.isNotEmpty(logRecord.getTagsRawData())) {
                    iotdbParserDataBinary(logRecord.getTagsRawData(), log.getTags());
                }
                logs.getLogs().add(log);
            }
        }
        logs.setTotal(storageDataList.size());
        // resort by self, because of the select query result order by time.
        if (Order.DES.equals(queryOrder)) {
            logs.getLogs().sort((Log l1, Log l2) -> Long.compare(l2.getTimestamp(), l1.getTimestamp()));
        } else {
            logs.getLogs().sort(Comparator.comparingLong(Log::getTimestamp));
        }
        return logs;
    }

    private void iotdbParserDataBinary(byte[] tagsRawData, List<KeyValue> tags) {
        try {
            LogTags logTags = LogTags.parseFrom(tagsRawData);
            logTags.getDataList().forEach(pair -> tags.add(new KeyValue(pair.getKey(), pair.getValue())));
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
