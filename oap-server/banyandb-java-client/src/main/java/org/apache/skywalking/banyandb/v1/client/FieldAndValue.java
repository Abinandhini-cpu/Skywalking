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

package org.apache.skywalking.banyandb.v1.client;

import java.util.List;
import org.apache.skywalking.banyandb.v1.Banyandb;

public interface FieldAndValue extends Field {
    /**
     * @return field name
     */
    String getFieldName();

    /**
     * @return true if value is null;
     */
    boolean isNull();

    static FieldAndValue build(Banyandb.TypedPair typedPair) {
        if (typedPair.hasIntPair()) {
            final Banyandb.Int intPair = typedPair.getIntPair();
            if (typedPair.getIsNull()) {
                return new LongFieldPair(typedPair.getKey(), null);
            } else {
                return new LongFieldPair(typedPair.getKey(), intPair.getValue());
            }
        } else if (typedPair.hasStrPair()) {
            final Banyandb.Str strPair = typedPair.getStrPair();
            if (typedPair.getIsNull()) {
                return new StringFieldPair(typedPair.getKey(), null);
            } else {
                return new StringFieldPair(typedPair.getKey(), strPair.getValue());
            }
        } else if (typedPair.hasIntArrayPair()) {
            final Banyandb.IntArray intArrayPair = typedPair.getIntArrayPair();
            if (typedPair.getIsNull()) {
                return new LongArrayFieldPair(typedPair.getKey(), null);
            } else {
                return new LongArrayFieldPair(typedPair.getKey(), intArrayPair.getValueList());
            }
        } else if (typedPair.hasStrArrayPair()) {
            final Banyandb.StrArray strArrayPair = typedPair.getStrArrayPair();
            if (typedPair.getIsNull()) {
                return new StringArrayFieldPair(typedPair.getKey(), null);
            } else {
                return new StringArrayFieldPair(typedPair.getKey(), strArrayPair.getValueList());
            }
        }
        throw new IllegalArgumentException("Unrecognized TypedPair, " + typedPair);
    }

    class StringFieldPair extends StringField implements FieldAndValue {
        private final String fieldName;

        StringFieldPair(final String fieldName, final String value) {
            super(value);
            this.fieldName = fieldName;
        }

        @Override
        public String getFieldName() {
            return fieldName;
        }

        @Override
        public boolean isNull() {
            return value == null;
        }
    }

    class StringArrayFieldPair extends StringArrayField implements FieldAndValue {
        private final String fieldName;

        StringArrayFieldPair(final String fieldName, final List<String> value) {
            super(value);
            this.fieldName = fieldName;
        }

        @Override
        public String getFieldName() {
            return fieldName;
        }

        @Override
        public boolean isNull() {
            return value == null;
        }
    }

    class LongFieldPair extends LongField implements FieldAndValue {
        private final String fieldName;

        LongFieldPair(final String fieldName, final Long value) {
            super(value);
            this.fieldName = fieldName;
        }

        @Override
        public String getFieldName() {
            return null;
        }

        @Override
        public boolean isNull() {
            return value == null;
        }
    }

    class LongArrayFieldPair extends LongArrayField implements FieldAndValue {
        private final String fieldName;

        LongArrayFieldPair(final String fieldName, final List<Long> value) {
            super(value);
            this.fieldName = fieldName;
        }

        @Override
        public String getFieldName() {
            return fieldName;
        }

        @Override
        public boolean isNull() {
            return value == null;
        }
    }
}
