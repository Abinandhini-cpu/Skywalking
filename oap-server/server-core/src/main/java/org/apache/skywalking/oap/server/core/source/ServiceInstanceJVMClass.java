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

package org.apache.skywalking.oap.server.core.source;

import lombok.Getter;
import lombok.Setter;

import static org.apache.skywalking.oap.server.core.source.DefaultScopeDefine.SERVICE_INSTANCE_CATALOG_NAME;
import static org.apache.skywalking.oap.server.core.source.DefaultScopeDefine.SERVICE_INSTANCE_JVM_CLASS;

@ScopeDeclaration(id = SERVICE_INSTANCE_JVM_CLASS, name = "ServiceInstanceJVMClass", catalog = SERVICE_INSTANCE_CATALOG_NAME)
@ScopeDefaultColumn.VirtualColumnDefinition(fieldName = "entityId", columnName = "entity_id", isID = true, type = String.class, idxOfCompositeID = 1)
public class ServiceInstanceJVMClass extends Source {
    @Override
    public int scope() {
        return SERVICE_INSTANCE_JVM_CLASS;
    }

    @Override
    public String getEntityId() {
        return String.valueOf(id);
    }

    @Getter
    @Setter
    private String id;
    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = "name", requireDynamicActive = true)
    private String name;
    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = "service_name", requireDynamicActive = true)
    private String serviceName;
    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = "service_id", idxOfCompositeID = 0)
    private String serviceId;
    @Getter
    @Setter
    private long loadedClassCount;
    @Getter
    @Setter
    private long totalUnloadedClassCount;
    @Getter
    @Setter
    private long totalLoadedClassCount;
}
