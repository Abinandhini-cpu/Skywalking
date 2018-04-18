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

package org.apache.skywalking.apm.collector.storage.es.dao.alarm;

import java.util.HashMap;
import java.util.Map;
import org.apache.skywalking.apm.collector.client.elasticsearch.ElasticSearchClient;
import org.apache.skywalking.apm.collector.core.annotations.trace.GraphComputingMetric;
import org.apache.skywalking.apm.collector.storage.dao.alarm.IServiceReferenceAlarmPersistenceDAO;
import org.apache.skywalking.apm.collector.storage.es.base.dao.AbstractPersistenceEsDAO;
import org.apache.skywalking.apm.collector.storage.table.alarm.ServiceReferenceAlarm;
import org.apache.skywalking.apm.collector.storage.table.alarm.ServiceReferenceAlarmTable;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;

/**
 * @author peng-yongsheng
 */
public class ServiceReferenceAlarmEsPersistenceDAO extends AbstractPersistenceEsDAO<ServiceReferenceAlarm> implements IServiceReferenceAlarmPersistenceDAO<IndexRequestBuilder, UpdateRequestBuilder, ServiceReferenceAlarm> {

    public ServiceReferenceAlarmEsPersistenceDAO(ElasticSearchClient client) {
        super(client);
    }

    @Override protected String tableName() {
        return ServiceReferenceAlarmTable.TABLE;
    }

    @Override protected ServiceReferenceAlarm esDataToStreamData(Map<String, Object> source) {
        ServiceReferenceAlarm serviceReferenceAlarm = new ServiceReferenceAlarm();
        serviceReferenceAlarm.setFrontApplicationId(((Number)source.get(ServiceReferenceAlarmTable.FRONT_APPLICATION_ID.getName())).intValue());
        serviceReferenceAlarm.setBehindApplicationId(((Number)source.get(ServiceReferenceAlarmTable.BEHIND_APPLICATION_ID.getName())).intValue());
        serviceReferenceAlarm.setFrontInstanceId(((Number)source.get(ServiceReferenceAlarmTable.FRONT_INSTANCE_ID.getName())).intValue());
        serviceReferenceAlarm.setBehindInstanceId(((Number)source.get(ServiceReferenceAlarmTable.BEHIND_INSTANCE_ID.getName())).intValue());
        serviceReferenceAlarm.setFrontServiceId(((Number)source.get(ServiceReferenceAlarmTable.FRONT_SERVICE_ID.getName())).intValue());
        serviceReferenceAlarm.setBehindServiceId(((Number)source.get(ServiceReferenceAlarmTable.BEHIND_SERVICE_ID.getName())).intValue());
        serviceReferenceAlarm.setSourceValue(((Number)source.get(ServiceReferenceAlarmTable.SOURCE_VALUE.getName())).intValue());

        serviceReferenceAlarm.setAlarmType(((Number)source.get(ServiceReferenceAlarmTable.ALARM_TYPE.getName())).intValue());
        serviceReferenceAlarm.setAlarmContent((String)source.get(ServiceReferenceAlarmTable.ALARM_CONTENT.getName()));

        serviceReferenceAlarm.setLastTimeBucket(((Number)source.get(ServiceReferenceAlarmTable.LAST_TIME_BUCKET.getName())).longValue());
        return serviceReferenceAlarm;
    }

    @Override protected Map<String, Object> esStreamDataToEsData(ServiceReferenceAlarm streamData) {
        Map<String, Object> target = new HashMap<>();
        target.put(ServiceReferenceAlarmTable.FRONT_APPLICATION_ID.getName(), streamData.getFrontApplicationId());
        target.put(ServiceReferenceAlarmTable.BEHIND_APPLICATION_ID.getName(), streamData.getBehindApplicationId());
        target.put(ServiceReferenceAlarmTable.FRONT_INSTANCE_ID.getName(), streamData.getFrontInstanceId());
        target.put(ServiceReferenceAlarmTable.BEHIND_INSTANCE_ID.getName(), streamData.getBehindInstanceId());
        target.put(ServiceReferenceAlarmTable.FRONT_SERVICE_ID.getName(), streamData.getFrontServiceId());
        target.put(ServiceReferenceAlarmTable.BEHIND_SERVICE_ID.getName(), streamData.getBehindServiceId());
        target.put(ServiceReferenceAlarmTable.SOURCE_VALUE.getName(), streamData.getSourceValue());

        target.put(ServiceReferenceAlarmTable.ALARM_TYPE.getName(), streamData.getAlarmType());
        target.put(ServiceReferenceAlarmTable.ALARM_CONTENT.getName(), streamData.getAlarmContent());

        target.put(ServiceReferenceAlarmTable.LAST_TIME_BUCKET.getName(), streamData.getLastTimeBucket());
        return target;
    }

    @Override protected String timeBucketColumnNameForDelete() {
        return ServiceReferenceAlarmTable.LAST_TIME_BUCKET.getName();
    }

    @GraphComputingMetric(name = "/persistence/get/" + ServiceReferenceAlarmTable.TABLE)
    @Override public ServiceReferenceAlarm get(String id) {
        return super.get(id);
    }
}
