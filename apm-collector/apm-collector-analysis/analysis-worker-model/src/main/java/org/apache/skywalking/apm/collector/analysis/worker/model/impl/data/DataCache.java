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

package org.apache.skywalking.apm.collector.analysis.worker.model.impl.data;

import org.apache.skywalking.apm.collector.core.cache.Window;
import org.apache.skywalking.apm.collector.core.data.StreamData;

/**
 * @author peng-yongsheng
 */
public class DataCache<STREAM_DATA extends StreamData> extends Window<DataCollection<STREAM_DATA>> {

    private DataCollection<STREAM_DATA> lockedDataCollection;

    @Override public DataCollection<STREAM_DATA> collectionInstance() {
        return new DataCollection<>();
    }

    public boolean containsKey(String id) {
        return lockedDataCollection.containsKey(id);
    }

    public StreamData get(String id) {
        return lockedDataCollection.get(id);
    }

    public void put(String id, STREAM_DATA data) {
        lockedDataCollection.put(id, data);
    }

    public void writing() {
        lockedDataCollection = getCurrentAndWriting();
    }

    public int currentCollectionSize() {
        return getCurrent().size();
    }

    public void finishWriting() {
        lockedDataCollection.finishWriting();
        lockedDataCollection = null;
    }
}
