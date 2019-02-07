package org.monitoring.stream.analytics.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.SocketOptions;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;


/**
 * Utils class for Hazelcast specific methods.
 */
public final class HazelcastUtils {

   

    private HazelcastUtils() {
    }

 
    public static  HazelcastInstance getClient(String config,String name) throws IOException {
	InputStream stream = new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8));
	ClientConfig clientConfig = new XmlClientConfigBuilder(stream).build();
	//configApp.setInstanceName(name);
	SocketOptions socketOptions = clientConfig.getNetworkConfig().getSocketOptions();
	socketOptions.setBufferSize(32);
	socketOptions.setKeepAlive(true);
	socketOptions.setTcpNoDelay(true);
	socketOptions.setReuseAddress(true);
	socketOptions.setLingerSeconds(5);
	HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
	return client;
    }
   
}
