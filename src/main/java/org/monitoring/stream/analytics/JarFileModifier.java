package org.monitoring.stream.analytics;


import java.util.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.*;

public class JarFileModifier {
    public static void main(String [] args) throws Exception {
        /* Define ZIP File System Properies in HashMap */    
        Map<String, String> zip_properties = new HashMap<>(); 
        /* set create to true if you want to create a new ZIP file */
        zip_properties.put("create", "true");
        /* specify encoding to UTF-8 */
        zip_properties.put("encoding", "UTF-8");        
        /* Locate File on disk for creation */
        URI zip_disk = URI.create("jar:file:/home/dhanuka/projects/flink-research/stream-analytics/target/stream-analytics-0.0.1-SNAPSHOT.jar");
        /* Create ZIP file System */
        try (FileSystem zipfs = FileSystems.newFileSystem(zip_disk, zip_properties)) {
            /* Path to source file */   
            Path file_to_zip = Paths.get("/home/dhanuka/projects/APPLICATION_CONFIG/application.properties");
            /* Path inside ZIP File */
            Path pathInZipfile = zipfs.getPath("application.properties");          
            /* Add file to archive */
            Files.copy(file_to_zip,pathInZipfile); 
        } 
    }
}
