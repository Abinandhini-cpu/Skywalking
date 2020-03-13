#!/usr/bin/env bash
# Licensed to the SkyAPM under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

cat /etc/hosts

echo 'starting OAP server...' \
    && export SW_CORE_GRPC_SSL_ENABLED=true \
    && export SW_CORE_GRPC_SSL_KEY_PATH="${SW_HOME}/certs/server-key.pem" \
    && export SW_CORE_GRPC_SSL_CERT_CHAIN_PATH="${SW_HOME}/certs/server.crt" \
    && export SW_CORE_GRPC_SSL_TRUSTED_CA_PATH="${SW_HOME}/certs/ca.crt" \
    && start_oap 'init'

echo 'starting Web app...' \
    && start_webapp '0.0.0.0' 8080

echo 'starting instrumented services...' \
    && start_instrumented_services

check_tcp 127.0.0.1 \
          9090 \
          60 \
          10 \
          "waiting for the instrumented service to be ready"

if [[ $? -ne 0 ]]; then
    echo "instrumented service failed to start in 30 * 10 seconds: "
    cat ${SERVICE_LOG}/*
    exit 1
fi

echo "SkyWalking e2e container is ready for tests"

tail -f ${OAP_LOG_DIR}/* \
        ${WEBAPP_LOG_DIR}/* \
        ${SERVICE_LOG}/* \
        ${ES_HOME}/logs/stdout.log
