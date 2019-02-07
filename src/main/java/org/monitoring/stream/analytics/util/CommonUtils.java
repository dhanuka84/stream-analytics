package org.monitoring.stream.analytics.util;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;


import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.nio.ConnectionManager;
import com.hazelcast.util.EmptyStatement;
import com.hazelcast.util.ExceptionUtil;

/**
 * Utils class for common methods.
 */
public final class CommonUtils {

	private static final Field ORIGINAL_FIELD;
	private static final int ASSERT_EVENTUALLY_TIMEOUT = 120;
	//This will decide whether has master data write permission
	public static volatile boolean canWriteToDB = false;
	public final static Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);
	
	private static final Encoder base64Encoder = Base64.getEncoder();
	private static final Decoder base64Decoder =  Base64.getDecoder();

	static {
		try {
			ORIGINAL_FIELD = HazelcastInstanceProxy.class.getDeclaredField("original");
			ORIGINAL_FIELD.setAccessible(true);
		} catch (Throwable t) {
			throw new IllegalStateException("Unable to get `original` field in `HazelcastInstanceProxy`!", t);
		}
	}

	private CommonUtils() {
	}

	public static String generateRandomString(int length) {
		StringBuilder sb = new StringBuilder(length);
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			char character = (char) (random.nextInt(26) + 'a');
			sb.append(character);
		}
		return sb.toString();
	}

	public static void assertTrue(String message, boolean condition) {
		if (!condition) {
			if (message == null) {
				throw new AssertionError();
			} else {
				throw new AssertionError(message);
			}
		}
	}

	public static void assertEquals(Object expected, Object actual) {
		assertEquals(null, expected, actual);
	}

	public static void assertEquals(String message, Object expected, Object actual) {
		if (!equalsRegardingNull(expected, actual)) {
			failNotEquals(message, expected, actual);
		}
	}

	private static boolean equalsRegardingNull(Object expected, Object actual) {
		if (expected == null) {
			return actual == null;
		}
		return isEquals(expected, actual);
	}

	private static boolean isEquals(Object expected, Object actual) {
		return expected.equals(actual);
	}

	private static void failNotEquals(String message, Object expected, Object actual) {
		throw new AssertionError(format(message, expected, actual));
	}

	private static String format(String message, Object expected, Object actual) {
		String formatted = "";
		if (message != null && !message.equals("")) {
			formatted = message + " ";
		}
		String expectedString = String.valueOf(expected);
		String actualString = String.valueOf(actual);
		if (expectedString.equals(actualString)) {
			return formatted + "expected: " + formatClassAndValue(expected, expectedString) + " but was: "
					+ formatClassAndValue(actual, actualString);
		} else {
			return formatted + "expected:<" + expectedString + "> but was:<" + actualString + ">";
		}
	}

	private static String formatClassAndValue(Object value, String valueString) {
		String className = value == null ? "null" : value.getClass().getName();
		return className + "<" + valueString + ">";
	}

	public static void sleepSeconds(int seconds) {
		try {
			SECONDS.sleep(seconds);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public static boolean sleepMillis(int millis) {
		try {
			MILLISECONDS.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
		return true;
	}

	public static void sleepAtLeastMillis(long sleepFor) {
		boolean interrupted = false;
		try {
			long remainingNanos = MILLISECONDS.toNanos(sleepFor);
			long sleepUntil = System.nanoTime() + remainingNanos;
			while (remainingNanos > 0) {
				try {
					NANOSECONDS.sleep(remainingNanos);
				} catch (InterruptedException e) {
					interrupted = true;
				} finally {
					remainingNanos = sleepUntil - System.nanoTime();
				}
			}
		} finally {
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}

	

	

	public static void assertOpenEventually(CountDownLatch latch) {
		assertOpenEventually(null, latch);
	}

	public static void assertOpenEventually(String message, CountDownLatch latch) {
		try {
			boolean completed = latch.await(ASSERT_EVENTUALLY_TIMEOUT, SECONDS);
			if (message == null) {
				assertTrue(String.format("CountDownLatch failed to complete within %d seconds , count left: %d",
						ASSERT_EVENTUALLY_TIMEOUT, latch.getCount()), completed);
			} else {
				assertTrue(String.format("%s, failed to complete within %d seconds , count left: %d", message,
						ASSERT_EVENTUALLY_TIMEOUT, latch.getCount()), completed);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static void assertTrueEventually(Runnable task, long timeoutSeconds) {
		try {
			AssertionError error = null;
			// we are going to check 5 times a second
			long iterations = timeoutSeconds * 5;
			int sleepMillis = 200;
			for (int i = 0; i < iterations; i++) {
				try {
					try {
						task.run();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					return;
				} catch (AssertionError e) {
					error = e;
				}
				sleepMillis(sleepMillis);
			}
			throw error;
		} catch (Throwable t) {
			throw ExceptionUtil.rethrow(t);
		}
	}

	public static void closeQuietly(Closeable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (IOException ignored) {
			EmptyStatement.ignore(ignored);
		}
	}

	public static void convertPropertiesToMap(final Map<String, String> configMap, final Properties configProps) {
		if (configProps != null && configMap != null) {
			for (final String name : configProps.stringPropertyNames()) {
				if(StringUtils.isBlank(name) || StringUtils.isBlank(configProps.getProperty(name))){
					continue;
				}
				configMap.put(name.trim(), configProps.getProperty(name).trim());
			}
		}
	}

	

	public static long getUnixTimestampNow() {
		DateTime utc = DateTime.now().withZone(DateTimeZone.UTC);
		long timestamp = utc.getMillis();
		return timestamp;
	}
	
	public static long getUnixTimestampToMidnight() {
		DateTime utc = new DateTime(DateTimeZone.UTC).withTimeAtStartOfDay();
		long timestamp = utc.getMillis();
		return timestamp;
	}

	public static String getUtcDate() {
		String format = "yyyy-MM-dd'T'HH:mm:ssZ";
		return DateTime.now().withZone(DateTimeZone.UTC).toString(format);
	}

	public static String getUtcDate(String format) {
		if (StringUtils.isBlank(format)) {
			format = "yyyy-MM-dd'T'HH:mm:ssZ";
		}
		return DateTime.now().withZone(DateTimeZone.UTC).toString(format);
	}
	
	public static String getUtcDate(long epochInMilliseconds, String format) {
		if (StringUtils.isBlank(format)) {
			format = "yyyy-MM-dd'T'HH:mm:ssZ";
		}
		return new DateTime(epochInMilliseconds).withZone(DateTimeZone.UTC).toString(format);
	}

	public static int getTimeDiffInSeconds(String beginTime, String endTime) {
		DateTimeFormatter formatter = geDateTimeFormatter();
		DateTime beginDateTime = formatter.parseDateTime(beginTime);
		DateTime endDateTime = formatter.parseDateTime(endTime);

		Seconds seconds = Seconds.secondsBetween(beginDateTime, endDateTime);
		return seconds.getSeconds();

	}
	
	public static DateTime getDateTimeFromString(String dateString) {
		DateTimeFormatter formatter = geDateTimeFormatter();
		return formatter.parseDateTime(dateString);
	}
	
	public static int compare(DateTime date1, DateTime date2) {
		return date1.compareTo(date2);
	}

	public static String encodeBase64(String stringToEncode) {
		return base64Encoder.encodeToString(stringToEncode.getBytes());
	}
	
	public static void copyEntries(Map<String,String> source, Map<String,String> target){
		Set<Entry<String, String>> entries = source.entrySet();
		for(Map.Entry<String,String> entry : entries){
			target.put(entry.getKey(), entry.getValue());
		}
	}
	
	public static DateTimeFormatter geDateTimeFormatter(){
		DateTimeParser[] parsers = { 
		        DateTimeFormat.forPattern( "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ" ).getParser(),
		        DateTimeFormat.forPattern( "yyyy-MM-dd'T'HH:mm:ssZ" ).getParser() };
		DateTimeFormatter formatter = new DateTimeFormatterBuilder().append( null, parsers ).toFormatter();
		return formatter;
	}
	
	public static boolean isTimeExpired(final LocalDateTime lastUpdatedTime, final int sessionTimeOutInMin, StringBuilder outputMsg){
		boolean isExpired = false;
		LocalDateTime now = LocalDateTime.now();
		Duration duration = Duration.between(lastUpdatedTime, now);
		long milliSeconds = duration.toMillis();
		if(outputMsg != null){
			outputMsg.append(milliSeconds);
		}
		if (milliSeconds >= (sessionTimeOutInMin * 60 * 1000)) {
			isExpired = true;
		}
		return isExpired;
	}

	public static HazelcastInstance getClusterInstance() {
		Set<HazelcastInstance> nodes = Hazelcast.getAllHazelcastInstances();
		if (nodes == null || nodes.isEmpty()) {
			LOGGER.warn("================================== Number of Local nodes ============================= " + 0);
			return null;
		}
	
		LOGGER.info("================================== Number of Local nodes ============================= "
				+ nodes.size());
		return nodes.iterator().next();
	}
	
	public static String getBase64String(final byte[] bytes){
		return base64Encoder.encodeToString(bytes);
	}
	
	public static byte[] getDecodeBase64Bytes(final String base64String){
		return base64Decoder.decode(base64String);
	}
	
}


