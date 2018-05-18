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

package org.apache.skywalking.apm.collector.ui.service;

import org.apache.skywalking.apm.collector.core.module.ModuleManager;
import org.apache.skywalking.apm.collector.storage.StorageModule;
import org.apache.skywalking.apm.collector.storage.dao.ui.INetworkAddressUIDAO;
import org.apache.skywalking.apm.network.proto.SpanLayer;

/**
 * @author peng-yongsheng
 */
public class NetworkAddressService {

    private final INetworkAddressUIDAO networkAddressUIDAO;

    public NetworkAddressService(ModuleManager moduleManager) {
        this.networkAddressUIDAO = moduleManager.find(StorageModule.NAME).getService(INetworkAddressUIDAO.class);
    }

    public int getNumOfDatabase() {
        return networkAddressUIDAO.getNumOfSpanLayer(SpanLayer.Database_VALUE);
    }

    public int getNumOfCache() {
        return networkAddressUIDAO.getNumOfSpanLayer(SpanLayer.Cache_VALUE);
    }

    public int getNumOfMQ() {
        return networkAddressUIDAO.getNumOfSpanLayer(SpanLayer.MQ_VALUE);
    }
}
