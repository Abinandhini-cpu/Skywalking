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

package org.apache.skywalking.apm.collector.storage.h2.dao;

import org.apache.skywalking.apm.collector.client.h2.H2Client;
import org.apache.skywalking.apm.collector.storage.dao.alarm.IApplicationReferenceAlarmPersistenceDAO;
import org.apache.skywalking.apm.collector.storage.h2.base.dao.H2DAO;
import org.apache.skywalking.apm.collector.storage.h2.base.define.H2SqlEntity;
import org.apache.skywalking.apm.collector.storage.table.alarm.ApplicationReferenceAlarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author peng-yongsheng
 */
public class ApplicationReferenceAlarmH2PersistenceDAO extends H2DAO implements IApplicationReferenceAlarmPersistenceDAO<H2SqlEntity, H2SqlEntity, ApplicationReferenceAlarm> {

    private final Logger logger = LoggerFactory.getLogger(ApplicationReferenceAlarmH2PersistenceDAO.class);

    public ApplicationReferenceAlarmH2PersistenceDAO(H2Client client) {
        super(client);
    }

    @Override public ApplicationReferenceAlarm get(String id) {
        return null;
    }

    @Override public H2SqlEntity prepareBatchInsert(ApplicationReferenceAlarm data) {
        return null;
    }

    @Override public H2SqlEntity prepareBatchUpdate(ApplicationReferenceAlarm data) {
        return null;
    }

    @Override public void deleteHistory(Long startTimestamp, Long endTimestamp) {

    }
}
