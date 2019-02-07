package org.monitoring.stream.analytics.util;

import org.apache.flink.table.descriptors.ConnectorDescriptor;
import org.apache.flink.table.descriptors.DescriptorProperties;

import java.util.HashMap;
import java.util.Map;


public class MySystemConnector extends ConnectorDescriptor {
    

    public MySystemConnector() {
      super("my-system", 1, false);
    }

    protected Map<String, String> toConnectorProperties() {
      Map<String, String> properties = new HashMap<>();
      //properties.put("connector.debug", Boolean.toString(isDebug));
      return properties;
    }

    @Override
    public void addConnectorProperties(DescriptorProperties arg0) {
	// TODO Auto-generated method stub
	
    }

}
