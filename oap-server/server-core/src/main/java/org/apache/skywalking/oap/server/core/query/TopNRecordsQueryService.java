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

package org.apache.skywalking.oap.server.core.query;

import java.io.IOException;
import java.util.List;
import org.apache.skywalking.oap.server.core.query.entity.*;
import org.apache.skywalking.oap.server.core.storage.StorageModule;
import org.apache.skywalking.oap.server.core.storage.query.*;
import org.apache.skywalking.oap.server.library.module.*;
import org.apache.skywalking.oap.server.library.module.Service;

/**
 * @author wusheng
 */
public class TopNRecordsQueryService implements Service {
    private final ModuleManager moduleManager;
    private ITopNRecordsQueryDAO topNRecordsQueryDAO;

    public TopNRecordsQueryService(ModuleManager manager) {
        this.moduleManager = manager;
    }

    private ITopNRecordsQueryDAO getTopNRecordsQueryDAO() {
        if (topNRecordsQueryDAO == null) {
            this.topNRecordsQueryDAO = moduleManager.find(StorageModule.NAME).provider().getService(ITopNRecordsQueryDAO.class);
        }
        return topNRecordsQueryDAO;
    }

    public List<TopNRecord> getTopNRecords(long startSecondTB, long endSecondTB, String metricName, int serviceId,
        int topN, Order order, long startTimestamp,long endTimeStamp) throws IOException {
        return getTopNRecordsQueryDAO().getTopNRecords(startSecondTB, endSecondTB, metricName, serviceId, topN, order,startTimestamp, endTimeStamp);
    }
}
