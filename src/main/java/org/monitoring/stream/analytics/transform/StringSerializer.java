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

/**
 * A serializer and deserializer for the {@link String} type.
 */
public class StringSerializer implements DeserializationSchema<String>, SerializationSchema<String> {

    private static final long serialVersionUID = 1L;
    private String encoding = "UTF8";

    @Override
    public byte[] serialize(String evt) {
	try {
	    return  evt.getBytes(encoding);
	} catch (Exception ex) {
	    throw new RuntimeException(ex);
	}

    }

    @Override
    public String deserialize(byte[] message) throws IOException {
	try {
	    return new String(message,encoding);
	} catch (Exception ex) {
	    throw new IOException(ex);
	}
    }

    @Override
    public boolean isEndOfStream(String nextElement) {
	return false;
    }

    @Override
    public TypeInformation<String> getProducedType() {
	return TypeInformation.of(String.class);
    }
}