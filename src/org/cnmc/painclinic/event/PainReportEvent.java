package org.cnmc.painclinic.event;

import java.util.Date;

public class PainReportEvent {
	/** The Constant EVENT_CREATED. */
	public static final int EVENT_CREATED = 100;

	/** The Constant EVENT_DELETED. */
	public static final int EVENT_DELETED = 101;

	/** The Constant EVENT_MODIFIED. */
	public static final int EVENT_MODIFIED = 102;
	
	/** The Constant EVENT_NOTIFIED. */
	public static final int EVENT_NOTIFIED = 103;
	
	private long patientID;

	private String description;
	private Date date;
	/** The timestamp. */
	private long timestamp;	
	/** The event type. */
	private int eventType;
	


	public PainReportEvent(){

	}

	public PainReportEvent(int type, long creationTime, Date creationDate, long patientId){
		this.eventType = type;
		this.timestamp = creationTime;
		this.date = creationDate;
		this.patientID = patientId; 
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getEventType() {
		return eventType;
	}

	public void setEventType(int eventType) {
		this.eventType = eventType;
	}
	
	public long getPatientID() {
		return patientID;
	}

	public void setPatientID(long patientID) {
		this.patientID = patientID;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	

	
	
}
