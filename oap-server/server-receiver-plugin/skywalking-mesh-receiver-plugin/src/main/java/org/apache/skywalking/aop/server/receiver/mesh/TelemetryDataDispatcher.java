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

package org.apache.skywalking.aop.server.receiver.mesh;

import org.apache.logging.log4j.util.Strings;
import org.apache.skywalking.apm.network.servicemesh.Protocol;
import org.apache.skywalking.apm.network.servicemesh.ServiceMeshMetric;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.cache.ServiceInstanceInventoryCache;
import org.apache.skywalking.oap.server.core.cache.ServiceInventoryCache;
import org.apache.skywalking.oap.server.core.source.DetectPoint;
import org.apache.skywalking.oap.server.core.source.Endpoint;
import org.apache.skywalking.oap.server.core.source.RequestType;
import org.apache.skywalking.oap.server.core.source.Service;
import org.apache.skywalking.oap.server.core.source.ServiceInstance;
import org.apache.skywalking.oap.server.core.source.ServiceInstanceRelation;
import org.apache.skywalking.oap.server.core.source.ServiceRelation;
import org.apache.skywalking.oap.server.core.source.SourceReceiver;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.util.TimeBucketUtils;

/**
 * TelemetryDataDispatcher processes the {@link ServiceMeshMetric} format telemetry data, transfers it to source
 * dispatcher.
 *
 * @author wusheng
 */
public class TelemetryDataDispatcher {
    private static MeshDataBufferFileCache CACHE;
    private static ServiceInventoryCache SERVICE_CACHE;
    private static ServiceInstanceInventoryCache SERVICE_INSTANCE_CACHE;
    private static SourceReceiver SOURCE_RECEIVER;

    private TelemetryDataDispatcher() {

    }

    public static void setCache(MeshDataBufferFileCache cache, ModuleManager moduleManager) {
        CACHE = cache;
        SERVICE_CACHE = moduleManager.find(CoreModule.NAME).getService(ServiceInventoryCache.class);
        SERVICE_INSTANCE_CACHE = moduleManager.find(CoreModule.NAME).getService(ServiceInstanceInventoryCache.class);
        SOURCE_RECEIVER = moduleManager.find(CoreModule.NAME).getService(SourceReceiver.class);
    }

    public static void preProcess(ServiceMeshMetric data) {
        CACHE.in(data);
    }

    /**
     * The {@link ServiceMeshMetricDataDecorator} is standard, all metadata registered through {@link #CACHE}
     *
     * @param decorator
     */
    static void doDispatch(ServiceMeshMetricDataDecorator decorator) {
        ServiceMeshMetric metric = decorator.getMetric();
        long minuteTimeBucket = TimeBucketUtils.INSTANCE.getMinuteTimeBucket(metric.getStartTime());
        toService(decorator, minuteTimeBucket);
        toServiceRelation(decorator, minuteTimeBucket);
        toServiceInstance(decorator, minuteTimeBucket);
        toServiceInstanceRelation(decorator, minuteTimeBucket);
        toEndpoint(decorator, minuteTimeBucket);
    }

    private static void toService(ServiceMeshMetricDataDecorator decorator, long minuteTimeBucket) {
        ServiceMeshMetric metric = decorator.getMetric();
        Service service = new Service();
        service.setTimeBucket(minuteTimeBucket);
        service.setId(metric.getDestServiceId());
        service.setName(getServiceName(metric.getDestServiceId(), metric.getDestServiceName()));
        service.setServiceInstanceName(getServiceInstanceName(metric.getDestServiceInstanceId(), metric.getDestServiceInstance()));
        service.setEndpointName(metric.getEndpoint());
        service.setLatency(metric.getLatency());
        service.setStatus(metric.getStatus());
        service.setType(protocol2Type(metric.getProtocol()));

        SOURCE_RECEIVER.receive(service);
    }

    private static void toServiceRelation(ServiceMeshMetricDataDecorator decorator, long minuteTimeBucket) {
        ServiceMeshMetric metric = decorator.getMetric();
        ServiceRelation serviceRelation = new ServiceRelation();
        serviceRelation.setTimeBucket(minuteTimeBucket);
        serviceRelation.setSourceServiceId(metric.getSourceServiceId());
        serviceRelation.setSourceServiceName(getServiceName(metric.getSourceServiceId(), metric.getSourceServiceName()));
        serviceRelation.setSourceServiceInstanceName(getServiceInstanceName(metric.getSourceServiceInstanceId(), metric.getSourceServiceInstance()));

        serviceRelation.setDestServiceId(metric.getDestServiceId());
        serviceRelation.setDestServiceName(getServiceName(metric.getDestServiceId(), metric.getDestServiceName()));
        serviceRelation.setDestServiceInstanceName(getServiceInstanceName(metric.getDestServiceInstanceId(), metric.getDestServiceInstance()));

        serviceRelation.setEndpoint(metric.getEndpoint());
        serviceRelation.setLatency(metric.getLatency());
        serviceRelation.setStatus(metric.getStatus());
        serviceRelation.setType(protocol2Type(metric.getProtocol()));
        serviceRelation.setResponseCode(metric.getResponseCode());
        serviceRelation.setDetectPoint(detectPointMapping(metric.getDetectPoint()));

        SOURCE_RECEIVER.receive(serviceRelation);
    }

    private static void toServiceInstance(ServiceMeshMetricDataDecorator decorator, long minuteTimeBucket) {
        ServiceMeshMetric metric = decorator.getMetric();
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setTimeBucket(minuteTimeBucket);
        serviceInstance.setId(metric.getDestServiceId());
        serviceInstance.setName(getServiceInstanceName(metric.getDestServiceInstanceId(), metric.getDestServiceInstance()));
        serviceInstance.setServiceId(metric.getDestServiceId());
        serviceInstance.setServiceName(getServiceName(metric.getDestServiceId(), metric.getDestServiceName()));
        serviceInstance.setEndpointName(metric.getEndpoint());
        serviceInstance.setLatency(metric.getLatency());
        serviceInstance.setStatus(metric.getStatus());
        serviceInstance.setType(protocol2Type(metric.getProtocol()));

        SOURCE_RECEIVER.receive(serviceInstance);
    }

    private static void toServiceInstanceRelation(ServiceMeshMetricDataDecorator decorator, long minuteTimeBucket) {
        ServiceMeshMetric metric = decorator.getMetric();
        ServiceInstanceRelation serviceRelation = new ServiceInstanceRelation();
        serviceRelation.setTimeBucket(minuteTimeBucket);
        serviceRelation.setSourceServiceInstanceId(metric.getSourceServiceInstanceId());
        serviceRelation.setSourceServiceInstanceName(getServiceInstanceName(metric.getSourceServiceInstanceId(), metric.getSourceServiceInstance()));
        serviceRelation.setSourceServiceId(metric.getSourceServiceId());
        serviceRelation.setSourceServiceName(getServiceName(metric.getSourceServiceId(), metric.getSourceServiceName()));

        serviceRelation.setDestServiceInstanceId(metric.getDestServiceInstanceId());
        serviceRelation.setDestServiceInstanceName(getServiceInstanceName(metric.getDestServiceInstanceId(), metric.getDestServiceInstance()));
        serviceRelation.setDestServiceId(metric.getDestServiceId());
        serviceRelation.setDestServiceName(getServiceName(metric.getDestServiceId(), metric.getDestServiceName()));

        serviceRelation.setEndpoint(metric.getEndpoint());
        serviceRelation.setLatency(metric.getLatency());
        serviceRelation.setStatus(metric.getStatus());
        serviceRelation.setType(protocol2Type(metric.getProtocol()));
        serviceRelation.setResponseCode(metric.getResponseCode());
        serviceRelation.setDetectPoint(detectPointMapping(metric.getDetectPoint()));

        SOURCE_RECEIVER.receive(serviceRelation);
    }

    private static void toEndpoint(ServiceMeshMetricDataDecorator decorator, long minuteTimeBucket) {
        ServiceMeshMetric metric = decorator.getMetric();
        Endpoint endpoint = new Endpoint();
        endpoint.setTimeBucket(minuteTimeBucket);
        endpoint.setId(decorator.getEndpointId());
        endpoint.setName(metric.getEndpoint());
        endpoint.setServiceId(metric.getDestServiceId());
        endpoint.setServiceName(getServiceName(metric.getDestServiceId(), metric.getDestServiceName()));
        endpoint.setServiceInstanceId(metric.getDestServiceInstanceId());
        endpoint.setServiceInstanceName(getServiceInstanceName(metric.getDestServiceInstanceId(), metric.getDestServiceInstance()));

        endpoint.setLatency(metric.getLatency());
        endpoint.setStatus(metric.getStatus());
        endpoint.setType(protocol2Type(metric.getProtocol()));

        SOURCE_RECEIVER.receive(endpoint);
    }

    private static RequestType protocol2Type(Protocol protocol) {
        switch (protocol) {
            case gRPC:
                return RequestType.gRPC;
            case HTTP:
                return RequestType.HTTP;
            case UNRECOGNIZED:
            default:
                return RequestType.RPC;
        }
    }

    private static DetectPoint detectPointMapping(org.apache.skywalking.apm.network.common.DetectPoint detectPoint) {
        switch (detectPoint) {
            case client:
                return DetectPoint.CLIENT;
            case server:
                return DetectPoint.SERVER;
            case proxy:
                return DetectPoint.PROXY;
            default:
                return DetectPoint.SERVER;
        }
    }

    private static String getServiceName(int serviceId, String serviceName) {
        if (Strings.isBlank(serviceName)) {
            return SERVICE_CACHE.get(serviceId).getName();
        } else {
            return serviceName;
        }
    }

    private static String getServiceInstanceName(int serviceInstanceId, String serviceInstanceName) {
        if (Strings.isBlank(serviceInstanceName)) {
            return SERVICE_INSTANCE_CACHE.get(serviceInstanceId).getName();
        } else {
            return serviceInstanceName;
        }
    }
}
