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

package org.apache.skywalking.oap.server.configuration.api;

import lombok.Getter;
import org.apache.skywalking.oap.server.library.module.*;

/**
 * ConfigChangeWatcher represents a watcher implementor,
 * it will be called when the target value changed.
 *
 * @author wusheng
 */
@Getter
public abstract class ConfigChangeWatcher {
    private final ModuleDefine module;
    private final ModuleProvider provider;
    private final String itemName;

    public ConfigChangeWatcher(ModuleDefine module, ModuleProvider provider, String itemName) {
        this.module = module;
        this.provider = provider;
        this.itemName = itemName;
    }

    /**
     * Notify the watcher, the new value received.
     *
     * @param value of new.
     */
    public abstract void notify(String value);

    /**
     * @return current value of current config.
     */
    public abstract String value();

    @Override public String toString() {
        return "ConfigChangeWatcher{" +
            "module=" + module +
            ", provider=" + provider +
            ", itemName='" + itemName + '\'' +
            '}';
    }
}
