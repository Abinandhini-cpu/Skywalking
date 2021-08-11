package org.apache.skywalking.banyandb.client.impl;

import com.google.common.collect.ImmutableList;
import org.apache.skywalking.banyandb.Database;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BanyanDBSchema {
    static final Database.Metadata METADATA = Database.Metadata.newBuilder().setName("sw").setGroup("default").build();
    static final List<String> FIELD_NAMES;

    static {
        Set<String> fields = new LinkedHashSet<>();
        fields.add("trace_id");
        fields.add("state");
        fields.add("service_id");
        fields.add("service_instance_id");
        fields.add("endpoint_id");
        fields.add("service_name");
        fields.add("service_instance_name");
        fields.add("endpoint_name");
        fields.add("duration");
        fields.add("start_time");
        fields.add("http.method");
        fields.add("status_code");
        fields.add("db.type");
        fields.add("db.instance");
        fields.add("mq.queue");
        fields.add("mq.topic");
        fields.add("mq.broker");
        FIELD_NAMES = ImmutableList.copyOf(fields);
    }
}
