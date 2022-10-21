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

package org.apache.skywalking.oap.meter.analyzer.prometheus.rule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.library.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Rules is factory to instance {@link Rule} from a local file.
 */
public class Rules {
    private static final Logger LOG = LoggerFactory.getLogger(Rule.class);

    public static List<Rule> loadRules(final String path) throws ModuleStartException {
        return loadRules(path, Collections.emptyList());
    }

    public static List<Rule> loadRules(final String path, List<String> enabledRules) throws ModuleStartException {
        File[] rules;
        try {
            rules = ResourceUtils.getAllPathFiles(path);
        } catch (FileNotFoundException e) {
            throw new ModuleStartException("Load fetcher rules failed", e);
        }
        final List<String> formedEnabledRules =
                enabledRules
                        .stream()
                        .map(rule -> rule.endsWith(".yaml") ? rule : rule + ".yaml")
                        .map(rule -> rule.startsWith("/") ? rule.substring(1) : rule)
                        .collect(Collectors.toList());

        return Arrays.stream(rules)
                .flatMap(f -> {
                    if (f.isDirectory()) {
                        return Arrays.stream(Objects.requireNonNull(f.listFiles()))
                                .filter(File::isFile)
                                .map(file -> {
                                    try (Reader r = new FileReader(file)) {
                                        String fileName = file.getName();
                                        int dotIndex = fileName.lastIndexOf('.');

                                        if (dotIndex == -1 || !"yaml".equals(fileName.substring(dotIndex + 1))) {
                                            return null;
                                        }
                                        String ruleName = fileName.substring(0, dotIndex);
                                        fileName = f.getName() + '/' + fileName;
                                        if (!formedEnabledRules.contains(fileName) && !formedEnabledRules.contains(f.getName() + "/*.yaml")) {
                                            return null;
                                        }
                                        Rule rule = new Yaml().loadAs(r, Rule.class);
                                        if (rule == null) {
                                            return null;
                                        }
                                        rule.setName(ruleName);
                                        return rule;
                                    } catch (IOException e) {
                                        LOG.debug("Reading file {} failed", f, e);
                                    }
                                    return null;
                                });
                    } else {
                        try (Reader r = new FileReader(f)) {
                            String fileName = f.getName();
                            int dotIndex = fileName.lastIndexOf('.');
                            if (dotIndex == -1 || !"yaml".equals(fileName.substring(dotIndex + 1))) {
                                return null;
                            }
                            String ruleName = fileName.substring(0, dotIndex);
                            if (!formedEnabledRules.contains(fileName)) {
                                return null;
                            }
                            Rule rule = new Yaml().loadAs(r, Rule.class);
                            if (rule == null) {
                                return null;
                            }
                            rule.setName(ruleName);
                            return Stream.of(rule);
                        } catch (IOException e) {
                            LOG.debug("Reading file {} failed", f, e);
                        }
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
