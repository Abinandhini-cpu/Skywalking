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

package org.apache.skywalking.oap.server.storage.plugin.elasticsearch.base;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.util.StringUtil;
import org.apache.skywalking.oap.server.core.storage.StorageException;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.core.storage.model.ModelColumn;
import org.apache.skywalking.oap.server.core.storage.model.ModelInstaller;
import org.apache.skywalking.oap.server.library.client.Client;
import org.apache.skywalking.oap.server.library.client.elasticsearch.ElasticSearchClient;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.storage.plugin.elasticsearch.StorageModuleElasticsearchConfig;
import org.elasticsearch.common.unit.TimeValue;

@Slf4j
public class StorageEsInstaller extends ModelInstaller {
    private final Gson gson = new Gson();
    private final StorageModuleElasticsearchConfig config;
    protected final ColumnTypeEsMapping columnTypeEsMapping;

    /**
     * The mappings of the template .
     */
    private final Map<String, Map<String, Object>> tables;

    public StorageEsInstaller(Client client,
                              ModuleManager moduleManager,
                              StorageModuleElasticsearchConfig config) throws StorageException {
        super(client, moduleManager);
        this.columnTypeEsMapping = new ColumnTypeEsMapping();
        this.config = config;
        this.tables = new HashMap<>();
    }

    @Override
    protected boolean isExists(Model model) throws StorageException {
        ElasticSearchClient esClient = (ElasticSearchClient) client;
        String tableName = PhysicalIndexManager.INSTANCE.getTableName(model);
        PhysicalIndices.registerRelation(model.getName(), tableName);
        try {
            if (model.isTimeSeries()) {
                return esClient.isExistsTemplate(tableName)
                    && esClient.isExistsIndex(TimeSeriesUtils.latestWriteIndexName(model))
                    && esClient.getDocNumber(tableName) > 0;
            } else {
                return esClient.isExistsIndex(tableName);
            }
        } catch (IOException e) {
            throw new StorageException(e.getMessage());
        }
    }

    @Override
    protected void createTable(Model model) throws StorageException {
        ElasticSearchClient esClient = (ElasticSearchClient) client;
        Map<String, Object> settings = createSetting(model);
        Map<String, Object> mapping = createMapping(model);
        String tableName = PhysicalIndexManager.INSTANCE.getTableName(model);
        PhysicalIndices.registerRelation(model.getName(), tableName);
        log.info("index {}'s columnTypeEsMapping builder str: {}",
                 esClient.formatIndexName(tableName), mapping.toString()
        );

        String indexName;
        try {
            if (model.isTimeSeries()) {
                if (!esClient.isExistsTemplate(tableName) || !isTemplateMappingCompatible(tableName, mapping)) {
                    Map<String, Object> templateMapping = appendTemplateMapping(tableName, mapping);
                    boolean isAcknowledged = esClient.createOrUpdateTemplate(tableName, settings, templateMapping);
                    log.info("create {} index template finished, isAcknowledged: {}", tableName, isAcknowledged);
                    if (!isAcknowledged) {
                        throw new StorageException("create " + tableName + " index template failure, ");
                    }
                }
                indexName = TimeSeriesUtils.latestWriteIndexName(model);
            } else {
                indexName = tableName;
            }

            if (esClient.isExistsIndex(indexName)) {
                boolean isAcknowledged = esClient.deleteByIndexName(indexName);
                if (!isAcknowledged) {
                    throw new StorageException("delete " + indexName + " time series index failure, ");
                }
            }
            boolean isAcknowledged = esClient.createIndex(indexName);
            log.info("create {} index finished, isAcknowledged: {}", indexName, isAcknowledged);
            if (!isAcknowledged) {
                throw new StorageException("create " + indexName + " time series index failure, ");
            }

        } catch (IOException e) {
            throw new StorageException(e.getMessage());
        }
    }

    /**
     * Append the mapping to the tables with the same table name key.
     */
    private Map<String, Object> appendTemplateMapping(String tableName, Map<String, Object> mapping) {
        if (!tables.containsKey(tableName)) {
            tables.put(tableName, mapping);
            return mapping;
        }
        Map<String, Object> existMapping = tables.get(tableName);
        Map<String, Object> existFields = getColumnProperties(existMapping);
        Map<String, Object> checkingFields = getColumnProperties(mapping);
        Map<String, Object> newFields = checkingFields.entrySet()
                                                      .stream()
                                                      .filter(item -> !existFields.containsKey(item.getKey()))
                                                      .collect(Collectors.toMap(
                                                          Map.Entry::getKey, Map.Entry::getValue));
        newFields.forEach(existFields::put);
        return existMapping;
    }

    protected Map<String, Object> getColumnProperties(Map<String, Object> mapping) {
        if (Objects.isNull(mapping) || mapping.size() == 0) {
            return new HashMap<>();
        }
        return (Map<String, Object>) ((Map<String, Object>) mapping.get(ElasticSearchClient.TYPE)).get("properties");
    }

    /**
     * Whether the tables contains the input mapping with the same table name key.
     */
    private boolean isTemplateMappingCompatible(String tableName, Map<String, Object> mapping) {
        if (!tables.containsKey(tableName)) {
            return false;
        }
        Map<String, Object> existMapping = tables.get(tableName);
        Map<String, Object> existFields = getColumnProperties(existMapping);
        Map<String, Object> checkingFields = getColumnProperties(mapping);
        return checkingFields.entrySet()
                             .stream().allMatch(item -> existFields.containsKey(item.getKey()));
    }

    protected Map<String, Object> createSetting(Model model) throws StorageException {
        Map<String, Object> setting = new HashMap<>();

        setting.put("index.number_of_replicas", model.isSuperDataset()
            ? config.getSuperDatasetIndexReplicasNumber()
            : config.getIndexReplicasNumber());
        setting.put("index.number_of_shards", model.isSuperDataset()
            ? config.getIndexShardsNumber() * config.getSuperDatasetIndexShardsFactor()
            : config.getIndexShardsNumber());
        setting.put("index.refresh_interval", model.isRecord()
            ? TimeValue.timeValueSeconds(10).toString()
            : TimeValue.timeValueSeconds(config.getFlushInterval()).toString());
        setting.put("analysis", getAnalyzerSetting(model.getColumns()));
        if (!StringUtil.isEmpty(config.getAdvanced())) {
            Map<String, Object> advancedSettings = gson.fromJson(config.getAdvanced(), Map.class);
            advancedSettings.forEach(setting::put);
        }
        return setting;
    }

    private Map getAnalyzerSetting(List<ModelColumn> analyzerTypes) throws StorageException {
        AnalyzerSetting analyzerSetting = new AnalyzerSetting();
        for (final ModelColumn column : analyzerTypes) {
            AnalyzerSetting setting = AnalyzerSetting.Generator.getGenerator(column.getAnalyzer())
                                                               .getGenerateFunc()
                                                               .generate(config);
            analyzerSetting.combine(setting);
        }
        return gson.fromJson(gson.toJson(analyzerSetting), Map.class);
    }

    protected Map<String, Object> createMapping(Model model) {
        Map<String, Object> mapping = new HashMap<>();
        Map<String, Object> type = new HashMap<>();

        mapping.put(ElasticSearchClient.TYPE, type);

        Map<String, Object> properties = new HashMap<>();
        type.put("properties", properties);

        for (ModelColumn columnDefine : model.getColumns()) {
            if (columnDefine.isMatchQuery()) {
                String matchCName = MatchCNameBuilder.INSTANCE.build(columnDefine.getColumnName().getName());

                Map<String, Object> originalColumn = new HashMap<>();
                originalColumn.put(
                    "type", columnTypeEsMapping.transform(columnDefine.getType(), columnDefine.getGenericType()));
                originalColumn.put("copy_to", matchCName);
                properties.put(columnDefine.getColumnName().getName(), originalColumn);

                Map<String, Object> matchColumn = new HashMap<>();
                matchColumn.put("type", "text");
                matchColumn.put("analyzer", columnDefine.getAnalyzer().getName());
                properties.put(matchCName, matchColumn);
            } else {
                Map<String, Object> column = new HashMap<>();
                column.put(
                    "type", columnTypeEsMapping.transform(columnDefine.getType(), columnDefine.getGenericType()));
                if (columnDefine.isStorageOnly()) {
                    column.put("index", false);
                }
                properties.put(columnDefine.getColumnName().getName(), column);
            }
        }

        if (PhysicalIndexManager.INSTANCE.isAggregationMode(model)) {
            Map<String, Object> column = new HashMap<>();
            column.put("type", "keyword");
            properties.put(PhysicalIndexManager.LOGIC_TABLE_NAME, column);
        }

        log.debug("elasticsearch index template setting: {}", mapping.toString());

        return mapping;
    }
}
