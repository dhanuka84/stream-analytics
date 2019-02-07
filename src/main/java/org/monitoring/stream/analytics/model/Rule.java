package org.monitoring.stream.analytics.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "DriverID", "AlertId", "Conditions" })
public class Rule implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("DriverID")
    private  String DriverID;
    @JsonProperty("AlertId")
    private  String alertId;
    @JsonProperty("Conditions")
    private  List<Condition> conditions = new ArrayList<>(10);

   

    public Rule() {
	super();
    }

    @JsonProperty("DriverID")
    public String getDriverID() {
    return DriverID;
    }

    @JsonProperty("DriverID")
    public void setDriverID(String DriverID) {
    this.DriverID = DriverID;
    }

    @JsonProperty("AlertId")
    public String getAlertId() {
    return alertId;
    }

    @JsonProperty("AlertId")
    public void setAlertId(String alertId) {
    this.alertId = alertId;
    }

    @JsonProperty("Conditions")
    public List<Condition> getConditions() {
    return conditions;
    }

    @JsonProperty("Conditions")
    public void setConditions(List<Condition> conditions) {
    this.conditions = conditions;
    }

    @Override
    public String toString() {
	return "Rule [DriverID=" + DriverID + ", alertId=" + alertId + ", conditions=" + conditions + "]";
    }

    
}
