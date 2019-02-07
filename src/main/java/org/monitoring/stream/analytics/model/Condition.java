package org.monitoring.stream.analytics.model;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
@JsonPropertyOrder({ "Attribute", "Operator", "Value", "DataType", "LogicalOperator" })
public class Condition implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("Attribute")
    private String attribute;
    @JsonProperty("Operator")
    private String operator;
    @JsonProperty("Value")
    private String value;
    @JsonProperty("DataType")
    private String dataType;
    @JsonProperty("LogicalOperator")
    private String logicalOperator;

    @JsonProperty("Attribute")
    public String getAttribute() {
    return attribute;
    }

    @JsonProperty("Attribute")
    public void setAttribute(String attribute) {
    this.attribute = attribute;
    }

    @JsonProperty("Operator")
    public String getOperator() {
    return operator;
    }

    @JsonProperty("Operator")
    public void setOperator(String operator) {
    this.operator = operator;
    }

    @JsonProperty("Value")
    public String getValue() {
    return value;
    }

    @JsonProperty("Value")
    public void setValue(String value) {
    this.value = value;
    }

    @JsonProperty("DataType")
    public String getDataType() {
    return dataType;
    }

    @JsonProperty("DataType")
    public void setDataType(String dataType) {
    this.dataType = dataType;
    }

    @JsonProperty("LogicalOperator")
    public String getLogicalOperator() {
    return logicalOperator;
    }

    @JsonProperty("LogicalOperator")
    public void setLogicalOperator(String logicalOperator) {
    this.logicalOperator = logicalOperator;
    }
    
    

    public Condition() {
	super();
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
	result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
	result = prime * result + ((logicalOperator == null) ? 0 : logicalOperator.hashCode());
	result = prime * result + ((operator == null) ? 0 : operator.hashCode());
	result = prime * result + ((value == null) ? 0 : value.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Condition other = (Condition) obj;
	if (attribute == null) {
	    if (other.attribute != null)
		return false;
	} else if (!attribute.equals(other.attribute))
	    return false;
	if (dataType == null) {
	    if (other.dataType != null)
		return false;
	} else if (!dataType.equals(other.dataType))
	    return false;
	if (logicalOperator == null) {
	    if (other.logicalOperator != null)
		return false;
	} else if (!logicalOperator.equals(other.logicalOperator))
	    return false;
	if (operator == null) {
	    if (other.operator != null)
		return false;
	} else if (!operator.equals(other.operator))
	    return false;
	if (value == null) {
	    if (other.value != null)
		return false;
	} else if (!value.equals(other.value))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return "Condition [attribute=" + attribute + ", operator=" + operator + ", value=" + value + ", dataType="
		+ dataType + ", logicalOperator=" + logicalOperator + "]";
    }

   
    
    

}
