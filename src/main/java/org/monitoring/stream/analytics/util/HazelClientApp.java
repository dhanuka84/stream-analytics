package org.monitoring.stream.analytics.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

public class HazelClientApp {
    
    public static void main(String ...strings) throws Exception {
	
	String sql = FileHandler.getFileContent("/home/dhanuka/projects/flink-research/stream-analytics/query.sql");
	String hzConfig = FileHandler.getFileContent("/home/dhanuka/projects/flink-research/stream-analytics/remote/hazelcast-client.xml");
	
	Properties props = FileHandler.loadPropertiesFromFile("/home/dhanuka/projects/flink-research/stream-analytics/application.properties");
	HazelcastInstance hzInst = HazelcastUtils.getClient(hzConfig, "dhanuka");
	MultiMap<String, String> distributedMap = hzInst.getMultiMap("masterDataSynch");
	String jobName = props.getProperty(ApplicationConfig.JOB_NAME);
	 System.out.println("======================= jobname "+jobName);
	 System.out.println("======================= size "+distributedMap.get(jobName).size());
	 
	 List<String> queries = new ArrayList<>();
	
	int count = 199;
	for(int i=2;i <= count; ++i) {
	    String query = sql.replace("Rule1", "Rule"+i);
	    query = query.replace("Dhanuka", "Dhanuka"+i);
	    distributedMap.put(jobName, query);
	    queries.add(query);
	    System.out.println(query);
	}
	
	
	
	hzInst.getLifecycleService().shutdown();
	
	/*int index = sql.indexOf("WHERE");
	sql = sql.substring(index);
	sql = sql.replace("WHERE", "(")+")";
	sql = sql.replace("Dhanuka", "Miro");
	System.out.println(sql);*/
	
	/*System.out.println(chunk(queries, 50).size());
	System.out.println(chunk(queries, 50));*/
	
	
	
    }
    
    public static List<List<String>> chunk(final List<String> arrayList, final int chunkSize) {

	int rest = arrayList.size() % chunkSize;
	int noOfChunks = arrayList.size() / chunkSize;
	int start = 0;
	int end = 0;
	int count = (rest == 0 ? noOfChunks: noOfChunks +1);

	System.out.println("rest  " + rest + " noOfChunks " + noOfChunks);
	List<List<String>> chunks = new ArrayList<>();
	for (int index = 0; index < count; ++index) {
	    if (index == 0 && rest > 0) {
		end = rest;
	    }else {
		end = start + chunkSize;
	    }

	    List<String> sublist = arrayList.subList(start, end);
	    start = end;
	    chunks.add(sublist);
	    //System.out.println(sublist);
	}
	return chunks;

    }

}
