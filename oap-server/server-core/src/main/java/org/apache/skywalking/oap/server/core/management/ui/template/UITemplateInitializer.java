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

package org.apache.skywalking.oap.server.core.management.ui.template;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.analysis.Layer;
import org.apache.skywalking.oap.server.core.query.input.DashboardSetting;
import org.apache.skywalking.oap.server.core.query.type.DashboardConfiguration;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.util.ResourceUtils;
import org.apache.skywalking.oap.server.library.util.StringUtil;

/**
 * UITemplateInitializer load the template from the config file in json format. It depends on the UI implementation only.
 * Each config file should be only one dashboard setting json object.
 * The dashboard names should be different in the same Layer and entity.
 */
public class UITemplateInitializer {
    public static String[] UI_TEMPLATE_FOLDER = new String[] {
        Layer.MESH.name(),
        Layer.GENERAL.name(),
        Layer.OS_LINUX.name(),
        Layer.MESH_CP.name(),
        Layer.MESH_DP.name(),
        Layer.K8S.name(),
        Layer.BROWSER.name(),
        Layer.SO11Y_OAP.name(),
        Layer.VIRTUAL_DATABASE.name(),
        Layer.K8S_SERVICE.name(),
        Layer.SO11Y_SATELLITE.name(),
        "custom"
    };
    private final UITemplateManagementService uiTemplateManagementService;
    private final ObjectMapper mapper;

    public UITemplateInitializer(ModuleManager manager) {
        this.uiTemplateManagementService = manager.find(CoreModule.NAME)
                                                  .provider()
                                                  .getService(UITemplateManagementService.class);
        this.mapper = new ObjectMapper();
        this.mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
    }

    public void initAll() throws IOException {
        for (String folder : UITemplateInitializer.UI_TEMPLATE_FOLDER) {
            File[] templateFiles = ResourceUtils.getPathFiles("ui-initialized-templates/" + folder.toLowerCase(
                Locale.ROOT));
            for (File file : templateFiles) {
                initTemplate(file);
            }
        }
    }

    public void initTemplate(File template) throws IOException {
        JsonNode jsonNode = mapper.readTree(template);
        if (jsonNode == null || jsonNode.size() == 0) {
            return;
        }
        if (jsonNode.size() > 1) {
            throw new IllegalArgumentException(
                "File:  " + template.getName() + " should be only one dashboard setting json object.");
        }
        JsonNode configNode = jsonNode.get(0).get("configuration");
        String inId = jsonNode.get(0).get("id").textValue();
        String inNameKey = StringUtil.join('_', configNode.get("layer").textValue(), configNode.get("entity").textValue(), configNode.get("name").textValue());
        verifyNameConflict(template, inId, inNameKey);

        DashboardSetting setting = new DashboardSetting();
        setting.setId(inId);
        setting.setConfiguration(configNode.toString());

        uiTemplateManagementService.addIfNotExist(setting);
    }

    private void verifyNameConflict(File template, String inId, String inNameKey) throws IOException {
        List<DashboardConfiguration> configurations = uiTemplateManagementService.getAllTemplates(false);
        for (DashboardConfiguration config : configurations) {
            JsonNode configNode = mapper.readTree(config.getConfiguration());
            String id = config.getId();
            String nameKey = StringUtil.join(
                '_', configNode.get("layer").textValue(), configNode.get("entity").textValue(),
                configNode.get("name").textValue()
            );
            if (Objects.equals(nameKey, inNameKey) && !id.equals(inId)) {
                throw new IllegalArgumentException(
                    "File:  " + template.getName() + " layer_entity_name: " + inNameKey + " conflict with exist configuration id: " + id);
            }
        }
    }
}
