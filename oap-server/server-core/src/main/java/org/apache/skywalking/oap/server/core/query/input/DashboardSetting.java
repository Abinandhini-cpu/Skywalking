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

package org.apache.skywalking.oap.server.core.query.input;

import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.oap.server.core.management.ui.template.UITemplate;
import org.apache.skywalking.oap.server.library.util.BooleanUtils;

@Setter
@Getter
public class DashboardSetting {
    private String id;
    private String configuration;

    public UITemplate toEntity() {
        UITemplate uiTemplate = new UITemplate();
        uiTemplate.setTemplateId(this.id);
        uiTemplate.setConfiguration(this.getConfiguration());
        uiTemplate.setUpdateTime(System.currentTimeMillis());
        uiTemplate.setDisabled(BooleanUtils.FALSE);
        return uiTemplate;
    }
}
