/*******************************************************************************
 * Copyright [2016] [Dhanuka Ranasinghe]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.monitoring.stream.analytics.util;

import static java.nio.file.Files.readAllBytes;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*import com.google.gson.JsonElement;
import com.google.gson.JsonParser;*/

public class FileHandler {

	private final static Logger log = LoggerFactory.getLogger(FileHandler.class);
	
	public static ByteArrayInputStream getFileContentAsBytes(final String fileLocation) throws IOException {
		Path path = Paths.get(fileLocation);
		return new ByteArrayInputStream(readAllBytes(path));
	}

	public static InputStream getResourceAsStream(final String fileName) {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
		return is;
	}

	public static String readInputStream(final InputStream is) throws Exception {

		if (is == null) {
			return null;
		}

		String theString = null;
		StringWriter writer = new StringWriter();
		try {

			Charset charset = Charset.forName("UTF-8");
			IOUtils.copy(is, writer, charset);
			theString = writer.toString();
		} finally {
			try {
				close(writer);
				close(is);
			} catch (Exception ex) {

			}
		}
		return theString;

	}

	/*
	 * public static JsonElement getJSONElement(final String fileName) {
	 * InputStream partitionKeyIS = FileHandler.getResourceAsStream(fileName);
	 * JsonParser parser = new JsonParser(); JsonElement element = null; try {
	 * element = parser.parse(readInputStream(partitionKeyIS)); } catch
	 * (Exception e) { log.error(" Error while reading " + fileName); } return
	 * element; }
	 */

	public static void deleteAndWriteToFile(final String fileLocation, final String output) throws IOException {

		BufferedWriter bw = null;
		FileWriter fw = null;
		try {
			File file = new File(fileLocation);

			// System.out.println("output : "+output);

			// if file doesnt exists, then create it
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();

			fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			bw.write(output);
		} finally {
			close(bw);
		}

	}

	public static void createAndUpdateFile(final boolean replaceFile, final String fileLocation, final String output) {
		try {
			Path path = Paths.get(fileLocation);
			Path parentDir = path.getParent();
			if (!Files.exists(parentDir)) {
				Files.createDirectories(parentDir);
			}

			if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
				Files.createFile(path);
			} else if (replaceFile) {
				Files.delete(path);
				Files.createFile(path);
			}

			Files.write(path, output.getBytes(), StandardOpenOption.APPEND);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean isFileExist(final String fileLocation) {
		Path path = Paths.get(fileLocation);
		return Files.exists(path, LinkOption.NOFOLLOW_LINKS);
	}

	public static String getFileContent(final String fileLocation) throws IOException {
		Path path = Paths.get(fileLocation);
		return new String(readAllBytes(path));
	}

	public static Properties loadResourceProperties(final String propertyFile) throws IOException {
		Properties prop = new Properties();
		InputStream is = null;
		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertyFile);
			prop.load(is);
		} finally {
			close(is);
		}

		System.out.println(prop.toString());
		return prop;

	}

	public static void close(Closeable closer) {
		try {
			closer.close();
		} catch (Exception ex) {

		}
	}

	public static Properties readEnvConfig() throws IOException {
		String USER_HOME = System.getProperty("user.home");
		/* String applicationLogDir = System.getenv("APPLICATION_LOG") */;
		String applicationConfigDir = System.getenv("APPLICATION_CONFIG");
		/* String applicationDefaultLogDir = "/application_log"; */
		String applicationConfFileName = "/application.properties";

		if (applicationConfigDir == null) {
			log.info("log file directory doesn't configured");
			applicationConfigDir = USER_HOME + "/application_config";
		}

		return loadPropertiesFromFile(applicationConfigDir + applicationConfFileName);
	}

	public static Properties loadPropertiesFromFile(final String filePath) throws IOException {
		// reading property file
		Path path = Paths.get(filePath);

		// If it doesn't exist
		Path parentDir = path.getParent();
		if (!Files.exists(parentDir)) {
			log.info("config directory doesn't exist");
			Files.createDirectories(parentDir);
		}

		if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			log.info("config file doesn't exist");
			Files.createFile(path);
		}

		Properties properties = new Properties();
		byte[] bytes = readAllBytes(path);
		InputStream myInputStream = new ByteArrayInputStream(bytes);
		properties.load(myInputStream);
		

		return trimKeyValue(properties);
	}
	
	public static Properties trimKeyValue(final Properties properties){
		Properties latest = new Properties();
		Set<Entry<Object, Object>> entries = properties.entrySet();
		Iterator<Entry<Object, Object>> iter = entries.iterator();
		while(iter.hasNext()){
			Entry<Object, Object> entry = iter.next();
			if(entry.getKey() == null || entry.getValue() == null){
				continue;
			}
			String key = entry.getKey().toString().trim();
			String value = entry.getValue().toString().trim();
			latest.put(key, value);
		}
		return latest;
	}

	public static File createTemporaryFile(final InputStream in, final String prefix, final String suffix) {
		File tempFile = null;
		try {
			tempFile = File.createTempFile(prefix, suffix);
			tempFile.deleteOnExit();
			try (FileOutputStream out = new FileOutputStream(tempFile)) {
				IOUtils.copy(in, out);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tempFile;
	}

}
