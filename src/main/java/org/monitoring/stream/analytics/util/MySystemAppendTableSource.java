package org.monitoring.stream.analytics.util;

import java.util.List;
import java.util.Map;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.sources.DefinedFieldMapping;
import org.apache.flink.table.sources.DefinedProctimeAttribute;
import org.apache.flink.table.sources.DefinedRowtimeAttributes;
import org.apache.flink.table.sources.RowtimeAttributeDescriptor;
import org.apache.flink.table.sources.StreamTableSource;
import org.apache.flink.types.Row;

public class MySystemAppendTableSource
	implements StreamTableSource<Row>, DefinedProctimeAttribute, DefinedRowtimeAttributes, DefinedFieldMapping {

    @Override
    public String explainSource() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public TypeInformation<Row> getReturnType() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public TableSchema getTableSchema() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Map<String, String> getFieldMapping() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public List<RowtimeAttributeDescriptor> getRowtimeAttributeDescriptors() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getProctimeAttribute() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public DataStream<Row> getDataStream(StreamExecutionEnvironment arg0) {
	// TODO Auto-generated method stub
	return null;
    }

}
