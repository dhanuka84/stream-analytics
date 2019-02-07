package org.monitoring.stream.analytics.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
@JsonPropertyOrder({ "DriverID", "AlertId", "EventId", "ProcessTime" })
public class FilteredEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("DriverID")
    private String DriverID;
    @JsonProperty("AlertId")
    private String alertId;
    @JsonProperty("EventId")
    private String eventId;
    @JsonProperty("ProcessTime")
    private String processTime;
    

    public FilteredEvent(String DriverID, String alertId, String eventId, String processTime) {
	super();
	this.DriverID = DriverID;
	this.alertId = alertId;
	this.eventId = eventId;
	this.processTime = processTime;
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

    @JsonProperty("EventId")
    public String getEventId() {
	return eventId;
    }

    @JsonProperty("EventId")
    public void setEventId(String eventId) {
	this.eventId = eventId;
    }

    @JsonProperty("ProcessTime")
    public String getProcessTime() {
	return processTime;
    }

    @JsonProperty("ProcessTime")
    public void setProcessTime(String processTime) {
	this.processTime = processTime;
    }

}
