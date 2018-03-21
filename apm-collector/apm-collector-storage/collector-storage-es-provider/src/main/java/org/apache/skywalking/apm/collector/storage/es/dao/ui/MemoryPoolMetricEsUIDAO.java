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

package org.apache.skywalking.apm.collector.storage.es.dao.ui;

import com.google.gson.JsonObject;
import org.apache.skywalking.apm.collector.client.elasticsearch.ElasticSearchClient;
import org.apache.skywalking.apm.collector.core.UnexpectedException;
import org.apache.skywalking.apm.collector.core.util.Const;
import org.apache.skywalking.apm.collector.storage.dao.ui.IMemoryPoolMetricUIDAO;
import org.apache.skywalking.apm.collector.storage.es.base.dao.EsDAO;
import org.apache.skywalking.apm.collector.storage.table.jvm.MemoryPoolMetricTable;
import org.elasticsearch.action.get.GetResponse;

/**
 * @author peng-yongsheng
 */
public class MemoryPoolMetricEsUIDAO extends EsDAO implements IMemoryPoolMetricUIDAO {

    public MemoryPoolMetricEsUIDAO(ElasticSearchClient client) {
        super(client);
    }

    @Override
    public JsonObject getMetric(int instanceId, long timeBucket, int poolType) {
        String id = timeBucket + Const.ID_SPLIT + instanceId + Const.ID_SPLIT + poolType;
        GetResponse getResponse = getClient().prepareGet(MemoryPoolMetricTable.TABLE, id).get();

        JsonObject metric = new JsonObject();
        if (getResponse.isExists()) {
            metric.addProperty("max", ((Number) getResponse.getSource().get(MemoryPoolMetricTable.COLUMN_MAX)).intValue());
            metric.addProperty("init", ((Number) getResponse.getSource().get(MemoryPoolMetricTable.COLUMN_INIT)).intValue());
            metric.addProperty("used", ((Number) getResponse.getSource().get(MemoryPoolMetricTable.COLUMN_USED)).intValue());
        } else {
            metric.addProperty("max", 0);
            metric.addProperty("init", 0);
            metric.addProperty("used", 0);
        }
        return metric;
    }

    @Override
    public JsonObject getMetric(int instanceId, long startTimeBucket, long endTimeBucket, int poolType) {
        throw new UnexpectedException("Not implement methodø");
    }
}
