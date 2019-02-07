package org.monitoring.stream.analytics.transform;

import java.io.IOException;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonParseException;
import org.monitoring.stream.analytics.model.Rule;
import org.monitoring.stream.analytics.util.JSONUtils;

/**
 * A serializer and deserializer for the {@link Rule} type.
 */
public class RuleParser implements DeserializationSchema<Rule>, SerializationSchema<Rule> {

    private static final long serialVersionUID = 1L;

    @Override
    public byte[] serialize(Rule evt) {
	try {
	    return JSONUtils.convertToByte(evt);
	} catch (Exception ex) {
	    throw new RuntimeException(ex);
	}

    }

    @Override
    public Rule deserialize(byte[] message) throws IOException {
	try {
	    Rule Rule = (Rule) JSONUtils.convertToObject(message, Rule.class);
	    return Rule;
	} catch (JsonParseException ex) {
	    throw new IOException(ex);
	}
    }

    @Override
    public boolean isEndOfStream(Rule nextElement) {
	return false;
    }

    @Override
    public TypeInformation<Rule> getProducedType() {
	return TypeInformation.of(Rule.class);
    }
}