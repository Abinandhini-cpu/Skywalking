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


package org.apache.skywalking.apm.plugin.jedis.v2;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;

public enum RedisMethodMatch {
    INSTANCE;

    private ElementMatcher.Junction<MethodDescription> getIntersectionalMethodMacher() {
        return named("zcount").or(named("sunionstore")).or(named("zunionstore"))
            .or(named("del")).or(named("zinterstore")).or(named("echo"))
            .or(named("hscan")).or(named("psubscribe")).or(named("type"))
            .or(named("sinterstore")).or(named("setex")).or(named("zlexcount"))
            .or(named("brpoplpush")).or(named("bitcount")).or(named("llen"))
            .or(named("zscan")).or(named("lpushx")).or(named("bitpos"))
            .or(named("setnx")).or(named("hvals")).or(named("evalsha"))
            .or(named("substr")).or(named("geodist")).or(named("zrangeByLex"))
            .or(named("geoadd")).or(named("expire")).or(named("bitop"))
            .or(named("zrangeByScore")).or(named("smove")).or(named("lset"))
            .or(named("decrBy")).or(named("pttl")).or(named("scan"))
            .or(named("zrank")).or(named("blpop")).or(named("rpoplpush"))
            .or(named("zremrangeByLex")).or(named("get")).or(named("lpop"))
            .or(named("persist")).or(named("scriptExists")).or(named("georadius"))
            .or(named("set")).or(named("srandmember")).or(named("incr")).or(named("setbit"))
            .or(named("hexists")).or(named("expireAt")).or(named("pexpire")).or(named("zcard"))
            .or(named("bitfield")).or(named("zrevrangeByLex")).or(named("sinter")).or(named("srem"))
            .or(named("getrange")).or(named("rename")).or(named("zrevrank")).or(named("exists"))
            .or(named("setrange")).or(named("zremrangeByRank")).or(named("sadd")).or(named("sdiff"))
            .or(named("zrevrange")).or(named("getbit")).or(named("scard")).or(named("sdiffstore"))
            .or(named("zrevrangeByScore")).or(named("zincrby")).or(named("rpushx")).or(named("psetex"))
            .or(named("zrevrangeWithScores")).or(named("strlen")).or(named("hdel")).or(named("zremrangeByScore"))
            .or(named("geohash")).or(named("brpop")).or(named("lrem")).or(named("hlen")).or(named("decr"))
            .or(named("scriptLoad")).or(named("lpush")).or(named("lindex")).or(named("zrange")).or(named("incrBy"))
            .or(named("getSet")).or(named("ltrim")).or(named("incrByFloat")).or(named("rpop")).or(named("sort"))
            .or(named("zrevrangeByScoreWithScores")).or(named("pfadd")).or(named("eval")).or(named("linsert"))
            .or(named("pfcount")).or(named("hkeys")).or(named("hsetnx")).or(named("hincrBy")).or(named("hgetAll"))
            .or(named("hset")).or(named("spop")).or(named("zrangeWithScores")).or(named("hincrByFloat"))
            .or(named("hmset")).or(named("renamenx")).or(named("zrem")).or(named("msetnx")).or(named("hmget"))
            .or(named("sunion")).or(named("hget")).or(named("zadd")).or(named("move")).or(named("subscribe"))
            .or(named("geopos")).or(named("mset")).or(named("zrangeByScoreWithScores")).or(named("zscore"))
            .or(named("pexpireAt")).or(named("georadiusByMember")).or(named("ttl")).or(named("lrange"))
            .or(named("smembers")).or(named("pfmerge")).or(named("rpush")).or(named("publish"))
            .or(named("mget")).or(named("sscan")).or(named("append")).or(named("sismember"));
    }

    public ElementMatcher<MethodDescription> getJedisMethodMatcher() {
        return getIntersectionalMethodMacher().or(named("sentinelMasters")).or(named("clusterReplicate")).or(named("readonly"))
            .or(named("randomKey")).or(named("clusterInfo")).or(named("pubsubNumSub"))
            .or(named("sentinelSlaves")).or(named("clusterSetSlotImporting")).or(named("clusterSlaves"))
            .or(named("clusterFailover")).or(named("clusterSetSlotMigrating")).or(named("watch"))
            .or(named("clientKill")).or(named("clusterKeySlot")).or(named("clusterCountKeysInSlot"))
            .or(named("sentinelGetMasterAddrByName")).or(named("objectRefcount")).or(named("clusterMeet"))
            .or(named("sentinelSet")).or(named("clusterSetSlotNode")).or(named("clusterAddSlots"))
            .or(named("pubsubNumPat")).or(named("slowlogGet")).or(named("sentinelReset")).or(named("clusterNodes"))
            .or(named("sentinelMonitor")).or(named("configGet")).or(named("objectIdletime"))
            .or(named("pubsubChannels")).or(named("getParams")).or(named("sentinelRemove"))
            .or(named("migrate")).or(named("clusterForget")).or(named("asking")).or(named("keys"))
            .or(named("clientSetname")).or(named("clusterSaveConfig")).or(named("configSet"))
            .or(named("dump")).or(named("clusterFlushSlots")).or(named("clusterGetKeysInSlot"))
            .or(named("clusterReset")).or(named("restore")).or(named("clusterDelSlots"))
            .or(named("sentinelFailover")).or(named("clusterSetSlotStable")).or(named("objectEncoding"));
    }

    public ElementMatcher<MethodDescription> getJedisClusterMethodMatcher() {
        return getIntersectionalMethodMacher();
    }
}
