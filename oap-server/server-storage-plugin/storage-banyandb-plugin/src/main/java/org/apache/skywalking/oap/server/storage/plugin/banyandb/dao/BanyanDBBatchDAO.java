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

package org.apache.skywalking.oap.server.storage.plugin.banyandb.dao;

import org.apache.skywalking.banyandb.v1.client.TraceBulkWriteProcessor;
import org.apache.skywalking.oap.server.core.storage.AbstractDAO;
import org.apache.skywalking.oap.server.core.storage.IBatchDAO;
import org.apache.skywalking.oap.server.library.client.request.InsertRequest;
import org.apache.skywalking.oap.server.library.client.request.PrepareRequest;
import org.apache.skywalking.oap.server.library.util.CollectionUtils;
import org.apache.skywalking.oap.server.storage.plugin.banyandb.BanyanDBStorageClient;

import java.util.List;

public class BanyanDBBatchDAO extends AbstractDAO<BanyanDBStorageClient> implements IBatchDAO {
    private TraceBulkWriteProcessor bulkProcessor;

    private final int maxBulkSize;
    private final int flushInterval;
    private final int concurrency;

    public BanyanDBBatchDAO(BanyanDBStorageClient client, int maxBulkSize, int flushInterval, int concurrency) {
        super(client);
        this.maxBulkSize = maxBulkSize;
        this.flushInterval = flushInterval;
        this.concurrency = concurrency;
    }

    @Override
    public void insert(InsertRequest insertRequest) {
        if (bulkProcessor == null) {
            this.bulkProcessor = getClient().createBulkProcessor(maxBulkSize, flushInterval, concurrency);
        }

        if (insertRequest instanceof BanyanDBTraceInsertRequest) {
            this.bulkProcessor.add(((BanyanDBTraceInsertRequest) insertRequest).getTraceWrite());
        }
    }

    @Override
    public void flush(List<PrepareRequest> prepareRequests) {
        if (bulkProcessor == null) {
            this.bulkProcessor = getClient().createBulkProcessor(maxBulkSize, flushInterval, concurrency);
        }

        if (CollectionUtils.isNotEmpty(prepareRequests)) {
            for (PrepareRequest prepareRequest : prepareRequests) {
                if (prepareRequest instanceof BanyanDBTraceInsertRequest) {
                    this.bulkProcessor.add(((BanyanDBTraceInsertRequest) prepareRequest).getTraceWrite());
                }
            }
        }
    }
}
