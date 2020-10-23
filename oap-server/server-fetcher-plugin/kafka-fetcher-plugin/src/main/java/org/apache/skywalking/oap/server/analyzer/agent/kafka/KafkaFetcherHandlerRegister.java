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

package org.apache.skywalking.oap.server.analyzer.agent.kafka;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.utils.Bytes;
import org.apache.skywalking.apm.util.StringUtil;
import org.apache.skywalking.oap.server.analyzer.agent.kafka.module.KafkaFetcherConfig;
import org.apache.skywalking.oap.server.analyzer.agent.kafka.provider.handler.KafkaHandler;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.library.server.grpc.CustomThreadFactory;

/**
 * Configuring and initializing a KafkaConsumer client as a dispatcher to delivery Kafka Message to registered handler by topic.
 */
@Slf4j
public class KafkaFetcherHandlerRegister implements Runnable {

    private ImmutableMap.Builder<String, KafkaHandler> builder = ImmutableMap.builder();
    private ImmutableMap<String, KafkaHandler> handlerMap;

    private List<TopicPartition> topicPartitions = Lists.newArrayList();
    private KafkaConsumer<String, Bytes> consumer = null;
    private final KafkaFetcherConfig config;
    private final boolean isSharding;
    private final boolean enableKafkaMessageAutoCommit;

    private int threadPoolSize = Runtime.getRuntime().availableProcessors() * 2;
    private int threadPoolQueueSize = 10000;
    private final ThreadPoolExecutor executor;

    public KafkaFetcherHandlerRegister(KafkaFetcherConfig config) throws ModuleStartException {
        this.config = config;
        this.enableKafkaMessageAutoCommit = config.isEnableKafkaMessageAutoCommit();

        Properties properties = new Properties();
        properties.putAll(config.getKafkaConsumerConfig());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, config.getGroupId());
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(enableKafkaMessageAutoCommit));

        AdminClient adminClient = AdminClient.create(properties);
        Set<String> missedTopics = adminClient.describeTopics(Lists.newArrayList(
            config.getTopicNameOfManagements(),
            config.getTopicNameOfMetrics(),
            config.getTopicNameOfProfiling(),
            config.getTopicNameOfTracingSegments(),
            config.getTopicNameOfMeters()
        ))
                                              .values()
                                              .entrySet()
                                              .stream()
                                              .map(entry -> {
                                                  try {
                                                      entry.getValue().get();
                                                      return null;
                                                  } catch (InterruptedException | ExecutionException e) {
                                                  }
                                                  return entry.getKey();
                                              })
                                              .filter(Objects::nonNull)
                                              .collect(Collectors.toSet());

        if (!missedTopics.isEmpty()) {
            log.info("Topics" + missedTopics.toString() + " not exist.");
            List<NewTopic> newTopicList = missedTopics.stream()
                                                      .map(topic -> new NewTopic(
                                                          topic,
                                                          config.getPartitions(),
                                                          (short) config.getReplicationFactor()
                                                      )).collect(Collectors.toList());

            try {
                adminClient.createTopics(newTopicList).all().get();
            } catch (Exception e) {
                throw new ModuleStartException("Failed to create Kafka Topics" + missedTopics + ".", e);
            }
        }

        if (config.isSharding() && StringUtil.isNotEmpty(config.getConsumePartitions())) {
            isSharding = true;
        } else {
            isSharding = false;
        }
        if (config.getKafkaHandlerThreadPoolSize() > 0) {
            threadPoolSize = config.getKafkaHandlerThreadPoolSize();
        }
        if (config.getKafkaHandlerThreadPoolQueueSize() > 0) {
            threadPoolQueueSize = config.getKafkaHandlerThreadPoolQueueSize();
        }

        consumer = new KafkaConsumer<>(properties, new StringDeserializer(), new BytesDeserializer());
        executor = new ThreadPoolExecutor(threadPoolSize, threadPoolSize,
                                          60, TimeUnit.SECONDS,
                                          new ArrayBlockingQueue(threadPoolQueueSize),
                                          new CustomThreadFactory("kafkaHandlerPool"),
                                          new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    public void register(KafkaHandler handler) {
        builder.put(handler.getTopic(), handler);
        topicPartitions.addAll(handler.getTopicPartitions());
    }

    public void start() {
        handlerMap = builder.build();
        if (isSharding) {
            consumer.assign(topicPartitions);
        } else {
            consumer.subscribe(handlerMap.keySet());
        }
        consumer.seekToEnd(consumer.assignment());
        Executors.newSingleThreadExecutor(new DefaultThreadFactory("KafkaConsumer")).submit(this);
    }

    @Override
    public void run() {
        while (true) {
            try {
                ConsumerRecords<String, Bytes> consumerRecords = consumer.poll(Duration.ofMillis(500L));
                if (!consumerRecords.isEmpty()) {
                    Iterator<ConsumerRecord<String, Bytes>> iterator = consumerRecords.iterator();
                    while (iterator.hasNext()) {
                        ConsumerRecord<String, Bytes> record = iterator.next();
                        executor.submit(() -> handlerMap.get(record.topic()).handle(record));
                    }
                    if (!enableKafkaMessageAutoCommit) {
                        consumer.commitAsync();
                    }
                }
            } catch (Exception e) {
                log.error("Kafka handle message error.", e);
            }
        }
    }
}
