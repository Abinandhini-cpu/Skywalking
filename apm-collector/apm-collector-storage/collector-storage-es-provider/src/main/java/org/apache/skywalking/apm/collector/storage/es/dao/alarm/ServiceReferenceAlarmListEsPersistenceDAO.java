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
import org.apache.skywalking.apm.collector.core.util.TimeBucketUtils;
import org.apache.skywalking.apm.collector.storage.dao.alarm.IServiceReferenceAlarmListPersistenceDAO;
import org.apache.skywalking.apm.collector.storage.es.base.dao.EsDAO;
import org.apache.skywalking.apm.collector.storage.table.alarm.ServiceReferenceAlarmList;
import org.apache.skywalking.apm.collector.storage.table.alarm.ServiceReferenceAlarmListTable;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author peng-yongsheng
 */
public class ServiceReferenceAlarmListEsPersistenceDAO extends EsDAO implements IServiceReferenceAlarmListPersistenceDAO<IndexRequestBuilder, UpdateRequestBuilder, ServiceReferenceAlarmList> {

    private final Logger logger = LoggerFactory.getLogger(ServiceReferenceAlarmListEsPersistenceDAO.class);

    public ServiceReferenceAlarmListEsPersistenceDAO(ElasticSearchClient client) {
        super(client);
    }

    @Override public ServiceReferenceAlarmList get(String id) {
        GetResponse getResponse = getClient().prepareGet(ServiceReferenceAlarmListTable.TABLE, id).get();
        if (getResponse.isExists()) {
            ServiceReferenceAlarmList serviceReferenceAlarmList = new ServiceReferenceAlarmList();
            serviceReferenceAlarmList.setId(id);
            Map<String, Object> source = getResponse.getSource();
            serviceReferenceAlarmList.setFrontApplicationId(((Number)source.get(ServiceReferenceAlarmListTable.COLUMN_FRONT_APPLICATION_ID)).intValue());
            serviceReferenceAlarmList.setBehindApplicationId(((Number)source.get(ServiceReferenceAlarmListTable.COLUMN_BEHIND_APPLICATION_ID)).intValue());
            serviceReferenceAlarmList.setFrontInstanceId(((Number)source.get(ServiceReferenceAlarmListTable.COLUMN_FRONT_INSTANCE_ID)).intValue());
            serviceReferenceAlarmList.setBehindInstanceId(((Number)source.get(ServiceReferenceAlarmListTable.COLUMN_BEHIND_INSTANCE_ID)).intValue());
            serviceReferenceAlarmList.setFrontServiceId(((Number)source.get(ServiceReferenceAlarmListTable.COLUMN_FRONT_SERVICE_ID)).intValue());
            serviceReferenceAlarmList.setBehindServiceId(((Number)source.get(ServiceReferenceAlarmListTable.COLUMN_BEHIND_SERVICE_ID)).intValue());
            serviceReferenceAlarmList.setSourceValue(((Number)source.get(ServiceReferenceAlarmListTable.COLUMN_SOURCE_VALUE)).intValue());

            serviceReferenceAlarmList.setAlarmType(((Number)source.get(ServiceReferenceAlarmListTable.COLUMN_ALARM_TYPE)).intValue());
            serviceReferenceAlarmList.setAlarmContent((String)source.get(ServiceReferenceAlarmListTable.COLUMN_ALARM_CONTENT));

            serviceReferenceAlarmList.setTimeBucket(((Number)source.get(ServiceReferenceAlarmListTable.COLUMN_TIME_BUCKET)).longValue());
            return serviceReferenceAlarmList;
        } else {
            return null;
        }
    }

    @Override public IndexRequestBuilder prepareBatchInsert(ServiceReferenceAlarmList data) {
        Map<String, Object> source = new HashMap<>();
        source.put(ServiceReferenceAlarmListTable.COLUMN_FRONT_APPLICATION_ID, data.getFrontApplicationId());
        source.put(ServiceReferenceAlarmListTable.COLUMN_BEHIND_APPLICATION_ID, data.getBehindApplicationId());
        source.put(ServiceReferenceAlarmListTable.COLUMN_FRONT_INSTANCE_ID, data.getFrontInstanceId());
        source.put(ServiceReferenceAlarmListTable.COLUMN_BEHIND_INSTANCE_ID, data.getBehindInstanceId());
        source.put(ServiceReferenceAlarmListTable.COLUMN_FRONT_SERVICE_ID, data.getFrontServiceId());
        source.put(ServiceReferenceAlarmListTable.COLUMN_BEHIND_SERVICE_ID, data.getBehindServiceId());
        source.put(ServiceReferenceAlarmListTable.COLUMN_SOURCE_VALUE, data.getSourceValue());

        source.put(ServiceReferenceAlarmListTable.COLUMN_ALARM_TYPE, data.getAlarmType());
        source.put(ServiceReferenceAlarmListTable.COLUMN_ALARM_CONTENT, data.getAlarmContent());

        source.put(ServiceReferenceAlarmListTable.COLUMN_TIME_BUCKET, data.getTimeBucket());

        return getClient().prepareIndex(ServiceReferenceAlarmListTable.TABLE, data.getId()).setSource(source);
    }

    @Override public UpdateRequestBuilder prepareBatchUpdate(ServiceReferenceAlarmList data) {
        Map<String, Object> source = new HashMap<>();
        source.put(ServiceReferenceAlarmListTable.COLUMN_FRONT_APPLICATION_ID, data.getFrontApplicationId());
        source.put(ServiceReferenceAlarmListTable.COLUMN_BEHIND_APPLICATION_ID, data.getBehindApplicationId());
        source.put(ServiceReferenceAlarmListTable.COLUMN_FRONT_INSTANCE_ID, data.getFrontInstanceId());
        source.put(ServiceReferenceAlarmListTable.COLUMN_BEHIND_INSTANCE_ID, data.getBehindInstanceId());
        source.put(ServiceReferenceAlarmListTable.COLUMN_FRONT_SERVICE_ID, data.getFrontServiceId());
        source.put(ServiceReferenceAlarmListTable.COLUMN_BEHIND_SERVICE_ID, data.getBehindServiceId());
        source.put(ServiceReferenceAlarmListTable.COLUMN_SOURCE_VALUE, data.getSourceValue());

        source.put(ServiceReferenceAlarmListTable.COLUMN_ALARM_TYPE, data.getAlarmType());
        source.put(ServiceReferenceAlarmListTable.COLUMN_ALARM_CONTENT, data.getAlarmContent());

        source.put(ServiceReferenceAlarmListTable.COLUMN_TIME_BUCKET, data.getTimeBucket());

        return getClient().prepareUpdate(ServiceReferenceAlarmListTable.TABLE, data.getId()).setDoc(source);
    }

    @Override public void deleteHistory(Long startTimestamp, Long endTimestamp) {
        long startTimeBucket = TimeBucketUtils.INSTANCE.getMinuteTimeBucket(startTimestamp);
        long endTimeBucket = TimeBucketUtils.INSTANCE.getMinuteTimeBucket(endTimestamp);
        BulkByScrollResponse response = getClient().prepareDelete()
            .filter(QueryBuilders.rangeQuery(ServiceReferenceAlarmListTable.COLUMN_TIME_BUCKET).gte(startTimeBucket).lte(endTimeBucket))
            .source(ServiceReferenceAlarmListTable.TABLE)
            .get();

        long deleted = response.getDeleted();
        logger.info("Delete {} rows history from {} index.", deleted, ServiceReferenceAlarmListTable.TABLE);
    }
}
