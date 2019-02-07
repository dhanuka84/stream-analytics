package org.monitoring.stream.analytics.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.monitoring.stream.analytics.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class JSONUtils {

	private final static Logger LOGGER = LoggerFactory.getLogger(JSONUtils.class);

	private static ObjectMapper mapper = new ObjectMapper();

	
	public static ObjectNode createEmptyJsonNode() {
		return mapper.createObjectNode();
	}
	
	public static String convertToString(final Object obj) {
		String jsonString = null;
		try {
			jsonString = mapper.writeValueAsString(obj);
		} catch (JsonProcessingException ex) {
			LOGGER.error("*************************  Error occurred while converting to String ************************"
					+ ApplicationException.getStackTrace(ex));
		}
		return jsonString;
	}
	
	public static byte[] convertToByte(final Object obj) {
		byte[] bytes = null;
		try {
			bytes = mapper.writeValueAsBytes(obj);
		} catch (JsonProcessingException ex) {
			LOGGER.error("*************************  Error occurred while converting to String ************************"
					+ ApplicationException.getStackTrace(ex));
		}
		return bytes;
	}

	public static Object convertToObject(final String content, Class type) throws IOException {
		Object obj = null;
		try {
			obj = mapper.readValue(content, type);
		} catch (JsonProcessingException ex) {
			LOGGER.error("*************************  Error occurred while converting to String ************************"
					+ ApplicationException.getStackTrace(ex));
		}
		return obj;
	}

	public static Object convertToObject(final JsonNode content, Class type) throws IOException {
		Object obj = null;
		try {
			obj = mapper.treeToValue(content, type);
		} catch (JsonProcessingException ex) {
			LOGGER.error("*************************  Error occurred while converting to Object ************************"
					+ ApplicationException.getStackTrace(ex));
		}
		return obj;
	}

	public static Object convertToObject(final byte[] content, Class type) throws IOException {
		Object obj = null;
		try {
			obj = mapper.readValue(content, type);
		} catch (JsonProcessingException ex) {
			LOGGER.error("*************************  Error occurred while converting to String ************************"
					+ ApplicationException.getStackTrace(ex));
		}
		return obj;
	}

	public static JsonNode convertToJSON(final InputStream stream) {
		JsonNode root = null;
		try {
			root = mapper.readTree(stream);
		} catch (Exception ex) {
			LOGGER.error("*************************  Error occurred while converting to String ************************"
					+ ApplicationException.getStackTrace(ex));
		}
		return root;
	}

	public static JsonNode convertToJSON(final String json) {
		JsonNode root = null;
		try {
			root = mapper.readTree(json);
		} catch (Exception ex) {
			LOGGER.error("*************************  Error occurred while converting to String ************************"
					+ ApplicationException.getStackTrace(ex));
		}
		return root;
	}

	public static JsonNode convertToJSON(final byte[] stream) {
		JsonNode root = null;
		try {
			root = mapper.readTree(stream);
		} catch (Exception ex) {
			LOGGER.error("*************************  Error occurred while converting to String: {} ************************"
					+ ApplicationException.getStackTrace(ex),new String(stream));
		}
		return root;
	}
	
	public static JsonNode convertToJSON(final Object object) {
		JsonNode root = null;
		try {
			root = mapper.valueToTree(object);
		} catch (Exception ex) {
			LOGGER.error("*************************  Error occurred while converting to JsonNode ************************"
					+ ApplicationException.getStackTrace(ex));
		}
		return root;
	}

	public static JsonNode getChild(JsonNode parent, final String key) {
		return parent.get(key);
	}
	    
	public static JsonNode getChildByPath(JsonNode parent, final String key) {
		return parent.findPath(key);
	}
    
    
	public static JsonNode getChild(final String content, final String path) throws IOException {
		JsonNode root = mapper.readTree(content);
		return root.at(path);
	}

	

	public static <T, V> Map<T, V> convertToMap(final String content, T keyType, V valueType) throws IOException {
		Map<T, V> obj = null;
		try {
			obj = mapper.readValue(content, new TypeReference<Map<T, V>>() {});

		} catch (JsonProcessingException ex) {
			LOGGER.error("*************************  Error occurred while converting to String ************************"
					+ ApplicationException.getStackTrace(ex));
		}
		return obj;
	}
	
	public static <V> List<V> convertToList(final String content, V valueType) throws IOException {
		List<V> obj = null;
		try {
			obj = mapper.readValue(content, new TypeReference<List<V>>() {});

		} catch (JsonProcessingException ex) {
			LOGGER.error("*************************  Error occurred while converting to String ************************"
					+ ApplicationException.getStackTrace(ex));
		}
		return obj;
	}
	
	public static String writeListToJsonArray(final List<Event> list) throws IOException {  

	    final ByteArrayOutputStream out = new ByteArrayOutputStream();
	    final ObjectMapper mapper = new ObjectMapper();

	    mapper.writeValue(out, list);

	    final byte[] data = out.toByteArray();
	    return new String(data);
	}

}
