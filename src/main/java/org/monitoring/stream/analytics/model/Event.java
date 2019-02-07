package org.monitoring.stream.analytics.model;

import java.io.Serializable;
/**
 * Data type for events, consisting of the originating IP address and an event type.
 */
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "EventId","DriverID", "HireID", "CorrelationID", "PassengerName" })
public class Event  implements Serializable{


    private static final long serialVersionUID = 1L;
    
    @JsonProperty("EventId")
    private String eventId;

    @JsonProperty("DriverID")
    private String DriverID;
    @JsonProperty("HireID")
    private Integer HireID;
    @JsonProperty("CorrelationID")
    private String CorrelationID;
    @JsonProperty("PassengerName")
    private String providerInfo;

    @JsonProperty("DriverID")
    public String getDriverID() {
	return DriverID;
    }

    @JsonProperty("DriverID")
    public void setDriverID(String DriverID) {
	this.DriverID = DriverID;
    }

    @JsonProperty("HireID")
    public Integer getHireID() {
	return HireID;
    }

    @JsonProperty("HireID")
    public void setHireID(Integer HireID) {
	this.HireID = HireID;
    }

    @JsonProperty("CorrelationID")
    public String getCorrelationID() {
	return CorrelationID;
    }

    @JsonProperty("CorrelationID")
    public void setCorrelationID(String CorrelationID) {
	this.CorrelationID = CorrelationID;
    }

    @JsonProperty("PassengerName")
    public String getProviderInfo() {
	return providerInfo;
    }

    @JsonProperty("PassengerName")
    public void setProviderInfo(String providerInfo) {
	this.providerInfo = providerInfo;
    }
    
    


    public Event() {
	super();
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((DriverID == null) ? 0 : DriverID.hashCode());
	return result;
    }
    
    
    @JsonProperty("EventId")
    public String getEventId() {
        return eventId;
    }

    @JsonProperty("EventId")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Event other = (Event) obj;
	if (DriverID == null) {
	    if (other.DriverID != null)
		return false;
	} else if (!DriverID.equals(other.DriverID))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return "Event [DriverID=" + DriverID + ", HireID=" + HireID + ", CorrelationID=" + CorrelationID + ", providerInfo="
		+ providerInfo + ", eventType="  + "]";
    }
    
    

}