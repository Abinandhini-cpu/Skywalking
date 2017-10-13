/*
 * Copyright 2017, OpenSkywalking Organization All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project repository: https://github.com/OpenSkywalking/skywalking
 */

package org.skywalking.apm.collector.ui.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.skywalking.apm.collector.core.util.Const;
import org.skywalking.apm.collector.storage.dao.DAOContainer;
import org.skywalking.apm.collector.ui.dao.IServiceNameDAO;

/**
 * @author pengys5
 */
public class ServiceIdCache {

    //TODO size configuration
    private static Cache<String, Integer> CACHE = CacheBuilder.newBuilder().maximumSize(1000).build();

    public static int get(int applicationId, String serviceName) {
        try {
            return CACHE.get(applicationId + Const.ID_SPLIT + serviceName, () -> {
                IServiceNameDAO dao = (IServiceNameDAO)DAOContainer.INSTANCE.get(IServiceNameDAO.class.getName());
                return dao.getServiceId(applicationId, serviceName);
            });
        } catch (Throwable e) {
            return 0;
        }
    }

    public static int getForUI(int applicationId, String serviceName) {
        int serviceId = get(applicationId, serviceName);
        if (serviceId == 0) {
            IServiceNameDAO dao = (IServiceNameDAO)DAOContainer.INSTANCE.get(IServiceNameDAO.class.getName());
            serviceId = dao.getServiceId(applicationId, serviceName);
            CACHE.put(applicationId + Const.ID_SPLIT + serviceName, serviceId);
        }
        return serviceId;
    }
}
